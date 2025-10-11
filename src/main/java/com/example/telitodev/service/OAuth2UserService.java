package com.example.telitodev.service;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.Rol;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

/**
 * Servicio personalizado para manejar la autenticación OAuth2
 * Se encarga del onboarding automático de usuarios externos
 */
@Service("oAuth2UserService")
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    private static final String DEFAULT_EXTERNAL_ROLE = "DEV"; // Rol por defecto para usuarios externos

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // Llamar al servicio padre para obtener los datos del usuario OAuth2
            OAuth2User oAuth2User = super.loadUser(userRequest);
            
            // Procesar el usuario OAuth2
            return processOAuth2User(userRequest, oAuth2User);
            
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException("Error al procesar usuario OAuth2: " + ex.getMessage());
        }
    }

    @Transactional
    public OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // Extraer datos del usuario según el proveedor
        UserInfo userInfo = extractUserInfo(registrationId, attributes);
        
        // Buscar usuario existente
        Usuario usuario = findExistingUser(userInfo);
        
        if (usuario == null) {
            // Crear nuevo usuario externo
            usuario = createNewExternalUser(userInfo, registrationId);
        } else {
            // Actualizar usuario existente si es necesario
            updateExistingUser(usuario, userInfo, registrationId);
        }
        
        // Crear las autoridades basadas en el rol del usuario
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombreRol())
        );
        
        // Retornar el usuario OAuth2 con las autoridades correctas
        return new DefaultOAuth2User(authorities, attributes, "sub");
    }
    
    @Transactional
    public OAuth2User processOAuth2UserFromToken(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // Extraer datos del usuario (asumimos Google)
        UserInfo userInfo = extractUserInfo("google", attributes);
        
        // Buscar usuario existente por correo o provider ID
        Usuario usuario = findExistingUser(userInfo);
        
        if (usuario == null) {
            // Usuario nuevo - crear cuenta automáticamente
            usuario = createNewExternalUser(userInfo, "google");
        } else {
            // Usuario existente - actualizar información OAuth2 si es necesario
            updateExistingUser(usuario, userInfo, "google");
        }
        
        // Verificar que el usuario esté activo
        if (!usuario.getEstado()) {
            throw new OAuth2AuthenticationException("Usuario desactivado. Comuníquese con el administrador.");
        }
        
        // Crear autoridades basadas en el rol del usuario
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombreRol())
        );
        
        // Crear nuevo OAuth2User con las autoridades correctas de nuestra BD
        return new DefaultOAuth2User(authorities, attributes, "sub");
    }
    
    private UserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
        UserInfo userInfo = new UserInfo();
        
        if ("google".equals(registrationId)) {
            userInfo.setProviderId((String) attributes.get("sub"));
            userInfo.setEmail((String) attributes.get("email"));
            userInfo.setName((String) attributes.get("name"));
            userInfo.setGivenName((String) attributes.get("given_name"));
            userInfo.setFamilyName((String) attributes.get("family_name"));
            
        } else {
            // Aquí puedes agregar soporte para otros proveedores (Azure AD, Okta, etc.)
            throw new OAuth2AuthenticationException("Proveedor OAuth2 no soportado: " + registrationId);
        }
        
        return userInfo;
    }
    
    private Usuario findExistingUser(UserInfo userInfo) {
        // Primero buscar por correo
        Usuario usuario = usuarioRepository.findByCorreo(userInfo.getEmail());
        
        if (usuario != null) {
            return usuario;
        }
        
        // Luego buscar por provider ID
        return usuarioRepository.findByOauthProviderIdAndOauthProvider(
            userInfo.getProviderId(), 
            "google" // o el proveedor correspondiente
        ).orElse(null);
    }
    
    @Transactional
    private Usuario createNewExternalUser(UserInfo userInfo, String provider) {
        // Generar DNI único para usuario externo (8 dígitos aleatorios)
        String dni = generateUniqueExternalDni();
        
        // Obtener rol por defecto para usuarios externos - manejo de duplicados
        Rol rolDefault;
        try {
            List<Rol> rolesDeveloper = rolRepository.findAll().stream()
                .filter(r -> DEFAULT_EXTERNAL_ROLE.equals(r.getNombreRol()))
                .collect(java.util.stream.Collectors.toList());
                
            if (!rolesDeveloper.isEmpty()) {
                rolDefault = rolesDeveloper.get(0); // Usar el primer DEVELOPER encontrado
            } else {
                // Fallback: buscar cualquier rol disponible
                Optional<Rol> fallbackRole = rolRepository.findById(2); // DEV como fallback
                rolDefault = fallbackRole.orElseThrow(() -> 
                    new OAuth2AuthenticationException("No se encontró ningún rol válido"));
            }
        } catch (Exception e) {
            throw new OAuth2AuthenticationException("Error al asignar rol al usuario externo: " + e.getMessage());
        }
        
        // Crear nuevo usuario externo
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setDni(dni);
        nuevoUsuario.setNombre(userInfo.getGivenName() != null ? userInfo.getGivenName() : userInfo.getName());
        nuevoUsuario.setApellidoPaterno(userInfo.getFamilyName() != null ? userInfo.getFamilyName() : "Externo");
        nuevoUsuario.setApellidoMaterno(""); // Vacío para usuarios externos
        nuevoUsuario.setCorreo(userInfo.getEmail());
        nuevoUsuario.setContrasena(null); // Sin contraseña para usuarios OAuth2
        nuevoUsuario.setAlias(generateAlias(userInfo.getName()));
        nuevoUsuario.setFechaRegistro(Timestamp.valueOf(LocalDateTime.now()));
        nuevoUsuario.setEstado(true);
        nuevoUsuario.setRol(rolDefault);
        nuevoUsuario.setTipoAcceso(Usuario.TipoAcceso.externo);
        nuevoUsuario.setOauthProviderId(userInfo.getProviderId());
        nuevoUsuario.setOauthProvider(provider);
        
        return usuarioRepository.save(nuevoUsuario);
    }
    
    @Transactional
    private void updateExistingUser(Usuario usuario, UserInfo userInfo, String provider) {
        boolean needsUpdate = false;
        
        // Actualizar información OAuth2 si el usuario no la tiene
        if (usuario.getOauthProviderId() == null) {
            usuario.setOauthProviderId(userInfo.getProviderId());
            usuario.setOauthProvider(provider);
            needsUpdate = true;
        }
        
        // Actualizar tipo de acceso si no está configurado
        if (usuario.getTipoAcceso() == null) {
            usuario.setTipoAcceso(Usuario.TipoAcceso.externo);
            needsUpdate = true;
        }
        
        if (needsUpdate) {
            usuarioRepository.save(usuario);
        }
    }
    
    private String generateUniqueExternalDni() {
        String dni;
        do {
            // Generar DNI de 8 dígitos empezando con 9 (para distinguir usuarios externos)
            dni = "9" + String.format("%07d", new Random().nextInt(10000000));
        } while (usuarioRepository.findByDni(dni) != null);
        
        return dni;
    }
    
    private String generateAlias(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "external_user";
        }
        
        // Limpiar el nombre y crear alias
        String cleanName = name.toLowerCase()
            .replaceAll("[^a-z0-9]", "")
            .substring(0, Math.min(name.length(), 10));
            
        return "ext_" + cleanName;
    }
    
    /**
     * Clase interna para manejar la información del usuario OAuth2
     */
    private static class UserInfo {
        private String providerId;
        private String email;
        private String name;
        private String givenName;
        private String familyName;
        
        // Getters y setters
        public String getProviderId() { return providerId; }
        public void setProviderId(String providerId) { this.providerId = providerId; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getGivenName() { return givenName; }
        public void setGivenName(String givenName) { this.givenName = givenName; }
        
        public String getFamilyName() { return familyName; }
        public void setFamilyName(String familyName) { this.familyName = familyName; }
    }
}