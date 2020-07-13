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
        assertEquals("/linkout", classUnderTest.getRouteUrl("linkOut", ""));
        assertEquals("/linkout?remoteUrl=TestUrl", classUnderTest.getRouteUrl("linkOut", "TestUrl"));
        assertEquals("/staff", classUnderTest.getRouteUrl("users.showStaff", "", ""));
        assertEquals("/staff?sort=ASC&page=1", classUnderTest.getRouteUrl("users.showStaff", "ASC", "1"));
        assertEquals("/staff?page=1", classUnderTest.getRouteUrl("users.showStaff", "", "1"));
        assertEquals("/staff?sort=ASC", classUnderTest.getRouteUrl("users.showStaff", "ASC", ""));
        assertEquals("/api/v1/projects/Essentials/tags/1.33.7", classUnderTest.getRouteUrl("apiv1.listTags", "Essentials", "1.33.7"));
        assertEquals("/api/v1/projects/Essentials/pages?parentId=2", classUnderTest.getRouteUrl("apiv1.listPages", "Essentials", "2"));
        assertEquals("/api/v1/projects/Essentials/pages", classUnderTest.getRouteUrl("apiv1.listPages", "Essentials", ""));

    }
}
