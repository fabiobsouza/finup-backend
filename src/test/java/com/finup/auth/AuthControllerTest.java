package com.finup.auth;

import com.finup.user.User;
import com.finup.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void testLogin_Success() {
        LoginRequest req = new LoginRequest("test@example.com", "password");
        User user = User.builder().id(1L).name("Test User").email("test@example.com").build();
        
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mocked-jwt-token");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        ResponseEntity<?> result = authController.login(req, response);
        
        assertEquals(200, result.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) result.getBody();
        assertEquals(1L, body.get("id"));
        assertEquals("test@example.com", body.get("email"));
        
        Cookie cookie = response.getCookie("finup_token");
        assertNotNull(cookie);
        assertEquals("mocked-jwt-token", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testRegister_Success() {
        RegisterRequest req = new RegisterRequest("New User", "new@example.com", "password");
        
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("mocked-jwt-token");
        
        doAnswer(invocation -> {
            User passedUser = invocation.getArgument(0);
            passedUser.setId(1L);
            return passedUser;
        }).when(userRepository).save(any(User.class));
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        ResponseEntity<?> result = authController.register(req, response);
        
        assertEquals(201, result.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) result.getBody();
        assertEquals("new@example.com", body.get("email"));
        
        Cookie cookie = response.getCookie("finup_token");
        assertNotNull(cookie);
        assertEquals("mocked-jwt-token", cookie.getValue());
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("New User", savedUser.getName());
        assertEquals("encoded-password", savedUser.getPassword());
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest("New User", "existing@example.com", "password");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseEntity<?> result = authController.register(req, response);
        
        assertEquals(400, result.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) result.getBody();
        assertEquals("E-mail já cadastrado", body.get("message"));
    }

    @Test
    void testLogout() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseEntity<?> result = authController.logout(response);
        
        assertEquals(200, result.getStatusCodeValue());
        Cookie cookie = response.getCookie("finup_token");
        assertNotNull(cookie);
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
    }

    @Test
    void testMe_Authenticated() {
        User user = User.builder().id(1L).name("Test User").email("test@example.com").build();
        ResponseEntity<?> result = authController.me(user);
        
        assertEquals(200, result.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) result.getBody();
        assertEquals("test@example.com", body.get("email"));
    }

    @Test
    void testMe_NotAuthenticated() {
        ResponseEntity<?> result = authController.me(null);
        
        assertEquals(401, result.getStatusCodeValue());
    }
}
