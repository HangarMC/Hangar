package io.papermc.hangar.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class BBCodeConverterTest {

    private static final Path PATH = Path.of("src/test/resources/io/papermc/hangar/utils");
    private final BBCodeConverter converter = new BBCodeConverter();

    @Test
    void testDuplicates() {
        String result = converter.convertToMarkdown("[b][s][b]Test[b]Tes[/b]t[/s][/b][/b]");
        Assertions.assertEquals("**~~TestTest~~**", result);
    }

    @Test
    void testRemoved() {
        String result = converter.convertToMarkdown("[list][size=4][color=red][user=kennytv]T[/size][center]e[font=upsidedown][/center]st[/font][/color][/user][/list]");
        Assertions.assertEquals("Test", result);
    }

    @Test
    void testList() {
        String result = converter.convertToMarkdown("[list]\n[*]Element 1\n[*]Element 2\n[/list]");
        Assertions.assertEquals("\n* Element 1\n* Element 2\n", result);
    }

    @Test
    void testFormatting() {
        String result = converter.convertToMarkdown("[b]I am bold [i]and cursive [s]and crossed out[/s][/i][/b]");
        Assertions.assertEquals("**I am bold *and cursive ~~and crossed out~~***", result);
    }

    @Test
    void testImageAndUrl() {
        String result = converter.convertToMarkdown("[img]imagelink[/img], [url=https://github.com/]click here[/url] or [url='https://github.com/']here[/url]");
        Assertions.assertEquals("![imagelink](imagelink), [click here](https://github.com/) or [here](https://github.com/)", result);
    }

    @Test
    void testCode() {
        String result = converter.convertToMarkdown("[code]Codeblock![/code]");
        Assertions.assertEquals("```\nCodeblock!\n```", result);
    }

    @Test
    void testInlineCode() {
        String result = converter.convertToMarkdown("My code is [icode]inline[/icode]!");
        Assertions.assertEquals("My code is `inline`!", result);
    }

    @Test
    void testAttachment() {
        String result = converter.convertToMarkdown("[attach]100[/attach]");
        Assertions.assertEquals("![https://www.spigotmc.org/attachments/100](https://www.spigotmc.org/attachments/100)", result);
    }

    @Test
    void testMedia() {
        String result = converter.convertToMarkdown("[MEDIA=youtube]dQw4w9WgXcQ[/MEDIA]");
        Assertions.assertEquals("@[YouTube](https://youtu.be/dQw4w9WgXcQ)", result);
    }

    @Test
    void testMediaUnsupportedPlatform() {
        String result = converter.convertToMarkdown("[MEDIA=vimeo]163721649[/MEDIA]");
        Assertions.assertEquals("[MEDIA=vimeo]163721649[/MEDIA]", result);
    }

    @Test
    void testCodeBlocksSameLine() {
        String result = converter.convertToMarkdown("""
            [code]{
              "key": "minecraft:plains",
              "override": {},
              "condition": {
                "type": "",
              }
            }[/code]""");
        Assertions.assertEquals("""
            ```
            {
              "key": "minecraft:plains",
              "override": {},
              "condition": {
                "type": "",
              }
            }
            ```""", result);
    }

    @Test
    void testCodeBlocksDiffLine() {
        Assertions.assertEquals("""
            ```

            MARKDOWN

            ```""",
            converter.convertToMarkdown("""
            [code]
            MARKDOWN
            [/code]"""));

        Assertions.assertEquals("""
            ```
            MARKDOWN
            ```""",
            converter.convertToMarkdown("[code]MARKDOWN[/code]"));
    }

    @Test
    void testCodeBlocksDiffLineLang() {
       Assertions.assertEquals("""
               ```kt

               E

               ```""",
           converter.convertToMarkdown("""
               [code=Kotlin]
               E
               [/code]"""));

        Assertions.assertEquals("""
               ```java
               E

               ```""",
            converter.convertToMarkdown("""
               [code=Java]E
               [/code]"""));

        Assertions.assertEquals("""
               ```java
               E
               ```""",
            converter.convertToMarkdown("[code=Java]E[/code]"));
    }

    @Test
    void testEscaping() {
        Assertions.assertEquals("`hi[B]bold[/B]`", converter.convertToMarkdown("[icode]hi[B]bold[/B][/icode]"));
        Assertions.assertEquals("""
            ```
            hi[B]bold[/B]
            ```""", converter.convertToMarkdown("[code]hi[B]bold[/B][/code]"));
    }

    @Test
    void testComplexExample() throws IOException {
        // Be sure to retest/-generate this output if "breaking" changes are made, for example to spacing
        String input = Files.readString(PATH.resolve("BBCodeExample.txt"));
        String expected = Files.readString(PATH.resolve("BBCodeConverted.txt"));
        String result = converter.convertToMarkdown(input);
        Assertions.assertEquals(expected, result);
    }
}
