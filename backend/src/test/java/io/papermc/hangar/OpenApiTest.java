package io.papermc.hangar;

import io.papermc.hangar.controller.helper.ControllerTest;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.validation.ValidationResult;
import org.openapitools.codegen.validations.oas.OpenApiEvaluator;
import org.openapitools.codegen.validations.oas.RuleConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenApiTest extends ControllerTest {

    @Test
    void testPublicOpenApi() throws Exception {
        String response = this.mockMvc.perform(get("/v3/api-docs/public"))
            .andExpect(status().is(200))
            .andReturn().getResponse().getContentAsString();

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        SwaggerParseResult result = new OpenAPIParser().readContents(response, null, options);
        OpenAPI specification = result.getOpenAPI();

        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setEnableRecommendations(true);
        OpenApiEvaluator evaluator = new OpenApiEvaluator(ruleConfiguration);
        ValidationResult validationResult = evaluator.validate(specification);

        // check validation errors
        assertThat(result.getMessages()).isEmpty();
        assertThat(validationResult.getErrors()).isEmpty();
        assertThat(validationResult.getWarnings()).isEmpty();

        // check that sorters and filters got added
        PathItem projects = specification.getPaths().get("/api/v1/projects");
        assertThat(projects).isNotNull();
        List<Parameter> parameters = projects.getGet().getParameters();
        assertThat(parameters).isNotNull();
        assertThat(parameters.stream().anyMatch(p -> "sort".equals(p.getName()))).isTrue();
        assertThat(parameters.stream().anyMatch(p -> "category".equals(p.getName()))).isTrue();
        assertThat(parameters.stream().anyMatch(p -> "owner".equals(p.getName()))).isTrue();
        assertThat(parameters.stream().anyMatch(p -> "limit".equals(p.getName()))).isTrue();
        assertThat(parameters.stream().anyMatch(p -> "offset".equals(p.getName()))).isTrue();
    }
}
