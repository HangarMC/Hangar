package io.papermc.hangar.service.internal;

import com.vladsch.flexmark.ast.MailLink;
import com.vladsch.flexmark.ext.admonition.AdmonitionExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.emoji.EmojiImageType;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.gitlab.GitLabExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.LinkResolver;
import com.vladsch.flexmark.html.LinkResolverFactory;
import com.vladsch.flexmark.html.renderer.LinkResolverBasicContext;
import com.vladsch.flexmark.html.renderer.LinkStatus;
import com.vladsch.flexmark.html.renderer.LinkType;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import io.papermc.hangar.config.hangar.HangarConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

@Service
public class MarkdownService {

    private final Parser markdownParser;
    private final MutableDataSet options;
    private final HangarConfig config;

    @Autowired
    public MarkdownService(HangarConfig config) {
        this.config = config;

        options = new MutableDataSet()
                .set(HtmlRenderer.ESCAPE_HTML, true)
                .set(AnchorLinkExtension.ANCHORLINKS_TEXT_PREFIX, "<i class='v-icon notranslate mdi mdi-link-variant' aria-hidden='true'></i>")
                .set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "headeranchor")
                .set(AnchorLinkExtension.ANCHORLINKS_WRAP_TEXT, false)
                // GFM table compatibility
                .set(TablesExtension.COLUMN_SPANS, false)
                .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
                .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
                .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
                // extensions
                .set(EmojiExtension.USE_IMAGE_TYPE, EmojiImageType.UNICODE_ONLY)
                .set(
                        Parser.EXTENSIONS,
                        Arrays.asList(
                                AutolinkExtension.create(),
                                AnchorLinkExtension.create(),
                                StrikethroughExtension.create(),
                                TaskListExtension.create(),
                                TablesExtension.create(),
                                TypographicExtension.create(),
                                WikiLinkExtension.create(),
                                EmojiExtension.create(),
                                FootnoteExtension.create(),
                                AdmonitionExtension.create(),
                                GitLabExtension.create()
                        )
                );

        markdownParser = Parser.builder(options).build();
    }

    public String render(String input) {
        if (input == null) return "";
        return this.render(input, RenderSettings.defaultSettings);
    }

    public String render(String input, RenderSettings settings) {
        MutableDataSet localOptions = new MutableDataSet(this.options);

        if (settings.linkEscapeChars != null) {
            localOptions.set(WikiLinkExtension.LINK_ESCAPE_CHARS, settings.linkEscapeChars);
        }
        if (settings.linkPrefix != null) {
            localOptions.set(WikiLinkExtension.LINK_PREFIX, settings.linkPrefix);
        }

        HtmlRenderer htmlRenderer = HtmlRenderer
                .builder(localOptions)
                .linkResolverFactory(new ExternalLinkResolverFactory(config))
                .build();

        return htmlRenderer.render(markdownParser.parse(input));
    }

    static class RenderSettings {
        private final String linkEscapeChars;
        private final String linkPrefix;

        public static final RenderSettings defaultSettings = new RenderSettings(null, null);

        public RenderSettings(String linkEscapeChars, String linkPrefix) {
            this.linkEscapeChars = linkEscapeChars;
            this.linkPrefix = linkPrefix;
        }
    }

    static class ExternalLinkResolverFactory implements LinkResolverFactory {

        private final HangarConfig config;

        ExternalLinkResolverFactory(HangarConfig config) {
            this.config = config;
        }

        @Override
        public @Nullable Set<Class<?>> getAfterDependents() {
            return null;
        }

        @Override
        public @Nullable Set<Class<?>> getBeforeDependents() {
            return null;
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @Override
        public @NotNull LinkResolver apply(@NotNull LinkResolverBasicContext linkResolverBasicContext) {
            return new ExternalLinkResolver(config);
        }
    }

    static class ExternalLinkResolver implements LinkResolver {

        private final HangarConfig config;

        ExternalLinkResolver(HangarConfig config) {
            this.config = config;
        }

        @Override
        public @NotNull ResolvedLink resolveLink(@NotNull Node node, @NotNull LinkResolverBasicContext context, @NotNull ResolvedLink link) {
            if (link.getLinkType() == LinkType.IMAGE || node instanceof MailLink) {
                return link;
            } else {
                return link.withStatus(LinkStatus.VALID).withUrl(config.security.makeSafe(link.getUrl()));
            }
        }
    }
}
