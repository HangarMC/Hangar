package me.minidigger.hangar.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import me.minidigger.hangar.controller.HangarController;

@Controller
public class PagesController extends HangarController {

    @PostMapping("/pages/preview")
    public Object showPreview() {
        return null; // TODO implement showPreview request controller
    }

    @GetMapping("/{author}/{slug}/pages/{page}")
    public Object show(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object page) {
        return null; // TODO implement show request controller
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/pages/{page}/delete")
    public Object delete(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object page) {
        return null; // TODO implement delete request controller
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/pages/{page}/edit")
    public Object save(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object page) {
        return null; // TODO implement save request controller
    }

    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/pages/{page}/edit")
    public Object showEditor(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object page) {
        return null; // TODO implement showEditor request controller
    }

}

