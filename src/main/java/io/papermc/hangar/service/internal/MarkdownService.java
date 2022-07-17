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
import com.vladsch.flexmark.ext.resizable.image.ResizableImageExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.ext.youtube.embedded.YouTubeLinkExtension;
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
import io.papermc.hangar.util.HtmlSanitizerUtil;
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
            .set(AnchorLinkExtension.ANCHORLINKS_TEXT_SUFFIX, "<svg class=\"ml-2 text-xl\" preserveAspectRatio=\"xMidYMid meet\" viewBox=\"0 0 24 24\" width=\"1.2em\" height=\"1.2em\"><path fill=\"currentColor\" d=\"M10.59 13.41c.41.39.41 1.03 0 1.42c-.39.39-1.03.39-1.42 0a5.003 5.003 0 0 1 0-7.07l3.54-3.54a5.003 5.003 0 0 1 7.07 0a5.003 5.003 0 0 1 0 7.07l-1.49 1.49c.01-.82-.12-1.64-.4-2.42l.47-.48a2.982 2.982 0 0 0 0-4.24a2.982 2.982 0 0 0-4.24 0l-3.53 3.53a2.982 2.982 0 0 0 0 4.24m2.82-4.24c.39-.39 1.03-.39 1.42 0a5.003 5.003 0 0 1 0 7.07l-3.54 3.54a5.003 5.003 0 0 1-7.07 0a5.003 5.003 0 0 1 0-7.07l1.49-1.49c-.01.82.12 1.64.4 2.43l-.47.47a2.982 2.982 0 0 0 0 4.24a2.982 2.982 0 0 0 4.24 0l3.53-3.53a2.982 2.982 0 0 0 0-4.24a.973.973 0 0 1 0-1.42Z\"></path></svg>")
            .set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "headeranchor inline-flex items-center order-last")
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
                    GitLabExtension.create(),
                    YouTubeLinkExtension.create(),
                    ResizableImageExtension.create()
                )
            );

        markdownParser = Parser.builder(options).build();
    }

    public String render(String input) {
        if (input == null) {
            return "";
        }
        return this.render(input, RenderSettings.defaultSettings);
    }

    public String render(String input, RenderSettings settings) {
        input = HtmlSanitizerUtil.SANITIZER.sanitize(input);
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
