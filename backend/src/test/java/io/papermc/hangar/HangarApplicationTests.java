package io.papermc.hangar;

import io.papermc.hangar.controller.ApplicationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HangarApplicationTests {

    @Autowired
    private ApplicationController controller;

    @Test
    void contextLoads() {
        assertThat(this.controller).isNotNull();
    }
}
