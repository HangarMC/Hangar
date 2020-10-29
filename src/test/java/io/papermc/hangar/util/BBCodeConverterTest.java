package io.papermc.hangar.util;

import io.papermc.hangar.controller.util.BBCodeConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BBCodeConverterTest {

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
        Assertions.assertEquals("```Codeblock!```", result);
    }

    @Test
    void testAttachment() {
        String result = converter.convertToMarkdown("[attach]100[/attach]");
        Assertions.assertEquals("![https://www.spigotmc.org/attachments/100](https://www.spigotmc.org/attachments/100)", result);
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
