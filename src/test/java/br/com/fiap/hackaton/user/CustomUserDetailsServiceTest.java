package br.com.fiap.hackaton.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import br.com.fiap.v2i.user.CustomUserDetailsService;
import br.com.fiap.v2i.user.User;
import br.com.fiap.v2i.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    @DisplayName("Should return UserDetails when user exists")
    void shouldReturnUserDetailsWhenUserExists() {
        String username = "thiago";
        User user = User.create(username, "senhaSegura123");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("senhaSegura123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        String username = "usuarioInexistente";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername(username);
        });

        verify(userRepository, times(1)).findByUsername(username);
    }
}