package io.papermc.hangar.components.discovery;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Anyone
@RateLimit(path = "discovery")
@RequestMapping(path = "/api/internal/discovery", produces = MediaType.APPLICATION_JSON_VALUE)
public class DiscoveryController extends HangarComponent {

    private final DiscoveryService discoveryService;

    public DiscoveryController(final DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping("/daily")
    public List<ProjectCompact> daily() {
        return this.discoveryService.getDailyPicks();
    }

    @GetMapping("/excluded")
    @PermissionRequired(NamedPermission.REVIEWER)
    public List<ExcludedProject> excluded() {
        return this.discoveryService.getExcluded();
    }

    @PostMapping("/exclude/{projectId}")
    @PermissionRequired(NamedPermission.REVIEWER)
    public void exclude(@PathVariable final long projectId) {
        this.discoveryService.exclude(projectId, this.getHangarPrincipal().getUserId());
    }

    @PostMapping("/include/{projectId}")
    @PermissionRequired(NamedPermission.REVIEWER)
    public void include(@PathVariable final long projectId) {
        this.discoveryService.include(projectId);
    }
}
