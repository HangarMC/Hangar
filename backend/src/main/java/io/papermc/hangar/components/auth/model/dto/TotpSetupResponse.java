package io.papermc.hangar.components.auth.model.dto;

public record TotpSetupResponse(String secret, String qrCode) {}
