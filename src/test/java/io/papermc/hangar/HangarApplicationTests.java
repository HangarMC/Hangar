package io.papermc.hangar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.papermc.hangar.controller.ApplicationController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HangarApplicationTests {

    @Autowired
    private ApplicationController controller;

    @Test
    void contextLoads() {
        assertThat(this.controller).isNotNull();
    }
}
