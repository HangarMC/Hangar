package io.papermc.hangar.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class BBCodeConverter {

    private static final Map<String, TagReplacer> REPLACERS = new HashMap<>();
    private static final Map<String, String> SIMPLE_SINGLETON_REPLACERS = new HashMap<>();
    private static final String CLOSING_FORMAT = "[/%s]";
    private static final char TAG_PREFIX = '[';
    private static final char TAG_SUFFIX = ']';
    private static final char ARG_PREFIX = '=';

    // https://regex101.com/r/MWDUfk/1
    private static final Pattern headlinePattern = Pattern.compile("([^#]+)(#+ )(.+)");
    // https://regex101.com/r/s4Nxgc/1
    private static final Pattern boldNewlinePattern = Pattern.compile("(\\*\\*)(.*?)(\\n)?(.*?)(\\*\\*)");

    private String currentTag;
    private String currentArg;
    private String currentContent;

    static {
        final TagReplacer removeTag = (tag, tagArg, content) -> content;
        REPLACERS.put("color", removeTag);
        REPLACERS.put("left", removeTag);
        REPLACERS.put("right", removeTag);
        REPLACERS.put("u", removeTag);
        REPLACERS.put("quote", removeTag);
        REPLACERS.put("font", removeTag);
        REPLACERS.put("user", removeTag);
        REPLACERS.put("list", removeTag);
        REPLACERS.put("attach", (tag, tagArg, content) -> "");

        REPLACERS.put("b", encaseIfNotBlank("**", "**"));
        REPLACERS.put("i", encaseIfNotBlank("*", "*"));
        REPLACERS.put("s", encaseIfNotBlank("~~", "~~"));
        REPLACERS.put("center", encaseIfNotBlank("<center>", "</center>"));
        REPLACERS.put("spoiler", (tag, tagArg, content) -> "<details>\n<summary>%s</summary>\n\n%s\n</details>\n".formatted(removeQuotes(tagArg), content));
        REPLACERS.put("img", (tag, tagArg, content) -> "![" + content + "](" + content + ")");
        REPLACERS.put("media", (tag, tagArg, content) -> "youtube".equals(tagArg) ? "![YouTube](https://youtu.be/" + content + ")" : null);
        REPLACERS.put("size", (tag, tagArg, content) -> {
            if (content.isBlank() || tagArg == null) {
                return content;
            }

            int size;
            try {
                size = Integer.parseInt(tagArg);
                if (size == 4) { // 4 is the default size
                    return content;
                }
            } catch (final NumberFormatException ignored) {
                return content;
            }

            // Clamp the size to what's allowed and start from 2 as we only have 6 header levels in markdown
            size = Math.min(7, size);
            size = Math.max(2, size);

            final int headerLevel = 8 - size;
            return "#".repeat(headerLevel) + " " + content;
        });
        REPLACERS.put("heading", (tag, tagArg, content) -> {
            if (content.isBlank()) {
                return content;
            }

            final int heading;
            try {
                heading = Integer.parseInt(tagArg);
            } catch (final NumberFormatException ignored) {
                return content;
            }
            if (heading < 1 || heading > 3) {
                return content;
            }
            return "#".repeat(heading) + " " + content;
        });
        REPLACERS.put("url", (tag, tagArg, content) -> {
            String url = tagArg == null ? content : tagArg;
            final char firstCharacter = url.length() > 2 ? url.charAt(0) : '-';
            if ((firstCharacter == '\'' || firstCharacter == '\"') && url.charAt(url.length() - 1) == firstCharacter) {
                url = url.substring(1, url.length() - 1);
            }
            return "[" + content + "](" + url + ")";
        });
        REPLACERS.put("code", new TagReplacer() {
            @Override
            public String process(final String tag, final String tagArg, final String content) {
                final String lang;
                if (tagArg == null) {
                    lang = "";
                } else if (tagArg.equals("kotlin")) {
                    lang = "kt";
                } else {
                    lang = tagArg;
                }

                return "```" + lang + "\n"
                    + content
                    + "\n```";
            }

            @Override
            public boolean hasRawContents() {
                return true;
            }

            @Override
            public boolean appendNewline() {
                return true;
            }
        });
        REPLACERS.put("icode", new TagReplacer() {
            @Override
            public String process(final String tag, final String tagArg, final String content) {
                return "`" + content + "`";
            }

            @Override
            public boolean hasRawContents() {
                return true;
            }
        });

        // Unordered list entries (do not have closing tags)
        SIMPLE_SINGLETON_REPLACERS.put("*", "* ");
    }

    private static TagReplacer encaseIfNotBlank(final String prefix, final String suffix) {
        return (tag, tagArg, content) -> content.isBlank() ? content : prefix + content + suffix;
    }

    /**
     * Converts the given BBcode input to Markdown.
     *
     * @param s string
     * @return converted text to Markdown formatting
     */
    public String convertToMarkdown(String s) {
        // Deduplication, remove spaces in tags
        int index = 0;
        while ((index = s.indexOf(TAG_PREFIX, index)) != -1) {
            final int closingIndex = this.process(s, index, true);
            if (closingIndex == -1) {
                index++;
                continue;
            }

            s = s.substring(0, index) + this.currentContent + s.substring(closingIndex);
        }

        // Iterate until no whitespaces are left (else they might only be moved into the upper tag...)
        String result;
        while ((result = this.removeTrailingWhitespaces(s)) != null) {
            s = result;
        }

        // Tag conversion
        index = 0;
        while ((index = s.indexOf(TAG_PREFIX, index)) != -1) {
            final int closingIndex = this.process(s, index, false);
            if (closingIndex == -1) {
                // No closing tag/no simple match
                index++;
                continue;
            }

            if (this.currentContent == null) {
                // Simple opening tag match
                final String replacement = SIMPLE_SINGLETON_REPLACERS.get(this.currentTag);
                s = s.substring(0, index) + replacement + s.substring(closingIndex);
                continue;
            }

            final TagReplacer replacer = REPLACERS.get(this.currentTag);
            if (replacer == null) {
                // No replacer found
                index++;
                continue;
            }

            String processed = replacer.process(this.currentTag, this.currentArg, this.currentContent);
            if (processed == null) {
                index++;
            } else {
                if (replacer.appendNewline()) {
                    processed += "\n";
                }

                s = s.substring(0, index) + processed + s.substring(closingIndex);
                if (replacer.hasRawContents()) {
                    index += processed.length();
                }
            }
        }

        // Removes newlines from the end of the last tag adds newlines
        final TagReplacer replacer = REPLACERS.get(this.currentTag);
        if (replacer != null && replacer.appendNewline()) {
            final int lastChar = s.length() - 1;
            if (s.lastIndexOf('\n') == lastChar) {
                return s.substring(0, lastChar);
            }
        }

        return cleanup(s);
    }

    private @Nullable String removeTrailingWhitespaces(String s) {
        int index = 0;
        boolean foundTrailingSpace = false;
        while ((index = s.indexOf(TAG_PREFIX, index)) != -1) {
            final int closingIndex = this.process(s, index, false);
            if (closingIndex == -1 || this.currentContent == null) {
                index++;
                continue;
            }
            final TagReplacer replacer = REPLACERS.get(this.currentTag);
            if (replacer != null && replacer.hasRawContents()) {
                index++;
                continue;
            }

            final boolean startsWithSpace = this.currentContent.startsWith(" ");
            final boolean endsWithSpace = this.currentContent.endsWith(" ");
            if (startsWithSpace || endsWithSpace) {
                foundTrailingSpace = true;
                this.currentContent = this.currentContent.trim();
            }

            // Readd opening and closing tag, then spaces
            final int tagSuffixIndex = s.indexOf(TAG_SUFFIX, index);
            this.currentContent = s.substring(index, tagSuffixIndex + 1) + this.currentContent + s.substring(closingIndex - this.currentTag.length() - 3, closingIndex);
            if (startsWithSpace) {
                this.currentContent = " " + this.currentContent;
            }
            if (endsWithSpace) {
                this.currentContent += " ";
            }

            s = s.substring(0, index) + this.currentContent + s.substring(closingIndex);
            index++;
        }
        return foundTrailingSpace ? s : null;
    }

    /**
     * @param s           string
     * @param index       index to start searching from
     * @param deduplicate whether tags should be deduplicated, false for normal conversion
     * @return index after the currently processed tag is closed, or -1 if none
     */
    private int process(final String s, final int index, final boolean deduplicate) {
        final int tagSuffixIndex = s.indexOf(TAG_SUFFIX, index);
        if (tagSuffixIndex == -1 || tagSuffixIndex == index + 1) {
            // No closing bracket
            return -1;
        }

        String tagName = s.substring(index + 1, tagSuffixIndex).toLowerCase();
        String tagArg = null;
        final int argIndex = tagName.indexOf(ARG_PREFIX);
        if (argIndex != -1) {
            tagArg = tagName.substring(argIndex + 1);
            tagName = tagName.substring(0, argIndex);
        }

        if (!deduplicate && SIMPLE_SINGLETON_REPLACERS.containsKey(tagName)) {
            // Simple opening tag only replacement
            this.currentTag = tagName;
            this.currentArg = null;
            this.currentContent = null;
            return tagSuffixIndex + 1;
        }

        final String lowerCaseString = s.toLowerCase();
        final String closingTag = String.format(CLOSING_FORMAT, tagName);
        final int closingIndex = lowerCaseString.indexOf(closingTag, index);
        if (closingIndex == -1) {
            // No closing tag
            return -1;
        }

        this.currentTag = tagName;
        this.currentArg = tagArg;
        this.currentContent = s.substring(tagSuffixIndex + 1, closingIndex);
        if (deduplicate) {
            final String fullTag = s.substring(index, tagSuffixIndex + 1).toLowerCase();
            final String lowerCaseContent = lowerCaseString.substring(tagSuffixIndex + 1, closingIndex);
            final int duplicateTagIndex = lowerCaseContent.indexOf(fullTag);
            if (duplicateTagIndex == -1) {
                // No duplicate
                return -1;
            }

            // Keep opening tag, remove duplicate opening in content, skip one closing tag
            this.currentContent = fullTag + this.currentContent.substring(0, duplicateTagIndex) + this.currentContent.substring(duplicateTagIndex + fullTag.length());
        }

        return closingIndex + closingTag.length();
    }

    @FunctionalInterface
    public interface TagReplacer {

        /**
         * @param tag     tag name inside of the square brackets
         * @param tagArg  arg if present, else null
         * @param content content between opening and closing tag
         */
        String process(String tag, String tagArg, String content);

        /**
         * @return if the contents should be converted from bbcode as well
         */
        default boolean hasRawContents() {
            return false;
        }

        /**
         * @return if a newline should be appended to the end of the converted bbcode
         */
        default boolean appendNewline() {
            return false;
        }
    }

    private static String removeQuotes(final String s) {
        if (s == null) {
            return "";
        }
        if (s.length() > 2) {
            final char c = s.charAt(0);
            if ((c == '\'' || c == '"') && c == s.charAt(s.length() - 1)) {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }

    private static String cleanup( String input) {
        final String[] lines = input.split("\n");
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            // move headlines to the start
            if (!line.startsWith("#") && line.contains("#")) {
                lines[i] = headlinePattern.matcher(line).replaceAll("$2$1$3");
            }
        }
        input = String.join("\n", lines);

        // make bold wrap around newlines correctly
        input = boldNewlinePattern.matcher(input).replaceAll((matchResult) -> {
            // $1$2$1$3$5$4$5
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotBlank(matchResult.group(2))) {
                sb.append(matchResult.group(1)).append(matchResult.group(2)).append(matchResult.group(1));
            }
            if (matchResult.group(3) != null) {
                sb.append(matchResult.group(3));
            }
            if (StringUtils.isNotBlank(matchResult.group(4))) {
                sb.append(matchResult.group(5)).append(matchResult.group(4)).append(matchResult.group(5));
            }
            return sb.toString();
        });

        return input;
    }
}
