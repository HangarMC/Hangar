package io.papermc.hangar.controller.internal.projects;

import com.fasterxml.jackson.core.type.TypeReference;
import io.papermc.hangar.controller.helper.ControllerTest;
import io.papermc.hangar.controller.helper.TestData;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.model.api.project.settings.Link;
import io.papermc.hangar.model.api.project.settings.LinkSection;
import io.papermc.hangar.model.api.project.settings.Tag;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.db.projects.ProjectTable;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectControllerTest extends ControllerTest {

    private static final String LEGACY_INVALID_KEYWORD = "keyword-longer-than-the-current-limit";
    private static final List<LinkSection> ORIGINAL_LINKS = List.of(
        new LinkSection(1, "top", null, List.of(new Link(1, "Docs", "https://docs.example.com")))
    );

    @Autowired
    private ProjectsDAO projectsDAO;

    private ProjectTable originalProject;

    @BeforeEach
    void prepareLegacyProject() {
        this.originalProject = this.projectsDAO.getById(TestData.PROJECT.getProjectId());
        final ProjectTable project = this.projectsDAO.getById(TestData.PROJECT.getProjectId());
        project.setCategory(Category.ADMIN_TOOLS);
        project.setDescription("Original description");
        project.setTags(List.of(Tag.ADDON));
        project.setKeywords(List.of(LEGACY_INVALID_KEYWORD));
        project.setLinks(new JSONB(ORIGINAL_LINKS));
        project.setLicenseType("MIT");
        project.setLicenseName("MIT License");
        project.setLicenseUrl("https://opensource.org/license/mit");
        project.setDonationEnabled(false);
        project.setDonationSubject("Original donation subject");
        this.projectsDAO.update(project);
    }

    @AfterEach
    void restoreProject() {
        this.projectsDAO.update(this.originalProject);
    }

    @Test
    void savesLinksWithoutValidatingOrChangingGeneralSettings() throws Exception {
        final List<LinkSection> links = List.of(
            new LinkSection(2, "top", null, List.of(new Link(2, "Source", "https://github.com/PaperMC/Hangar"))),
            new LinkSection(3, "sidebar", "Community", List.of(new Link(3, "Discord", "https://discord.gg/papermc")))
        );

        this.saveLinks(links).andExpect(status().isOk());

        final ProjectTable savedProject = this.projectsDAO.getById(TestData.PROJECT.getProjectId());
        assertThat(this.getLinks(savedProject)).isEqualTo(links);
        this.assertGeneralSettingsUnchanged(savedProject);
    }

    @Test
    void rejectsDuplicateTopSectionsWithoutUpdatingLinks() throws Exception {
        final List<LinkSection> links = List.of(
            new LinkSection(2, "top", null, List.of()),
            new LinkSection(3, "top", null, List.of())
        );

        this.saveLinks(links).andExpect(status().isBadRequest());

        this.assertLinksWereNotUpdated();
    }

    @Test
    void rejectsSidebarSectionWithoutTitleWithoutUpdatingLinks() throws Exception {
        final List<LinkSection> links = List.of(new LinkSection(2, "sidebar", null, List.of()));

        this.saveLinks(links).andExpect(status().isBadRequest());

        this.assertLinksWereNotUpdated();
    }

    @Test
    void rejectsSectionWithTooManyLinksWithoutUpdatingLinks() throws Exception {
        final List<Link> links = List.of(
            new Link(1, "One", "https://example.com/1"),
            new Link(2, "Two", "https://example.com/2"),
            new Link(3, "Three", "https://example.com/3"),
            new Link(4, "Four", "https://example.com/4"),
            new Link(5, "Five", "https://example.com/5"),
            new Link(6, "Six", "https://example.com/6")
        );

        this.saveLinks(List.of(new LinkSection(2, "top", null, links))).andExpect(status().isBadRequest());

        this.assertLinksWereNotUpdated();
    }

    @Test
    void generalSettingsEndpointStillValidatesKeywords() throws Exception {
        final Map<String, Object> settings = Map.of(
            "settings", Map.of(
                "links", ORIGINAL_LINKS,
                "tags", List.of(Tag.ADDON),
                "license", Map.of("type", "MIT", "name", "MIT License", "url", "https://opensource.org/license/mit"),
                "keywords", List.of(LEGACY_INVALID_KEYWORD)
            ),
            "category", Category.ADMIN_TOOLS,
            "description", "Changed description"
        );

        this.mockMvc.perform(post("/api/internal/projects/project/{project}/settings", TestData.PROJECT.getId())
                .with(this.apiKey(TestData.KEY_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsBytes(settings)))
            .andExpect(status().isBadRequest());

        final ProjectTable savedProject = this.projectsDAO.getById(TestData.PROJECT.getProjectId());
        assertThat(savedProject.getDescription()).isEqualTo("Original description");
        assertThat(this.getLinks(savedProject)).isEqualTo(ORIGINAL_LINKS);
    }

    private ResultActions saveLinks(final List<LinkSection> links) throws Exception {
        return this.mockMvc.perform(post("/api/internal/projects/project/{project}/links", TestData.PROJECT.getId())
            .with(this.apiKey(TestData.KEY_ADMIN))
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsBytes(Map.of("links", links))));
    }

    private void assertLinksWereNotUpdated() {
        final ProjectTable savedProject = this.projectsDAO.getById(TestData.PROJECT.getProjectId());
        assertThat(this.getLinks(savedProject)).isEqualTo(ORIGINAL_LINKS);
        this.assertGeneralSettingsUnchanged(savedProject);
    }

    private void assertGeneralSettingsUnchanged(final ProjectTable project) {
        assertThat(project.getCategory()).isEqualTo(Category.ADMIN_TOOLS);
        assertThat(project.getDescription()).isEqualTo("Original description");
        assertThat(project.getTags()).containsExactly(Tag.ADDON);
        assertThat(project.getKeywords()).containsExactly(LEGACY_INVALID_KEYWORD);
        assertThat(project.getLicenseType()).isEqualTo("MIT");
        assertThat(project.getLicenseName()).isEqualTo("MIT License");
        assertThat(project.getLicenseUrl()).isEqualTo("https://opensource.org/license/mit");
        assertThat(project.isDonationEnabled()).isFalse();
        assertThat(project.getDonationSubject()).isEqualTo("Original donation subject");
    }

    private List<LinkSection> getLinks(final ProjectTable project) {
        return project.getLinks().get(new TypeReference<>() {
        });
    }
}
