package io.papermc.hangar.util;

import org.junit.jupiter.api.Test;

import static io.papermc.hangar.util.RequestUtil.appendParam;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestUtilTest {

    @Test
    void test_appendParam() {
        assertEquals("http://localhost:8080?test=test", appendParam("http://localhost:8080", "test", "test"));
        assertEquals("http://localhost:8080/?test=test", appendParam("http://localhost:8080/", "test", "test"));
        assertEquals("http://localhost:8080/?dum=dum&test=test", appendParam("http://localhost:8080/?dum=dum", "test", "test"));
        assertEquals("http://localhost:8080/?dum&test=test", appendParam("http://localhost:8080/?dum", "test", "test"));
    }
}
