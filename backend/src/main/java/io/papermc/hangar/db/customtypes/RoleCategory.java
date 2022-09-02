package io.papermc.hangar.db.customtypes;

import com.fasterxml.jackson.annotation.JsonValue;
import org.postgresql.util.PGobject;

import java.util.Objects;

public class RoleCategory extends PGobject {

    public static final RoleCategory GLOBAL = new RoleCategory("global");
    public static final RoleCategory PROJECT = new RoleCategory("project");
    public static final RoleCategory ORGANIZATION = new RoleCategory("organization");

    private String value;

    RoleCategory(String value) {
        this();
        this.value = value;
    }

    public RoleCategory() {
        setType("role_category");
    }


    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RoleCategory that = (RoleCategory) o;

        return Objects.equals(value, that.value);
    }

    @Override
    public String toString() {
        return "RoleCategory{value:" + this.value + "}";
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
