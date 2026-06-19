package com.finup.auth;

import com.finup.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Atribui valores para os fields @Value utilizando reflection para não precisar levantar o Spring
        ReflectionTestUtils.setField(jwtService, "secret", "MinhaChaveSecretaSuperSeguraParaTestesUnitariosDoFinup");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L); // 1 hora
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        User user = User.builder().email("test@example.com").build();

        String token = jwtService.generateToken(user);
        
        assertNotNull(token);
        String username = jwtService.extractUsername(token);
        assertEquals("test@example.com", username);
    }

    @Test
    void testIsTokenValid() {
        User user = User.builder().email("test@example.com").build();
        String token = jwtService.generateToken(user);
        
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void testIsTokenValid_WrongUser() {
        User user1 = User.builder().email("test1@example.com").build();
        User user2 = User.builder().email("test2@example.com").build();
        
        String token = jwtService.generateToken(user1);
        
        assertFalse(jwtService.isTokenValid(token, user2));
    }

    @Test
    void testIsTokenValid_ExpiredToken() {
        // Reduz o tempo de expiração para o passado
        ReflectionTestUtils.setField(jwtService, "expirationMs", -3600000L); 
        
        User user = User.builder().email("test@example.com").build();
        String token = jwtService.generateToken(user);
        
        // io.jsonwebtoken lança ExpiredJwtException quando tenta parsear token expirado
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(token, user);
        });
    }
}
