package io.papermc.hangar.util;

import io.papermc.hangar.controller.util.BBCodeConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BBCodeConverterTest {

    private final BBCodeConverter converter = new BBCodeConverter();

    @Test
    void testDuplicates() {
        String result = converter.convertToMarkdown("[b][s][b]Test[b]Tes[/b]t[/s][/b][/b]");
        Assertions.assertEquals(result, "**~~TestTest~~**");
    }

    @Test
    void testRemoved() {
        String result = converter.convertToMarkdown("[list][size=4][color=red][user=kennytv]T[/size][center]e[font=upsidedown][/center]st[/font][/color][/user][/list]");
        Assertions.assertEquals(result, "Test");
    }

    @Test
    void testList() {
        String result = converter.convertToMarkdown("[list]\n[*]Element 1\n[*]Element 2\n[/list]");
        Assertions.assertEquals(result, "\n* Element 1\n* Element 2\n");
    }

    @Test
    void testFormatting() {
        String result = converter.convertToMarkdown("[b]I am bold [i]and cursive [s]and crossed out[/s][/i][/b]");
        Assertions.assertEquals(result, "**I am bold *and cursive ~~and crossed out~~***");
    }

    @Test
    void testImageAndUrl() {
        String result = converter.convertToMarkdown("[img]imagelink[/img], [url=https://github.com/]click here[/url]");
        Assertions.assertEquals(result, "![imagelink](imagelink), [click here](https://github.com/)");
    }

    @Test
    void testCode() {
        String result = converter.convertToMarkdown("[code]Codeblock![/code]");
        Assertions.assertEquals(result, "```Codeblock!```");
    }

    @Test
    void testAttachment() {
        String result = converter.convertToMarkdown("[attach]100[/attach]");
        Assertions.assertEquals(result, "![https://www.spigotmc.org/attachments/100](https://www.spigotmc.org/attachments/100)");
    }
}
