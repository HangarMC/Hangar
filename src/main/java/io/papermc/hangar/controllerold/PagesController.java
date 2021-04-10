package io.papermc.hangar.controllerold;

import io.papermc.hangar.controllerold.forms.RawPage;
import io.papermc.hangar.utils.BBCodeConverter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller("oldPagesController")
public class PagesController extends HangarController {

    @PostMapping(value = "/pages/bb-convert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> bbConverter(@RequestBody RawPage rawObj) {
        BBCodeConverter bbCodeConverter = new BBCodeConverter();
        return ResponseEntity.ok(bbCodeConverter.convertToMarkdown(rawObj.getRaw()));
    }

}

