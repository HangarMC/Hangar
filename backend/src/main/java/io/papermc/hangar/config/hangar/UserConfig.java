package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "hangar.users")
public class UserConfig {
    private int maxTaglineLen = 100;
    private List<String> staffRoles = List.of("Hangar_Admin", "Hangar_Mod");

    public int getMaxTaglineLen() {
        return maxTaglineLen;
    }

    public void setMaxTaglineLen(int maxTaglineLen) {
        this.maxTaglineLen = maxTaglineLen;
    }

    public List<String> getStaffRoles() {
        return staffRoles;
    }

    public void setStaffRoles(List<String> staffRoles) {
        this.staffRoles = staffRoles;
    }
}
