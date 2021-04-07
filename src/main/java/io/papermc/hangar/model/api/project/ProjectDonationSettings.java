package io.papermc.hangar.model.api.project;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.util.List;
import java.util.StringJoiner;

public class ProjectDonationSettings {

    private final boolean enable;
    private final String email;
    private final int defaultAmount;
    private final List<Integer> oneTimeAmounts;
    private final List<Integer> monthlyAmounts;

    @JdbiConstructor
    public ProjectDonationSettings(boolean enabled, String email, int defaultAmount, List<Integer> oneTimeAmounts, List<Integer> monthlyAmounts) {
        this.enable = enabled;
        this.email = email;
        this.defaultAmount = defaultAmount;
        this.oneTimeAmounts = oneTimeAmounts;
        this.monthlyAmounts = monthlyAmounts;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getEmail() {
        return email;
    }

    public int getDefaultAmount() {
        return defaultAmount;
    }

    public List<Integer> getOneTimeAmounts() {
        return oneTimeAmounts;
    }

    public List<Integer> getMonthlyAmounts() {
        return monthlyAmounts;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ProjectDonationSettings.class.getSimpleName() + "[", "]")
                .add("enable=" + enable)
                .add("email='" + email + "'")
                .add("defaultAmount=" + defaultAmount)
                .add("oneTimeAmounts=" + oneTimeAmounts)
                .add("monthlyAmounts=" + monthlyAmounts)
                .toString();
    }
}
