package br.com.zup.edu.nossalojavirtual.users;

import base.SpringBootIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends SpringBootIntegrationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            entityManager.createQuery("delete from User").executeUpdate();
        });
    }

    @Test
    @DisplayName("Should register a user when all data is valid")
    public void test0() throws Exception {
        // Arange
        Map<String, String> payload = Map.of("login", "jose@zup.com.br", "password", "12345678");
        // Act & Assert
        mockMvc.perform(POST("/api/users", payload)).andExpect(status().isCreated());
        Long usersCount = (Long) entityManager.createQuery("select count(u.id) from User as u").getSingleResult();
        assertThat(usersCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should not register a user when fields that expects to be not blank or null is invalid")
    public void test1() throws Exception {
        // Arange
        Map<String, String> payload = Map.of();
        // Act
        String contentAsString = mockMvc.perform(POST("/api/users", payload))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        // Assert
        List<String> errors = mapper.readValue(contentAsString, List.class);
        assertThat(errors).containsExactlyInAnyOrder(
                "O campo login não deve estar em branco",
                "O campo password não deve estar em branco"
        );
    }

    @Test
    @DisplayName("Should not register a user when password has less than 6 characters")
    public void test2() throws Exception {
        // Arange
        Map<String, String> payload = Map.of("login", "jose@zup.com.br", "password", "1234");
        // Act
        String contentAsString = mockMvc.perform(POST("/api/users", payload))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        // Assert
        List<String> errors = mapper.readValue(contentAsString, List.class);
        assertThat(errors).containsExactlyInAnyOrder("O campo password tamanho deve ser entre 6 e 2147483647");
    }

    @Test
    @DisplayName("Should register a user with password encrypted")
    public void test3() throws Exception {
        // Arange
        Map<String, String> payload = Map.of("login", "jose@zup.com.br", "password", "12345678");
        // Act
        mockMvc.perform(POST("/api/users", payload)).andExpect(status().isCreated());
        // Assert
        Optional<User> possibleUser = userRepository.findByEmail("jose@zup.com.br");
        assertThat(possibleUser).isNotEmpty();
        User user = possibleUser.get();
        assertThat(user).extracting("password").isNotEqualTo("12345678");
    }

    @Test
    @DisplayName("Should register a user with the instant of registration")
    public void test4() throws Exception {
        // Arange
        Map<String, String> payload = Map.of("login", "jose@zup.com.br", "password", "12345678");
        // Act
        mockMvc.perform(POST("/api/users", payload)).andExpect(status().isCreated());
        // Assert
        Optional<User> possibleUser = userRepository.findByEmail("jose@zup.com.br");
        assertThat(possibleUser).isNotEmpty();
        User user = possibleUser.get();
        assertThat(user).extracting("createdAt")
                .matches((o) -> ((LocalDateTime) o).isBefore(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should register only 1 user with a email")
    public void test5() throws Exception {
        // Arange
        String email = "jose@zup.com.br";
        User firstUser = new User(email, Password.encode("12345678"));
        userRepository.save(firstUser);
        Map<String, String> payload = Map.of("login", email, "password", "12345678");
        // Act
        String contentAsString = mockMvc.perform(POST("/api/users", payload))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        // Assert
        List<String> errors = mapper.readValue(contentAsString, List.class);
        assertThat(errors).containsExactlyInAnyOrder("O campo login is already registered");
    }
}
