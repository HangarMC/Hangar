package io.papermc.hangar.model.api.project.version;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A version that has been uploaded")
public record UploadedVersion(@Schema(description = "URL of the uploaded version", example = "https://hangar.papermc.io/PaperMC/Debuggery/versions/1.0.0") String url) {
}
