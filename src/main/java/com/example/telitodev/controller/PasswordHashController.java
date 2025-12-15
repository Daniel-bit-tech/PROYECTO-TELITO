package com.example.telitodev.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PasswordHashController {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @GetMapping("/api/generate-hash")
    public String generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        System.out.println("🔐 HASH GENERADO:");
        System.out.println("   - Password: " + password);
        System.out.println("   - Hash: " + hash);
        System.out.println("   - Length: " + hash.length());
        
        // Verificar que el hash valida correctamente
        boolean matches = passwordEncoder.matches(password, hash);
        System.out.println("   - Validation: " + (matches ? "✅ OK" : "❌ FAIL"));
        
        return "Password: " + password + "\n" +
               "Hash: " + hash + "\n" +
               "Length: " + hash.length() + "\n" +
               "Validates: " + matches;
    }
    
    @GetMapping("/api/verify-hash")
    public String verifyHash(@RequestParam String password, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        System.out.println("🔍 VERIFICACIÓN:");
        System.out.println("   - Password: " + password);
        System.out.println("   - Hash: " + hash);
        System.out.println("   - Matches: " + (matches ? "✅ OK" : "❌ FAIL"));
        
        return "Password: " + password + "\n" +
               "Hash: " + hash + "\n" +
               "Matches: " + (matches ? "✅ YES" : "❌ NO");
    }
}
