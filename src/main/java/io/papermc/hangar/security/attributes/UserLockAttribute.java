package io.papermc.hangar.security.attributes;

import io.papermc.hangar.util.Routes;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.util.Assert;

public class UserLockAttribute implements ConfigAttribute {

    private final Routes route;
    private final String args;

    public UserLockAttribute(Routes route, String args) {
        this.route = route;
        Assert.notNull(route, "You must provide a route!");
        this.args = args;
    }

    public Routes getRoute() {
        return route;
    }

    public String getArgs() {
        return args;
    }

    @Override
    public String getAttribute() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserLockAttribute)) return false;

        UserLockAttribute that = (UserLockAttribute) obj;

        return that.route == this.route && that.args == this.args;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
