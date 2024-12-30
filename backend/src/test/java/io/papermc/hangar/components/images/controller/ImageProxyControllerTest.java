package io.papermc.hangar.components.images.controller;

import io.papermc.hangar.controller.helper.ControllerTest;
import java.net.URI;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImageProxyControllerTest extends ControllerTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid",
        "http://invalid",
        "https://invalid",
        "javascript://alert('XSS')",
        "mailto:invalid",
        "ftp://invalid",
        "file://invalid",
        "/invalid",
    })
    public void proxyShouldReturnBadRequestForInvalidTarget(final String url) throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(new URI("/api/internal/image/" + url)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("400 BAD_REQUEST \"Bad target\""));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "./invalid",
        "../invalid",
        "stuff/../invalid",
    })
    public void proxyShouldRejectViaSpringFirewallForInvalidRequests(final String url) throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(new URI("/api/internal/image/" + url)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://img.shields.io/badge/License-AGPL%2520v3-yellow.svg?style=flat-square",
        "https://img.shields.io/badge/License-AGPL%20v3-yellow.svg?style=flat-square",
        "https://img.shields.io/badge/License-AGPLv3-yellow.svg?style=flat-square",
        "https://img.shields.io/badge/License-AGPL+v3-yellow.svg?style=flat-square",
        "https://img.shields.io/discord/755734170709524557?label=%7F&logo=discord&logoColor=%235865F2"
    })
    public void proxyShouldReturnOkForValidUrls(final String url) throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(new URI("/api/internal/image/" + url)))
            .andExpect(status().isOk());
    }
}
