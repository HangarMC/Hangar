package io.papermc.hangar.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BBCodeConverterTest {

    private static final Path PATH = Path.of("src/test/resources/io/papermc/hangar/utils");
    private final BBCodeConverter converter = new BBCodeConverter();

    @Test
    void testDuplicates() {
        final String result = this.converter.convertToMarkdown("[b][s][b]Test[b]Tes[/b]t[/s][/b][/b]");
        Assertions.assertEquals("**~~TestTest~~**", result);
    }

    @Test
    void testRemoved() {
        final String result = this.converter.convertToMarkdown("[list][size=4][color=red][user=kennytv]T[/size]e[font=upsidedown]st[/font][/color][/user][/list]");
        Assertions.assertEquals("Test", result);
    }

    @Test
    void testList() {
        final String result = this.converter.convertToMarkdown("[list]\n[*]Element 1\n[*]Element 2\n[/list]");
        Assertions.assertEquals("\n* Element 1\n* Element 2", result);
    }

    @Test
    void testFormatting() {
        final String result = this.converter.convertToMarkdown("[b]I am bold [i]and cursive [s]and crossed out[/s][/i][/b]");
        Assertions.assertEquals("**I am bold *and cursive ~~and crossed out~~***", result);
    }

    @Test
    void testImageAndUrl() {
        final String result = this.converter.convertToMarkdown("[img]imagelink[/img], [url=https://github.com/]click here[/url] or [url='https://github.com/']here[/url]");
        Assertions.assertEquals("![imagelink](imagelink), [click here](https://github.com/) or [here](https://github.com/)", result);
    }

    @Test
    void testCode() {
        final String result = this.converter.convertToMarkdown("[code]Codeblock![/code]");
        Assertions.assertEquals("```\nCodeblock!\n```", result);
    }

    @Test
    void testInlineCode() {
        final String result = this.converter.convertToMarkdown("My code is [icode]inline[/icode]!");
        Assertions.assertEquals("My code is `inline`!", result);
    }

    @Test
    void testAttachment() {
        final String result = this.converter.convertToMarkdown("[attach]100[/attach]");
        Assertions.assertEquals("", result);
    }

    @Test
    void testMedia() {
        final String result = this.converter.convertToMarkdown("[MEDIA=youtube]dQw4w9WgXcQ[/MEDIA]");
        Assertions.assertEquals("![YouTube](https://youtu.be/dQw4w9WgXcQ)", result);
    }

    @Test
    void testMediaUnsupportedPlatform() {
        final String result = this.converter.convertToMarkdown("[MEDIA=vimeo]163721649[/MEDIA]");
        Assertions.assertEquals("[MEDIA=vimeo]163721649[/MEDIA]", result);
    }

    @Test
    void testCodeBlocksSameLine() {
        final String result = this.converter.convertToMarkdown("""
            [code]{
            }[/code]""");
        Assertions.assertEquals("""
            ```
            {
            }
            ```""", result);
    }

    @Test
    void testCodeBlocksSameLineTextAfter() {
        final String result = this.converter.convertToMarkdown("[code]this is a newline[/code]NEWLINE");
        Assertions.assertEquals("""
            ```
            this is a newline
            ```
            NEWLINE""", result);
    }

    @Test
    void testCodeblockWhitespace() {
        final String result = this.converter.convertToMarkdown("[code]NEWLINE  [/code]NEWLINE");
        Assertions.assertEquals("```\nNEWLINE  \n```\nNEWLINE", result);
    }

    @Test
    void testCodeBlocksDiffLine() {
        Assertions.assertEquals("""
                ```

                MARKDOWN

                ```""",
            this.converter.convertToMarkdown("""
                [code]
                MARKDOWN
                [/code]"""));

        Assertions.assertEquals("""
                ```
                MARKDOWN
                ```""",
            this.converter.convertToMarkdown("[code]MARKDOWN[/code]"));

        Assertions.assertEquals("""
                ```
                Codeblock!
                ```
                ```
                Codeblock!
                ```""",
            this.converter.convertToMarkdown("[code]Codeblock![/code][code]Codeblock![/code]"));
    }

    @Test
    void testCodeBlocksDiffLineLang() {
        Assertions.assertEquals("""
                ```kt

                TEXT

                ```""",
            this.converter.convertToMarkdown("""
                [code=Kotlin]
                TEXT
                [/code]"""));

        Assertions.assertEquals("""
                ```java
                TEXT

                ```""",
            this.converter.convertToMarkdown("""
                [code=Java]TEXT
                [/code]"""));

        Assertions.assertEquals("""
                ```java
                TEXT
                ```""",
            this.converter.convertToMarkdown("[code=Java]TEXT[/code]"));
    }

    @Test
    void testEscaping() {
        Assertions.assertEquals("`hi[B]bold[/B]`", this.converter.convertToMarkdown("[icode]hi[B]bold[/B][/icode]"));
        Assertions.assertEquals("""
            ```
            hi[B]bold[/B]
            ```""", this.converter.convertToMarkdown("[code]hi[B]bold[/B][/code]"));
    }

    @Test
    void testComplexExample() throws IOException {
        // Be sure to retest/-generate this output if "breaking" changes are made, for example to spacing
        final String input = Files.readString(PATH.resolve("BBCodeExample.txt"));
        final String expected = Files.readString(PATH.resolve("BBCodeConverted.txt"));
        final String result = this.converter.convertToMarkdown(input);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void testMoveHeadingToFront() {
        final String input = "[B][SIZE=5]Dum[/SIZE][/B]";
        final String expected = "### **Dum**";
        final String result = this.converter.convertToMarkdown(input);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void testWrapBoldAroundNewline() {
        final String input = "[B]Dum\nDum[/B]";
        final String expected = "**Dum**\n**Dum**";
        final String result = this.converter.convertToMarkdown(input);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void testBoldList() {
        final String input = """
                [LIST]
                [*][B]bold[/B] not
                [*][B]bold[/B] not
                [*][B]bold[/B] not
                [/LIST]
                """;
        final String expected = """

                * **bold** not
                * **bold** not
                * **bold** not""";
        final String result = this.converter.convertToMarkdown(input);
        Assertions.assertEquals(expected, result);
    }
}
