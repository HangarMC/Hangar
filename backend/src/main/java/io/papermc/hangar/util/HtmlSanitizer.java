package io.papermc.hangar.util;

import io.papermc.hangar.config.hangar.HangarConfig;
import java.util.List;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlStreamEventReceiverWrapper;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class HtmlSanitizer {

    private final PolicyFactory sanitizer;

    @Autowired
    public HtmlSanitizer(final HangarConfig config) {
        //TODO remove this disgusting hack when we can put the sanitizer before the markdown renderer
        final PolicyFactory links = new HtmlPolicyBuilder().withPostprocessor((HtmlStreamEventReceiver r) -> new HtmlStreamEventReceiverWrapper(r) {
            @Override
            public void openTag(final String elementName, final List<String> attrs) {
                if (!"a".equals(elementName)) {
                    super.openTag(elementName, attrs);
                    return;
                }

                for (int i = 0, n = attrs.size(); i < n; i += 2) {
                    if (!"href".equals(attrs.get(i))) {
                        continue;
                    }

                    final String url = attrs.get(i + 1);
                    final String safeUrl = config.security.makeSafe(url);
                    if (!url.equals(safeUrl)) {
                        attrs.set(i + 1, safeUrl);
                    }
                }
                super.openTag(elementName, attrs);
            }
        }).allowStandardUrlProtocols().allowElements("a").allowAttributes("href", "id", "class").onElements("a").toFactory();
        final PolicyFactory iframes = new HtmlPolicyBuilder().withPostprocessor((HtmlStreamEventReceiver r) -> new HtmlStreamEventReceiverWrapper(r) {
            @Override
            public void openTag(final String elementName, final List<String> attrs) {
                if (!"iframe".equals(elementName)) {
                    super.openTag(elementName, attrs);
                    return;
                }

                for (int i = 0, n = attrs.size(); i < n; i += 2) {
                    if ("src".equals(attrs.get(i)) && !attrs.get(i + 1).startsWith("https://www.youtube-nocookie.com/embed/")) {
                        // Only allow YouTube video embeds
                        return;
                    }
                }
                super.openTag(elementName, attrs);
            }
        }).allowUrlProtocols("https").allowElements("iframe").allowAttributes("src", "allowfullscreen", "border", "height", "width", "frameborder", "allow", "class").onElements("iframe").toFactory();

        final PolicyFactory images = new HtmlPolicyBuilder().allowStandardUrlProtocols().allowElements("img", "svg", "path")
            .allowAttributes("alt", "src", "border", "height", "width", "preserveAspectRatio", "viewBox", "class").onElements("img", "svg") // TODO don't allow class attribute
            .allowAttributes("fill", "d").onElements("path").toFactory();
        sanitizer = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS).and(Sanitizers.TABLES).and(Sanitizers.STYLES)
            .and(images).and(links).and(iframes).and(new HtmlPolicyBuilder().allowElements("pre", "details", "summary", "hr", "code").toFactory());
    }

    public String sanitize(final String input) {
        return sanitizer.sanitize(input);
    }
}
