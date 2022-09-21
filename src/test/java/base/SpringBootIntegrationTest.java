package base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SpringBootIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper mapper;
    @Autowired
    protected EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager transactionManager;

    protected void executeQueryInTransaction(String queryString) {
        this.doInTransaction(() -> {
            entityManager.createQuery(queryString).executeUpdate();
        });
    }

    @Transactional
    protected void saveAEntity(Object entity) {
        this.doInTransaction(() -> {
            entityManager.persist(entity);
        });
    }

    public void doInTransaction(Runnable runnable) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }

    public MockHttpServletRequestBuilder GET(String uri) {
        return get(uri)
                .header("Accept-Language", "pt-br")
                .contentType(APPLICATION_JSON);
    }

    public MockHttpServletRequestBuilder GET(String uri, Object...uriVars) {
        return get(uri, uriVars)
                .header("Accept-Language", "pt-br")
                .contentType(APPLICATION_JSON);
    }

    public MockHttpServletRequestBuilder POST(String uri) {
        return post(uri)
                .header("Accept-Language", "pt-br")
                .contentType(APPLICATION_JSON);
    }


    public MockHttpServletRequestBuilder POST(String uri, Object payload) throws JsonProcessingException {
        String json = mapper.writeValueAsString(payload);
        return post(uri)
                .header("Accept-Language", "pt-br")
                .contentType(APPLICATION_JSON)
                .content(json);
    }
}
