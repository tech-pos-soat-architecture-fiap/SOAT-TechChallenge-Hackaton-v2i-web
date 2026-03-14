package br.com.fiap.hackaton.user;

import br.com.fiap.v2i.user.User;
import br.com.fiap.v2i.user.UserController;
import br.com.fiap.v2i.user.UserRegistrationDTO;
import br.com.fiap.v2i.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {

    @Test
    void shouldRegisterUser() {

        UserRepository userRepository = Mockito.mock(UserRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);

        Mockito.when(passwordEncoder.encode("123456"))
                .thenReturn("encodedPassword");

        UserController controller =
                new UserController(userRepository, passwordEncoder);

        UserRegistrationDTO dto = new UserRegistrationDTO(
                "maria",
                "123456",
                "maria@example.com"
        );

        ResponseEntity<String> response = controller.register(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User registered successfully!", response.getBody());

        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }
}