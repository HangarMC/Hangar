package io.papermc.hangar;

import io.papermc.hangar.controller.ApplicationController;
import io.papermc.hangar.controller.helper.ControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

class HangarApplicationTests extends ControllerTest {

    @Autowired
    private ApplicationController controller;

    @Test
    void contextLoads() {
        assertThat(this.controller).isNotNull();
    }
}
