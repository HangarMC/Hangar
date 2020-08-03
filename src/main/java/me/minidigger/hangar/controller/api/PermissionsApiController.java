package me.minidigger.hangar.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.PermissionType;
import me.minidigger.hangar.model.generated.PermissionCheck;
import me.minidigger.hangar.model.generated.Permissions;
import me.minidigger.hangar.service.PermissionService;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.service.project.ProjectService;

@Controller
public class PermissionsApiController implements PermissionsApi {

    private static final Logger log = LoggerFactory.getLogger(PermissionsApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final PermissionService permissionService;
    private final UserService userService;
    private final ProjectService projectService;

    @Autowired
    public PermissionsApiController(ObjectMapper objectMapper, HttpServletRequest request, PermissionService permissionService, UserService userService, ProjectService projectService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.permissionService = permissionService;
        this.userService = userService;
        this.projectService = projectService;
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAll(List<NamedPermission> permissions, String pluginId, String organizationName) {
        try {
            return new ResponseEntity<>(objectMapper.readValue("{\n  \"result\" : true,\n  \"type\" : \"global\"\n}", PermissionCheck.class), HttpStatus.OK); // TODO Implement me
        } catch (IOException e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<PermissionCheck> hasAny(List<NamedPermission> permissions, String pluginId, String organizationName) {
        try {
            return new ResponseEntity<>(objectMapper.readValue("{\n  \"result\" : true,\n  \"type\" : \"global\"\n}", PermissionCheck.class), HttpStatus.OK); // TODO Implement me
        } catch (IOException e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<Permissions> showPermissions(String pluginName, String organizationName) {
        // TODO no clue what to do with organization Name
        long userId = userService.getCurrentUser().getId();
        long pluginId = projectService.getIdByPluginId(pluginName);
        PermissionType type = PermissionType.PROJECT;
        List<NamedPermission> perms = permissionService.getProjectPermissions(userId, pluginId);
        if (perms.isEmpty()) {
            type = PermissionType.GLOBAL;
            perms = permissionService.getGlobalPermissions(userId);
        }
        return new ResponseEntity<>(new Permissions().type(type).permissions(perms), HttpStatus.OK);
    }
}

