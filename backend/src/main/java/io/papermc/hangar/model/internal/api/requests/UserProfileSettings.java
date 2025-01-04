package io.papermc.hangar.model.internal.api.requests;

import java.util.Map;

public record UserProfileSettings(String tagline, Map<String, String> socials) {}
