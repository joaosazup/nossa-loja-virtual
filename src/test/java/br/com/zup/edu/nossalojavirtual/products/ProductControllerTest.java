package br.com.zup.edu.nossalojavirtual.products;

import base.SpringBootIntegrationTest;
import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LOCAL_DATE_TIME;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest extends SpringBootIntegrationTest {
    @Autowired
    private ProductRepository productRepository;
    private User user;

    @BeforeEach
    void setUp() {
//        doInTransaction(() -> {
//            entityManager.createNativeQuery("delete from PRODUCT_CHARACTERISTCS").executeUpdate();
//            entityManager.createNativeQuery("delete from PRODUCT_PHOTOS").executeUpdate();
//        });
//        executeQueryInTransaction("delete from Product");
        productRepository.deleteAll();
//        doInTransaction(() -> {
//            entityManager.createQuery("select p from Product as p inner join fetch p.characteristics").getResultList().forEach(o -> entityManager.remove(o));
//        });
        executeQueryInTransaction("delete from Category");
        executeQueryInTransaction("delete from User");
    }

    private Map<String, Object> makeValidProductPayload() {
        Category category = new Category("Celulares");
        this.saveAEntity(category);

        List<Object> characteristicsPayload = List.of(
                Map.of("name", "Bateria", "description", "dura 1 dia de reprodução de video"),
                Map.of("name", "Tela", "description", "Oled"),
                Map.of("name", "Processador", "description", "A13 Bionic")
        );
        List<String> photosPayload = List.of(
                "https://conteudo.imguol.com.br/c/noticias/19/2022/09/07/iphone-14-1662573049403_v2_4x3.png");
        Map<String, Object> payload = new HashMap<>();
        payload.putAll(Map.of(
                "name", "Iphone",
                "price", 3499.9,
                "stockQuantity", 113,
                "photos", photosPayload,
                "characteristics", characteristicsPayload,
                "description", "Iphone x, tal tal tal",
                "categoryId", category.getId()
        ));
        return payload;
    }

    public JwtRequestPostProcessor makeJwtWithUser() {
        user = new User("jose@zup.com.br", Password.encode("12345678"));
        this.saveAEntity(user);
        return jwt().jwt(builder -> builder.claim("email", user.getUsername()))
                .authorities(new SimpleGrantedAuthority("SCOPE_products:write"));
    }

    @Test
    @Transactional
    @DisplayName("Should create a product when all data is valid")
    public void test0() throws Exception {
        // Arange
        Map<String, Object> payload = makeValidProductPayload();
        // Act & Assert
        mockMvc.perform(POST("/api/products", payload).with(makeJwtWithUser()))
                .andExpect(status().isCreated());
        Optional<Product> possibleProduct = productRepository.findByUser(user).stream().findFirst();
        assertThat(possibleProduct).isNotEmpty();
        Product product = possibleProduct.get();
        assertThat(product.getPhotos()).hasSize(1);
        assertThat(product.getCharacteristics()).hasSize(3);
    }

    @Test
    @DisplayName("Should not create a product when data is invalid")
    public void test1() throws Exception {
        // Arange
        Map<String, Object> payload = Map.of(
                "name", "",
                "price", 0,
                "stockQuantity", -1,
                "photos", List.of(),
                "characteristics", List.of(),
                "description", "x".repeat(1001),
                "categoryId", Long.MAX_VALUE
        );
        // Act & Assert
        String contentAsString = mockMvc.perform(POST("/api/products", payload).with(makeJwtWithUser()))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(
                        StandardCharsets.UTF_8);
        List<String> errors = mapper.readValue(contentAsString, List.class);
        assertThat(errors).containsExactlyInAnyOrder(
                "O campo photos tamanho deve ser entre 1 e 2147483647",
                "O campo price deve ser maior que ou igual a 0.01",
                "O campo description o comprimento deve ser entre 0 e 1000",
                "O campo characteristics tamanho deve ser entre 3 e 2147483647",
                "O campo stockQuantity deve ser maior que ou igual à 0",
                "O campo name não deve estar em branco",
                "O campo categoryId Category categoryId is not registered"
        );
    }

    @Test
    @DisplayName("Should create a product and save the current instante of the creation")
    public void test2() throws Exception {
        // Arange
        Map<String, Object> payload = makeValidProductPayload();
        // Act & Assert
        mockMvc.perform(POST("/api/products", payload).with(makeJwtWithUser()))
                .andExpect(status().isCreated());
        Optional<Product> possibleProduct = productRepository.findByUser(user).stream().findFirst();
        assertThat(possibleProduct).isNotEmpty();
        Product product = possibleProduct.get();
        assertThat(product).extracting("createdAt")
                .asInstanceOf(LOCAL_DATE_TIME)
                .isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should not create a product when not provide categoryId")
    public void test3() throws Exception {
        // Arange
        Map<String, Object> payload = makeValidProductPayload();
        payload.remove("categoryId");
        // Act & Assert
        String contentAsString = mockMvc.perform(POST("/api/products", payload).with(makeJwtWithUser()))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        List<String> errors = mapper.readValue(contentAsString, List.class);
        assertThat(errors).containsExactlyInAnyOrder("O campo categoryId não deve ser nulo");
    }
    
    @Test
    @DisplayName("Should not create a product when the user is not authenticated")
    public void test5() throws Exception {
        // Arange
        Map<String, Object> payload = makeValidProductPayload();
        // Act & Assert
        mockMvc.perform(POST("/api/products", payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should not create a product when the user is authenticated but not have the permissions")
    public void test6() throws Exception {
        // Arange
        Map<String, Object> payload = makeValidProductPayload();
        // Act & Assert
        mockMvc.perform(POST("/api/products", payload).with(jwt()))
                .andExpect(status().isForbidden());
    }
}
