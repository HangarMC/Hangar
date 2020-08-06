package me.minidigger.hangar.controller;

import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.viewhelpers.VersionData;
import me.minidigger.hangar.security.annotations.GlobalPermission;
import me.minidigger.hangar.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ReviewsController extends HangarController {

    private final VersionService versionService;

    @Autowired
    public ReviewsController(VersionService versionService) {
        this.versionService = versionService;
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/reviews")
    public ModelAndView showReviews(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        ModelAndView mav = new ModelAndView("users/admin/reviews");
        VersionData versionData = versionService.getVersionData(author, slug, version);
        // TODO finish controller
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/addmessage")
    public Object addMessage(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement addMessage request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/approve")
    public Object approveReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement approveReview request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/edit/{review}")
    public Object editReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version, @PathVariable Object review) {
        return null; // TODO implement editReview request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/init")
    public Object createReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement createReview request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/reopen")
    public Object reopenReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement reopenReview request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/reviewtoggle")
    public Object backlogToggle(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement backlogToggle request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/stop")
    public Object stopReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement stopReview request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/takeover")
    public Object takeoverReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement takeoverReview request controller
    }

}

