package io.papermc.hangar.controller.internal;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.controllerold.forms.FlagForm;
import io.papermc.hangar.model.internal.projects.HangarProjectFlag;
import io.papermc.hangar.service.internal.admin.FlagService;

@Controller
@Secured("ROLE_USER")
@RequestMapping(path = "/api/internal/flags", produces = MediaType.APPLICATION_JSON_VALUE)
public class FlagController extends HangarController {

    private final FlagService flagService;

    public FlagController(FlagService flagService) {
        this.flagService = flagService;
    }


    @PostMapping("/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void flag(@RequestBody FlagForm form) {
        flagService.createFlag(form.getProjectId(), getHangarUserId(), form.getReason(), form.getComment());
    }

    @ResponseBody
    @GetMapping("/{author}/{slug}")
    public List<HangarProjectFlag> getFlags(@PathVariable("author") String author, @PathVariable("slug") String slug) {
        return flagService.getFlags(author, slug);
    }

    @ResponseBody
    @GetMapping("/")
    public List<HangarProjectFlag> getFlags() {
        return flagService.getFlags();
    }
}
