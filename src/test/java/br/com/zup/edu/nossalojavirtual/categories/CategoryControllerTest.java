package br.com.zup.edu.nossalojavirtual.categories;

import base.SpringBootIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest extends SpringBootIntegrationTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        executeQueryInTransaction("delete from Category");
    }

    @Test
    @DisplayName("Should create a category without super category when all data is valid")
    public void test0() throws Exception {
        // Arange
        Map<String, String> payload = Map.of("name", "Eletronicos");
        MockHttpServletRequestBuilder request = POST("/api/categories", payload).with(
                jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")));
        // Act & Assert
        mockMvc.perform(request).andExpect(status().isCreated());
        Long categoriesCount = (Long) entityManager.createQuery("select count(c) from Category c").getSingleResult();
        assertThat(categoriesCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should create a category with a super category when all data is valid")
    public void test1() throws Exception {
        // Arange
        Category eletronicos = new Category("Eletronicos");
        categoryRepository.save(eletronicos);
        Map<String, Object> payload = Map.of("name", "Celulares", "superCategory", eletronicos.getId());
        MockHttpServletRequestBuilder request = POST("/api/categories", payload).with(
                jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")));
        // Act & Assert
        mockMvc.perform(request).andExpect(status().isCreated());
        Long categoriesCount = (Long) entityManager.createQuery("select count(c) from Category c").getSingleResult();
        assertThat(categoriesCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should not create a category with a super category that not exist")
    public void test2() throws Exception {
        // Arange
        Map<String, Object> payload = Map.of("name", "Celulares", "superCategory", 5L);
        MockHttpServletRequestBuilder request = POST("/api/categories", payload).with(
                jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")));
        // Act & Assert
        String contentAsString = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(
                        StandardCharsets.UTF_8);

        List<String> errors = mapper.readValue(contentAsString, List.class);
        // TODO: adjute the message, is repeating the field
        assertThat(errors).containsExactlyInAnyOrder("O campo superCategory The super category does not exists");
        Long categoriesCount = (Long) entityManager.createQuery("select count(c) from Category c").getSingleResult();
        assertThat(categoriesCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should not create a category with same name of other category already created")
    public void test3() throws Exception {
        // Arange
        Category eletronicos = new Category("Eletronicos");
        categoryRepository.save(eletronicos);
        Map<String, Object> payload = Map.of("name", "Eletronicos", "superCategory", eletronicos.getId());
        MockHttpServletRequestBuilder request = POST("/api/categories", payload).with(
                jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")));
        // Act & Assert
        String contentAsString = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        
        List<String> errors = mapper.readValue(contentAsString, List.class);
        assertThat(errors).containsExactlyInAnyOrder("O campo name is already registered");
        Long categoriesCount = (Long) entityManager.createQuery("select count(c) from Category c").getSingleResult();
        assertThat(categoriesCount).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should not create a category if the user is not logged-in")
    public void test4() throws Exception {
        // Arange
        Map<String, String> payload = Map.of("name", "Eletronicos");
        MockHttpServletRequestBuilder request = POST("/api/categories", payload);
        // Act & Assert
        mockMvc.perform(request).andExpect(status().isUnauthorized());
        Long categoriesCount = (Long) entityManager.createQuery("select count(c) from Category c").getSingleResult();
        assertThat(categoriesCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should not create a category if the user is logged-in but have not the permissions")
    public void test5() throws Exception {
        // Arange
        Map<String, String> payload = Map.of("name", "Eletronicos");
        MockHttpServletRequestBuilder request = POST("/api/categories", payload).with(jwt());
        // Act & Assert
        mockMvc.perform(request).andExpect(status().isForbidden());
        Long categoriesCount = (Long) entityManager.createQuery("select count(c) from Category c").getSingleResult();
        assertThat(categoriesCount).isEqualTo(0);
    }
}
