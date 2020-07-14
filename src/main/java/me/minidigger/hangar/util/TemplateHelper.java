package me.minidigger.hangar.util;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TemplateHelper {

    public Map<String, String> randomSponsor() {
        // TODO implement random sponsor stuff
        return Map.of("link", "https://minidigger.me", "image", "https://avatars2.githubusercontent.com/u/2185527?s=400&v=4");
    }
}
