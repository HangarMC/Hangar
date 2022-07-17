package io.papermc.hangar.util;

import org.owasp.html.AttributePolicy;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public final class HtmlSanitizerUtil {

    private static final PolicyFactory IMAGES = new HtmlPolicyBuilder().allowUrlProtocols("https").allowElements("img")
        .allowAttributes("alt", "src").onElements("img").allowAttributes("border", "height", "width")
        .matching(integerPolicy()).onElements("img").toFactory();
    public static final PolicyFactory SANITIZER = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS).and(IMAGES).and(Sanitizers.TABLES).and(Sanitizers.STYLES)
        .and(new HtmlPolicyBuilder().allowElements("details", "summary").toFactory());

    private static AttributePolicy integerPolicy() {
        return (elementName, attributeName, value) -> {
            int n = value.length();
            if (n == 0) {
                return null;
            }
            for (int i = 0; i < n; ++i) {
                char ch = value.charAt(i);
                if (ch == '.') {
                    return i == 0 ? null : value.substring(0, i);
                } else if ('0' > ch || ch > '9') {
                    return null;
                }
            }
            return value;
        };
    }
}
