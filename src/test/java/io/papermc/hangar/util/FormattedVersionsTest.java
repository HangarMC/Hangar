package io.papermc.hangar.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class FormattedVersionsTest {

    @Test
    void testFormattedVersions() {
        List<String> list1 = List.of("1.1", "1.2", "1.3", "1.5", "1.7", "1.8");

        Assertions.assertEquals(StringUtils.formatVersionNumbers(new ArrayList<>(list1)), "1.1-3, 1.5, 1.7-8");

        List<String> list2 = List.of("1.20", "1.23", "1.25", "1.30", "1.31");
        Assertions.assertEquals(StringUtils.formatVersionNumbers(new ArrayList<>(list2)), "1.20, 1.23, 1.25, 1.30-31");

        List<String> list3 = List.of("1.1.0", "1.1.1", "1.2.0", "1.2.2", "1.3", "1.4");
        Assertions.assertEquals(StringUtils.formatVersionNumbers(new ArrayList<>(list3)), "1.1.0-1, 1.2.0, 1.2.2, 1.3-4");
    }
}
