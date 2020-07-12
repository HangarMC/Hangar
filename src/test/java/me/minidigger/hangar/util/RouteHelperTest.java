package me.minidigger.hangar.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RouteHelper.class})
class RouteHelperTest {

    @Autowired
    private RouteHelper classUnderTest;

    @Test
    void testSomeShit() throws Exception {
        assertEquals("/", classUnderTest.getRouteUrl("showHome"));
        assertEquals("/organisations/new", classUnderTest.getRouteUrl("org.showCreator"));
        assertEquals("/linkout", classUnderTest.getRouteUrl("linkout"));
        assertEquals("/linkout?remoteUrl=TestUrl", classUnderTest.getRouteUrl("linkout", "TestUrl"));
        assertEquals("/staff", classUnderTest.getRouteUrl("users.showStaff"));
        assertEquals("/staff?sort=ASC&page=1", classUnderTest.getRouteUrl("users.showStaff", "ASC", "1"));
        assertEquals("/staff?page=1", classUnderTest.getRouteUrl("users.showStaff", "", "1"));
        assertEquals("/staff?sort=ASC", classUnderTest.getRouteUrl("users.showStaff", "ASC"));
    }
}
