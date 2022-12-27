package io.papermc.hangar.db.customtypes;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import org.postgresql.util.PGobject;

public class RoleCategory extends PGobject {

    public static final RoleCategory GLOBAL = new RoleCategory("global");
    public static final RoleCategory PROJECT = new RoleCategory("project");
    public static final RoleCategory ORGANIZATION = new RoleCategory("organization");

    private String value;

    RoleCategory(final String value) {
        this();
        this.value = value;
    }

    public RoleCategory() {
        this.setType("role_category");
    }


    @Override
    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final RoleCategory that = (RoleCategory) o;

        return Objects.equals(this.value, that.value);
    }

    @Override
    public String toString() {
        return "RoleCategory{value:" + this.value + "}";
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.value != null ? this.value.hashCode() : 0);
        return result;
    }
}
