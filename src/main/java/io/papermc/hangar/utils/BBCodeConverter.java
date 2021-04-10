package io.papermc.hangar.utils;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BBCodeConverter {

    private static final Map<String, TagReplacer> REPLACERS = new HashMap<>();
    private static final Map<String, String> SIMPLE_SINGLETON_REPLACERS = new HashMap<>();
    private static final String CLOSING_FORMAT = "[/%s]";
    private static final char TAG_PREFIX = '[';
    private static final char TAG_SUFFIX = ']';
    private static final char ARG_PREFIX = '=';

    private String currentTag;
    private String currentArg;
    private String currentContent;

    static {
        // Remove tags
        REPLACERS.put("color", (tag, tagArg, content) -> content);
        REPLACERS.put("center", (tag, tagArg, content) -> content);
        REPLACERS.put("u", (tag, tagArg, content) -> content);
        REPLACERS.put("quote", (tag, tagArg, content) -> content);
        REPLACERS.put("font", (tag, tagArg, content) -> content);
        REPLACERS.put("user", (tag, tagArg, content) -> content);
        REPLACERS.put("list", (tag, tagArg, content) -> content);
        REPLACERS.put("size", (tag, tagArg, content) -> content);

        REPLACERS.put("spoiler", (tag, tagArg, content) -> content); //TODO?
        REPLACERS.put("b", (tag, tagArg, content) -> "**" + content + "**");
        REPLACERS.put("i", (tag, tagArg, content) -> "*" + content + "*");
        REPLACERS.put("s", (tag, tagArg, content) -> "~~" + content + "~~");
        REPLACERS.put("img", (tag, tagArg, content) -> "![" + content + "](" + content + ")");
        REPLACERS.put("url", (tag, tagArg, content) -> {
            String url = tagArg;
            char firstCharacter = url.length() > 2 ? url.charAt(0) : '-';
            if ((firstCharacter == '\'' || firstCharacter == '\"') && url.charAt(url.length() - 1) == firstCharacter) {
                url = url.substring(1, url.length() - 1);
            }
            return "[" + content + "](" + url + ")";
        });
        REPLACERS.put("code", (tag, tagArg, content) -> "```" + content + "```");
        REPLACERS.put("icode", (tag, tagArg, content) -> "`" + content + "`");
        REPLACERS.put("attach", ((tag, tagArg, content) -> {
            String imageUrl = "https://www.spigotmc.org/attachments/" + content;
            return "![" + imageUrl + "](" + imageUrl + ")";
        }));

        // Unordered list entries (do not have closing tags)
        SIMPLE_SINGLETON_REPLACERS.put("*", "* ");
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
            int closingIndex = process(s, index, true);
            if (closingIndex == -1) {
                index++;
                continue;
            }

            s = s.substring(0, index) + currentContent + s.substring(closingIndex);
        }

        // Iterate until no whitespaces are left (else they might only be moved into the upper tag...)
        String result;
        while ((result = removeTrailingWhitespaces(s)) != null) {
            s = result;
        }

        // Tag conversion
        index = 0;
        while ((index = s.indexOf(TAG_PREFIX, index)) != -1) {
            int closingIndex = process(s, index, false);
            if (closingIndex == -1) {
                // No closing tag/no simple match
                index++;
                continue;
            }

            if (currentContent == null) {
                // Simple opening tag match
                String replacement = SIMPLE_SINGLETON_REPLACERS.get(currentTag);
                s = s.substring(0, index) + replacement + s.substring(closingIndex);
                continue;
            }

            TagReplacer replacer = REPLACERS.get(currentTag);
            if (replacer == null) {
                // No replacer found
                index++;
                continue;
            }

            String processed = replacer.process(currentTag, currentArg, currentContent);
            s = s.substring(0, index) + processed + s.substring(closingIndex);
        }
        return s;
    }

    @Nullable
    private String removeTrailingWhitespaces(String s) {
        int index = 0;
        boolean foundTrailingSpace = false;
        while ((index = s.indexOf(TAG_PREFIX, index)) != -1) {
            int closingIndex = process(s, index, false);
            if (closingIndex == -1 || currentContent == null) {
                index++;
                continue;
            }

            boolean startsWithSpace = currentContent.startsWith(" ");
            boolean endsWithSpace = currentContent.endsWith(" ");
            if (startsWithSpace || endsWithSpace) {
                foundTrailingSpace = true;
                currentContent = currentContent.trim();
            }

            // Readd opening and closing tag, then spaces
            int tagSuffixIndex = s.indexOf(TAG_SUFFIX, index);
            currentContent = s.substring(index, tagSuffixIndex + 1) + currentContent + s.substring(closingIndex - currentTag.length() - 3, closingIndex);
            if (startsWithSpace) {
                currentContent = " " + currentContent;
            }
            if (endsWithSpace) {
                currentContent += " ";
            }

            s = s.substring(0, index) + currentContent + s.substring(closingIndex);
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
    private int process(String s, int index, boolean deduplicate) {
        int tagSuffixIndex = s.indexOf(TAG_SUFFIX, index);
        if (tagSuffixIndex == -1 || tagSuffixIndex == index + 1) {
            // No closing bracket
            return -1;
        }

        String tagName = s.substring(index + 1, tagSuffixIndex).toLowerCase();
        String tagArg = null;
        int argIndex = tagName.indexOf(ARG_PREFIX);
        if (argIndex != -1) {
            tagArg = tagName.substring(argIndex + 1);
            tagName = tagName.substring(0, argIndex);
        }

        if (!deduplicate && SIMPLE_SINGLETON_REPLACERS.containsKey(tagName)) {
            // Simple opening tag only replacement
            currentTag = tagName;
            currentArg = null;
            currentContent = null;
            return tagSuffixIndex + 1;
        }

        String lowerCaseString = s.toLowerCase();
        String closingTag = String.format(CLOSING_FORMAT, tagName);
        int closingIndex = lowerCaseString.indexOf(closingTag, index);
        if (closingIndex == -1) {
            // No closing tag
            return -1;
        }

        currentTag = tagName;
        currentArg = tagArg;
        currentContent = s.substring(tagSuffixIndex + 1, closingIndex);
        if (deduplicate) {
            String fullTag = s.substring(index, tagSuffixIndex + 1).toLowerCase();
            String lowerCaseContent = lowerCaseString.substring(tagSuffixIndex + 1, closingIndex);
            int duplicateTagIndex = lowerCaseContent.indexOf(fullTag);
            if (duplicateTagIndex == -1) {
                // No duplicate
                return -1;
            }

            // Keep opening tag, remove duplicate opening in content, skip one closing tag
            currentContent = fullTag + currentContent.substring(0, duplicateTagIndex) + currentContent.substring(duplicateTagIndex + fullTag.length());
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
    }
}
