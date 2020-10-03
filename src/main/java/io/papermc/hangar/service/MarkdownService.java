package io.papermc.hangar.service;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class MarkdownService {

    private final Parser markdownParser;
    private final MutableDataSet options;

    public MarkdownService() {
        options = new MutableDataSet()
                .set(HtmlRenderer.SUPPRESS_HTML, true)
                .set(AnchorLinkExtension.ANCHORLINKS_TEXT_PREFIX, "<i class=\"fas fa-link\"></i>")
                .set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "headeranchor")
                .set(AnchorLinkExtension.ANCHORLINKS_WRAP_TEXT, false)
                // GFM table compatibility
                .set(TablesExtension.COLUMN_SPANS, false)
                .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
                .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
                .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
                .set(
                        Parser.EXTENSIONS,
                        Arrays.asList(
                                AutolinkExtension.create(),
                                AnchorLinkExtension.create(),
                                StrikethroughExtension.create(),
                                TaskListExtension.create(),
                                TablesExtension.create(),
                                TypographicExtension.create(),
                                WikiLinkExtension.create()
                        )
                );

        markdownParser = Parser.builder(options).build();
    }

    public String render(String input) {
        if (input == null) return "";
        return this.render(input, RenderSettings.defaultSettings);
    }

    public String render(String input, RenderSettings settings) {
        MutableDataSet options = new MutableDataSet(this.options);

        if (settings.linkEscapeChars != null) {
            options.set(WikiLinkExtension.LINK_ESCAPE_CHARS, settings.linkEscapeChars);
        }
        if (settings.linkPrefix != null) {
            options.set(WikiLinkExtension.LINK_PREFIX, settings.linkPrefix);
        }

        HtmlRenderer htmlRenderer = HtmlRenderer
                .builder(options)
//                .linkResolverFactory(new ExternalLinkResolver())
                .build();

        return htmlRenderer.render(markdownParser.parse(input));
    }

    static class RenderSettings {
        private String linkEscapeChars;
        private String linkPrefix;

        public static final RenderSettings defaultSettings = new RenderSettings(null, null);

        public RenderSettings(String linkEscapeChars, String linkPrefix) {
            this.linkEscapeChars = linkEscapeChars;
            this.linkPrefix = linkPrefix;
        }
    }

    // TODO external links for markdown shit
//    static class ExternalLinkResolver implements LinkResolver {
//
//        @Override
//        public @NotNull ResolvedLink resolveLink(@NotNull Node node, @NotNull LinkResolverBasicContext context, @NotNull ResolvedLink link) {
//            if (link.getLinkType() == LinkType.IMAGE || node instanceof MailLink) {
//                return link;
//            } else {
//                return link.withStatus(LinkStatus.VALID).withUrl(wrapExternal(link.getUrl()));
//            }
//        }
//
//        private String wrapExternal(String urlString) {
//            try {
//                URI uri  = new URI(urlString);
//                String host = uri.getHost();
//                if (uri.getScheme() != null && host == null) {
//                    if (uri.getScheme().equals("mailto")) {
//                        return urlString;
//                    } else {
//                        return controllers.routes.Application.linkOut(urlString).toString
//                    }
//                } else {
//                    String trustedUrlHosts = this.config.app.trustedUrlHosts;
//                    val checkSubdomain = (trusted: String) =>
//                    trusted(0) == '.' && (host.endsWith(trusted) || host == trusted.substring(1))
//                    if (host == null || trustedUrlHosts.exists(trusted => trusted == host || checkSubdomain(trusted))) { // scalafix:ok
//                       return urlString
//                    } else {
//                       return controllers.routes.Application.linkOut(urlString).toString
//                    }
//                }
//            } catch (URISyntaxException ex) {
//               return controllers.routes.Application.linkOut(urlString).toString
//            }
//        }
//    }
}
