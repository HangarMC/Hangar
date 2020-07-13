package me.minidigger.hangar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import me.minidigger.hangar.controller.HangarController;

@Controller
public class ReviewsController extends HangarController {

    @RequestMapping("/{author}/{slug}/versions/{version}/reviews")
    public Object showReviews(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement showReviews request controller
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/addmessage")
    public Object addMessage(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement addMessage request controller
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/approve")
    public Object approveReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement approveReview request controller
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/edit/{review}")
    public Object editReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version, @PathVariable Object review) {
        return null; // TODO implement editReview request controller
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/init")
    public Object createReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement createReview request controller
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/reopen")
    public Object reopenReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement reopenReview request controller
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/reviewtoggle")
    public Object backlogToggle(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement backlogToggle request controller
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/stop")
    public Object stopReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement stopReview request controller
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/reviews/takeover")
    public Object takeoverReview(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement takeoverReview request controller
    }

}

