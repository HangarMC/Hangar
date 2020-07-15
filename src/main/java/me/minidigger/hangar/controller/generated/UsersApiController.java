package me.minidigger.hangar.controller.generated;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import me.minidigger.hangar.model.generated.PaginatedCompactProjectResult;
import me.minidigger.hangar.model.generated.ProjectSortingStrategy;
import me.minidigger.hangar.model.generated.User;

@Controller
public class UsersApiController implements UsersApi {

    private static final Logger log = LoggerFactory.getLogger(UsersApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    public UsersApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @Override
    public ResponseEntity<PaginatedCompactProjectResult> showStarred(String user, ProjectSortingStrategy sort, Long limit, Long offset) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<>(objectMapper.readValue("{\n  \"result\" : [ {\n    \"plugin_id\" : \"plugin_id\",\n    \"promoted_versions\" : [ {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    }, {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    } ],\n    \"visibility\" : \"public\",\n    \"stats\" : {\n      \"downloads\" : 5,\n      \"recent_downloads\" : 7,\n      \"recent_views\" : 2,\n      \"watchers\" : 3,\n      \"stars\" : 9,\n      \"views\" : 5\n    },\n    \"name\" : \"name\",\n    \"namespace\" : {\n      \"owner\" : \"owner\",\n      \"slug\" : \"slug\"\n    },\n    \"category\" : \"admin_tools\"\n  }, {\n    \"plugin_id\" : \"plugin_id\",\n    \"promoted_versions\" : [ {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    }, {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    } ],\n    \"visibility\" : \"public\",\n    \"stats\" : {\n      \"downloads\" : 5,\n      \"recent_downloads\" : 7,\n      \"recent_views\" : 2,\n      \"watchers\" : 3,\n      \"stars\" : 9,\n      \"views\" : 5\n    },\n    \"name\" : \"name\",\n    \"namespace\" : {\n      \"owner\" : \"owner\",\n      \"slug\" : \"slug\"\n    },\n    \"category\" : \"admin_tools\"\n  } ],\n  \"pagination\" : {\n    \"offset\" : 6,\n    \"limit\" : 0,\n    \"count\" : 1\n  }\n}", PaginatedCompactProjectResult.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<User> showUser(String user) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<>(objectMapper.readValue("{\n  \"join_date\" : \"2000-01-23T04:56:07.000+00:00\",\n  \"roles\" : [ {\n    \"color\" : \"color\",\n    \"name\" : \"name\",\n    \"title\" : \"title\"\n  }, {\n    \"color\" : \"color\",\n    \"name\" : \"name\",\n    \"title\" : \"title\"\n  } ],\n  \"name\" : \"name\",\n  \"created_at\" : \"2000-01-23T04:56:07.000+00:00\",\n  \"tagline\" : \"tagline\"\n}", User.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<PaginatedCompactProjectResult> showWatching(@PathVariable("user") String user, ProjectSortingStrategy sort, Long limit, Long offset) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<>(objectMapper.readValue("{\n  \"result\" : [ {\n    \"plugin_id\" : \"plugin_id\",\n    \"promoted_versions\" : [ {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    }, {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    } ],\n    \"visibility\" : \"public\",\n    \"stats\" : {\n      \"downloads\" : 5,\n      \"recent_downloads\" : 7,\n      \"recent_views\" : 2,\n      \"watchers\" : 3,\n      \"stars\" : 9,\n      \"views\" : 5\n    },\n    \"name\" : \"name\",\n    \"namespace\" : {\n      \"owner\" : \"owner\",\n      \"slug\" : \"slug\"\n    },\n    \"category\" : \"admin_tools\"\n  }, {\n    \"plugin_id\" : \"plugin_id\",\n    \"promoted_versions\" : [ {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    }, {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    } ],\n    \"visibility\" : \"public\",\n    \"stats\" : {\n      \"downloads\" : 5,\n      \"recent_downloads\" : 7,\n      \"recent_views\" : 2,\n      \"watchers\" : 3,\n      \"stars\" : 9,\n      \"views\" : 5\n    },\n    \"name\" : \"name\",\n    \"namespace\" : {\n      \"owner\" : \"owner\",\n      \"slug\" : \"slug\"\n    },\n    \"category\" : \"admin_tools\"\n  } ],\n  \"pagination\" : {\n    \"offset\" : 6,\n    \"limit\" : 0,\n    \"count\" : 1\n  }\n}", PaginatedCompactProjectResult.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
