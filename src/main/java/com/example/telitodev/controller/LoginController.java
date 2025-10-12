package com.example.telitodev.controller;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Optional;

@Controller
public class LoginController extends BaseController {

    final UsuarioRepository usuarioRepository;

    public LoginController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "detail", required = false) String detail,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model, HttpSession session, Authentication auth,
                                HttpServletRequest request) {

        // Log para capturar todas las URLs que llegan al login
        System.out.println("🔍 DEBUG URL RECEIVED:");
        System.out.println("   - Request URL: " + request.getRequestURL());
        System.out.println("   - Query String: " + request.getQueryString());
        System.out.println("   - Full URL: " + request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            return "redirect:/dashboard";
        }
        
        if (error != null) {
            String errorMessage = "Credenciales inválidas";
            
            System.out.println("🔍 DEBUG LOGIN ERROR:");
            System.out.println("   - Error parameter presente: " + error);
            System.out.println("   - Detail parameter: " + detail);

            // Si es error OAuth2, mostrar información específica
            if ("oauth2".equals(error)) {
                errorMessage = "Error en autenticación OAuth2";

                // Obtener detalles del error de la sesión
                String oauth2ErrorMessage = (String) session.getAttribute("OAUTH2_ERROR");
                String oauth2ErrorType = (String) session.getAttribute("OAUTH2_ERROR_TYPE");

                if (oauth2ErrorMessage != null) {
                    System.out.println("   - OAuth2 Error Message: " + oauth2ErrorMessage);
                    errorMessage = "Error OAuth2: " + oauth2ErrorMessage;
                }

                if (oauth2ErrorType != null) {
                    System.out.println("   - OAuth2 Error Type: " + oauth2ErrorType);
                }

                // Limpiar errores de la sesión
                session.removeAttribute("OAUTH2_ERROR");
                session.removeAttribute("OAUTH2_ERROR_TYPE");

                model.addAttribute("error", errorMessage);
                return "sesion/login";
            }

            // Verificar si es error por usuario desactivado en tiempo real
            if ("disabled".equals(error)) {
                errorMessage = "Su cuenta ha sido desactivada. Comuníquese con el administrador.";
                System.out.println("   - ✅ Usuario desactivado en tiempo real - aplicando mensaje específico");
                model.addAttribute("error", errorMessage);
                return "sesion/login";
            }
            
            // Obtener la excepción de autenticación de la sesión
            Exception exception = (Exception) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            
            if (exception != null) {
                System.out.println("   - Excepción encontrada: " + exception.getClass().getSimpleName());
                System.out.println("   - Mensaje completo: '" + exception.getMessage() + "'");
                System.out.println("   - Tipo exacto: " + exception.getClass().getName());
                
                // Verificar si es nuestra excepción personalizada o contiene el mensaje específico
                if (exception.getClass().getName().contains("UsuarioDesactivadoException") ||
                    (exception.getMessage() != null && exception.getMessage().contains("Usuario inactivo"))) {
                    errorMessage = "Usuario inactivo. Comuníquese con el administrador.";
                    System.out.println("   - ✅ Aplicando mensaje de usuario desactivado");
                } else {
                    System.out.println("   - ❌ Aplicando mensaje de credenciales inválidas");
                }
            } else {
                System.out.println("   - ❌ No se encontró excepción en la sesión");
            }
            
            System.out.println("   - Mensaje final: '" + errorMessage + "'");
            model.addAttribute("error", errorMessage);
        }
        
        if (logout != null) {
            model.addAttribute("logout", "Sesión cerrada con éxito");
        }
        
        return "sesion/login";
    }



    @GetMapping("/dashboard")
    public String home(Authentication authentication) {
        System.out.println("=== LoginController.dashboard() llamado ===");
        
        if (authentication == null) {
            System.out.println("Sin autenticación, redirigiendo a login");
            return "redirect:/login";
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        System.out.println("Usuario autenticado: " + authentication.getName());
        System.out.println("Autoridades: " + authorities);

        for (GrantedAuthority authority : authorities) {
            System.out.println("Procesando autoridad: " + authority.getAuthority());
            switch (authority.getAuthority()) {
                case "ROLE_SUPERADMIN":
                    System.out.println("Redirigiendo SUPERADMIN a /admin/home");
                    return "redirect:/admin/home";
                case "ROLE_DEV":
                case "ROLE_DEVELOPER":
                    System.out.println("Redirigiendo DEVELOPER a /dev/home");
                    return "redirect:/dev/home";
                case "ROLE_QA":
                    System.out.println("Redirigiendo QA a /qa/home");
                    return "redirect:/qa/home";
                case "ROLE_PO":
                    System.out.println("Redirigiendo PO a /po/home");
                    return "redirect:/po/home";
                default:
                    System.out.println("Autoridad no reconocida: " + authority.getAuthority());
                    break;
            }
        }

        System.out.println("No se encontró autoridad válida, redirigiendo a login");
        return "redirect:/login";
    }

    // Endpoint temporal para manejar redirecciones automáticas a /home
    @GetMapping("/home")
    public String homeRedirect(Authentication authentication, HttpServletRequest request) {
        System.out.println("=== ENDPOINT TEMPORAL /home LLAMADO ===");
        System.out.println("URL completa: " + request.getRequestURL() + "?" + request.getQueryString());

        if (authentication != null) {
            System.out.println("Usuario autenticado: " + authentication.getName());
            System.out.println("Autoridades: " + authentication.getAuthorities());

            // Redirigir al dashboard correcto según el rol
            return home(authentication);
        } else {
            System.out.println("Sin autenticación, redirigiendo a login");
            return "redirect:/login";
        }
    }

    // Endpoint para verificar configuración OAuth2
    @GetMapping("/oauth2-test")
    public String oauth2Test(Model model) {
        System.out.println("=== TESTING OAUTH2 CONFIGURATION ===");
        model.addAttribute("message", "OAuth2 Test - Click the button below to test Google OAuth2");
        return "oauth2-test"; // Vamos a crear esta página
    }

    // Endpoint para capturar errores OAuth2 específicos
    @GetMapping("/oauth2-debug")
    public String oauth2Debug(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            @RequestParam(value = "error_uri", required = false) String errorUri,
            HttpServletRequest request,
            Model model) {

        System.out.println("=== OAUTH2 DEBUG ERROR ===");
        System.out.println("Error: " + error);
        System.out.println("Error Description: " + errorDescription);
        System.out.println("Error URI: " + errorUri);
        System.out.println("Full URL: " + request.getRequestURL() + "?" + request.getQueryString());

        model.addAttribute("error", error);
        model.addAttribute("errorDescription", errorDescription);
        model.addAttribute("errorUri", errorUri);

        return "oauth2-test"; // Reutilizar la misma página
    }

    /**
     * Endpoint para manejar el éxito de OAuth2
     */
    @GetMapping("/oauth2-success")
    public String handleOAuth2Success(Authentication authentication, Model model) {
        System.out.println("=== OAuth2 Success Handler ===");

        if (authentication != null && authentication.isAuthenticated()) {
            System.out.println("Usuario OAuth2 autenticado: " + authentication.getName());
            System.out.println("Tipo de principal: " + authentication.getPrincipal().getClass().getName());

            // Para usuarios OAuth2, redirigir directamente al dashboard apropiado
            return home(authentication);
        }

        System.out.println("Error: OAuth2 authentication falló");
        model.addAttribute("error", "Error en la autenticación con Google. Inténtelo nuevamente.");
        return "sesion/login";
    }

    /**
     * Endpoint para manejar errores de OAuth2
     */
    @GetMapping("/oauth2-error")
    public String handleOAuth2Error(@RequestParam(value = "error", required = false) String error, Model model) {
        System.out.println("=== OAuth2 Error Handler ===");
        System.out.println("Error OAuth2: " + error);

        String errorMessage = "Error en la autenticación con Google. Inténtelo nuevamente.";

        if (error != null) {
            switch (error) {
                case "access_denied":
                    errorMessage = "Acceso denegado. Debe autorizar el acceso para continuar.";
                    break;
                case "unauthorized_client":
                    errorMessage = "Cliente no autorizado. Contacte al administrador.";
                    break;
                default:
                    errorMessage = "Error en la autenticación con Google: " + error;
                    break;
            }
        }

        model.addAttribute("error", errorMessage);
        return "sesion/login";
    }
}

