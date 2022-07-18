package io.papermc.hangar.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public final class HtmlSanitizerUtil {

    private static final PolicyFactory IMAGES = new HtmlPolicyBuilder().allowUrlProtocols("https").allowElements("img")
        .allowAttributes("alt", "src").onElements("img").allowAttributes("border", "height", "width").onElements("img").toFactory();
    private static final PolicyFactory SANITIZER = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS).and(IMAGES).and(Sanitizers.TABLES).and(Sanitizers.STYLES)
        .and(new HtmlPolicyBuilder().allowElements("pre", "details", "summary").toFactory());

    public static String sanitize(final String input) {
        return SANITIZER.sanitize(input);
    }
}
