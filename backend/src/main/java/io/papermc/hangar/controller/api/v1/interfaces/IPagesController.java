package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.project.PageEditForm;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Pages")
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IPagesController {

    @Operation(
        summary = "Returns the main page of a project",
        operationId = "getMainPage",
        description = "Returns the main page of a project. Requires visibility of the page.",
        tags = "Pages"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/pages/main/{slug}", produces = MediaType.TEXT_PLAIN_VALUE)
    String getMainPage(@Parameter(description = "The slug of the project to return the page for") @PathVariable String slug);

    @GetMapping(value = "/pages/main/{author}/{slug}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Deprecated(forRemoval = true)
    default String getMainPage(@Parameter(description = "The author of the project to return the page for") @PathVariable String author,
                       @Parameter(description = "The slug of the project to return the page for") @PathVariable String slug) {
        return this.getMainPage(slug);
    }

    @Operation(
        summary = "Returns a page of a project",
        operationId = "getPage",
        description = "Returns a page of a project. Requires visibility of the page.",
        tags = "Pages"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @GetMapping(value = "/pages/page/{slug}", produces = MediaType.TEXT_PLAIN_VALUE)
    String getPage(@Parameter(description = "The slug of the project to return the page for") @PathVariable String slug,
                   @Parameter(description = "The path of the page") @RequestParam String path);

    @GetMapping(value = "/pages/page/{author}/{slug}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Deprecated(forRemoval = true)
    default String getPage(@Parameter(description = "The author of the project to return the page for") @PathVariable String author,
                   @Parameter(description = "The slug of the project to return the page for") @PathVariable String slug,
                   @Parameter(description = "The path of the page") @RequestParam String path) {
        return this.getPage(slug, path);
    }

    @Operation(
        summary = "Edits the main page of a project",
        operationId = "editMainPage",
        description = "Edits the main page of a project. Requires the `edit_page` permission in the project or owning organization.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "edit_page"),
        tags = "Pages"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @PatchMapping(path = "/pages/editmain/{slug}", consumes = MediaType.APPLICATION_JSON_VALUE)
    void editMainPage(@Parameter(description = "The slug of the project to change the page for") @PathVariable String slug,
                      @Parameter(description = "The path and new contents of the page") @RequestBody StringContent pageEditForm);

    @PatchMapping(path = "/pages/editmain/{author}/{slug}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated(forRemoval = true)
    default void editMainPage(@Parameter(description = "The author of the project to change the page for") @PathVariable String author,
                      @Parameter(description = "The slug of the project to change the page for") @PathVariable String slug,
                      @Parameter(description = "The path and new contents of the page") @RequestBody StringContent pageEditForm) {
        this.editMainPage(slug, pageEditForm);
    }

    @Operation(
        summary = "Edits a page of a project",
        operationId = "editPage",
        description = "Edits a page of a project. Requires the `edit_page` permission in the project or owning organization.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "edit_page"),
        tags = "Pages"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @PatchMapping(path = "/pages/edit/{slug}", consumes = MediaType.APPLICATION_JSON_VALUE)
    void editPage(@Parameter(description = "The slug of the project to change the page for") @PathVariable String slug,
                  @Parameter(description = "The path and new contents of the page") @RequestBody PageEditForm pageEditForm);

    @PatchMapping(path = "/pages/edit/{author}/{slug}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated(forRemoval = true)
    default void editPage(@Parameter(description = "The author of the project to change the page for") @PathVariable String author,
                  @Parameter(description = "The slug of the project to change the page for") @PathVariable String slug,
                  @Parameter(description = "The path and new contents of the page") @RequestBody PageEditForm pageEditForm) {
        this.editPage(slug, pageEditForm);
    }
}
