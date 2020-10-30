package io.papermc.hangar.controller.forms.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

@Validated
public class License {

    private final String name;
    private final String url;

    @JsonCreator
    public License(@JsonProperty(value = "name", required = true) String name,
                   @JsonProperty(value = "url", required = true) String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }


    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "License{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
