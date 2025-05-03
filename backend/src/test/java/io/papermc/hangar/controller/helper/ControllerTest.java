package io.papermc.hangar.controller.helper;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.model.api.auth.ApiSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.bytebuddy.utility.dispatcher.JavaDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = TestData.class)
public class ControllerTest {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected JWTVerifier jwtVerifier;

    public static GenericContainer<?> meili = new GenericContainer<>(DockerImageName.parse("getmeili/meilisearch:v1.13"))
        .withReuse(true)
        .withEnv("MEILI_MASTER_KEY", "masterKey")
        .withEnv("MEILI_ENV", "development");

    static {
        meili.setPortBindings(List.of("8800:7700"));
        meili.start();
    }

    private final Map<String, String> jwtCache = new HashMap<>();

    private String getJwt(final String apiKey) throws Exception {
        String token = this.jwtCache.get(apiKey);
        try {
            if (token != null && this.jwtVerifier.verify(token) != null) {
                return token;
            }
        } catch (final JWTVerificationException ex) {
            this.logger.info("JWT Expired...");
        }
        final String response = this.mockMvc.perform(post("/api/v1/authenticate?apiKey=" + apiKey)).andReturn().getResponse().getContentAsString();
        final ApiSession apiSession = this.objectMapper.readValue(response, ApiSession.class);
        token = apiSession.token();
        this.jwtCache.put(apiKey, token);
        return token;
    }

    protected RequestPostProcessor apiKey(final String apiKey) {
        return request -> {
            try {
                request.addHeader("Authorization", "HangarAuth " + this.getJwt(apiKey));
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            return request;
        };
    }
}
