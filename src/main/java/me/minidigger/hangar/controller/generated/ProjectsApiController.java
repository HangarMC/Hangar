package me.minidigger.hangar.controller.generated;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import me.minidigger.hangar.model.generated.Category;
import me.minidigger.hangar.model.generated.PaginatedProjectResult;
import me.minidigger.hangar.model.generated.Project;
import me.minidigger.hangar.model.generated.ProjectMember;
import me.minidigger.hangar.model.generated.ProjectSortingStrategy;
import me.minidigger.hangar.model.generated.ProjectStatsDay;

@Controller
public class ProjectsApiController implements ProjectsApi {

    private static final Logger log = LoggerFactory.getLogger(ProjectsApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    public ProjectsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @Override
    public ResponseEntity<PaginatedProjectResult> listProjects(String q, List<Category> categories, List<String> tags, String owner, ProjectSortingStrategy sort, Boolean relevance, Long limit, Long offset) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<>(objectMapper.readValue("{\n  \"result\" : [ {\n    \"icon_url\" : \"icon_url\",\n    \"plugin_id\" : \"plugin_id\",\n    \"settings\" : {\n      \"license\" : {\n        \"name\" : \"name\",\n        \"url\" : \"url\"\n      },\n      \"sources\" : \"sources\",\n      \"forum_sync\" : true,\n      \"issues\" : \"issues\",\n      \"support\" : \"support\",\n      \"homepage\" : \"homepage\"\n    },\n    \"last_updated\" : \"2000-01-23T04:56:07.000+00:00\",\n    \"visibility\" : \"public\",\n    \"user_actions\" : {\n      \"starred\" : true,\n      \"watching\" : true\n    },\n    \"created_at\" : \"2000-01-23T04:56:07.000+00:00\",\n    \"description\" : \"description\",\n    \"promoted_versions\" : [ {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    }, {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    } ],\n    \"stats\" : {\n      \"downloads\" : 5,\n      \"recent_downloads\" : 7,\n      \"recent_views\" : 2,\n      \"watchers\" : 3,\n      \"stars\" : 9,\n      \"views\" : 5\n    },\n    \"name\" : \"name\",\n    \"namespace\" : {\n      \"owner\" : \"owner\",\n      \"slug\" : \"slug\"\n    },\n    \"category\" : \"admin_tools\"\n  }, {\n    \"icon_url\" : \"icon_url\",\n    \"plugin_id\" : \"plugin_id\",\n    \"settings\" : {\n      \"license\" : {\n        \"name\" : \"name\",\n        \"url\" : \"url\"\n      },\n      \"sources\" : \"sources\",\n      \"forum_sync\" : true,\n      \"issues\" : \"issues\",\n      \"support\" : \"support\",\n      \"homepage\" : \"homepage\"\n    },\n    \"last_updated\" : \"2000-01-23T04:56:07.000+00:00\",\n    \"visibility\" : \"public\",\n    \"user_actions\" : {\n      \"starred\" : true,\n      \"watching\" : true\n    },\n    \"created_at\" : \"2000-01-23T04:56:07.000+00:00\",\n    \"description\" : \"description\",\n    \"promoted_versions\" : [ {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    }, {\n      \"version\" : \"version\",\n      \"tags\" : [ {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      }, {\n        \"data\" : \"data\",\n        \"color\" : {\n          \"background\" : \"background\",\n          \"foreground\" : \"foreground\"\n        },\n        \"name\" : \"name\",\n        \"display_data\" : \"display_data\",\n        \"minecraft_version\" : \"minecraft_version\"\n      } ]\n    } ],\n    \"stats\" : {\n      \"downloads\" : 5,\n      \"recent_downloads\" : 7,\n      \"recent_views\" : 2,\n      \"watchers\" : 3,\n      \"stars\" : 9,\n      \"views\" : 5\n    },\n    \"name\" : \"name\",\n    \"namespace\" : {\n      \"owner\" : \"owner\",\n      \"slug\" : \"slug\"\n    },\n    \"category\" : \"admin_tools\"\n  } ],\n  \"pagination\" : {\n    \"offset\" : 6,\n    \"limit\" : 0,\n    \"count\" : 1\n  }\n}", PaginatedProjectResult.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<ProjectMember> showMembers(String pluginId, Long limit, Long offset) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<>(objectMapper.readValue("{\n  \"roles\" : [ {\n    \"color\" : \"color\",\n    \"name\" : \"name\",\n    \"title\" : \"title\"\n  }, {\n    \"color\" : \"color\",\n    \"name\" : \"name\",\n    \"title\" : \"title\"\n  } ],\n  \"user\" : \"user\"\n}", ProjectMember.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<Project> showProject(String pluginId) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<>(objectMapper.readValue("{\n  \"icon_url\" : \"icon_url\",\n  \"plugin_id\" : \"plugin_id\",\n  \"settings\" : {\n    \"license\" : {\n      \"name\" : \"name\",\n      \"url\" : \"url\"\n    },\n    \"sources\" : \"sources\",\n    \"forum_sync\" : true,\n    \"issues\" : \"issues\",\n    \"support\" : \"support\",\n    \"homepage\" : \"homepage\"\n  },\n  \"last_updated\" : \"2000-01-23T04:56:07.000+00:00\",\n  \"visibility\" : \"public\",\n  \"user_actions\" : {\n    \"starred\" : true,\n    \"watching\" : true\n  },\n  \"created_at\" : \"2000-01-23T04:56:07.000+00:00\",\n  \"description\" : \"description\",\n  \"promoted_versions\" : [ {\n    \"version\" : \"version\",\n    \"tags\" : [ {\n      \"data\" : \"data\",\n      \"color\" : {\n        \"background\" : \"background\",\n        \"foreground\" : \"foreground\"\n      },\n      \"name\" : \"name\",\n      \"display_data\" : \"display_data\",\n      \"minecraft_version\" : \"minecraft_version\"\n    }, {\n      \"data\" : \"data\",\n      \"color\" : {\n        \"background\" : \"background\",\n        \"foreground\" : \"foreground\"\n      },\n      \"name\" : \"name\",\n      \"display_data\" : \"display_data\",\n      \"minecraft_version\" : \"minecraft_version\"\n    } ]\n  }, {\n    \"version\" : \"version\",\n    \"tags\" : [ {\n      \"data\" : \"data\",\n      \"color\" : {\n        \"background\" : \"background\",\n        \"foreground\" : \"foreground\"\n      },\n      \"name\" : \"name\",\n      \"display_data\" : \"display_data\",\n      \"minecraft_version\" : \"minecraft_version\"\n    }, {\n      \"data\" : \"data\",\n      \"color\" : {\n        \"background\" : \"background\",\n        \"foreground\" : \"foreground\"\n      },\n      \"name\" : \"name\",\n      \"display_data\" : \"display_data\",\n      \"minecraft_version\" : \"minecraft_version\"\n    } ]\n  } ],\n  \"stats\" : {\n    \"downloads\" : 5,\n    \"recent_downloads\" : 7,\n    \"recent_views\" : 2,\n    \"watchers\" : 3,\n    \"stars\" : 9,\n    \"views\" : 5\n  },\n  \"name\" : \"name\",\n  \"namespace\" : {\n    \"owner\" : \"owner\",\n    \"slug\" : \"slug\"\n  },\n  \"category\" : \"admin_tools\"\n}", Project.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<Map<String, ProjectStatsDay>> showProjectStats(String pluginId, LocalDate fromDate, LocalDate toDate) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<Map<String, ProjectStatsDay>>(objectMapper.readValue("{\n  \"key\" : {\n    \"downloads\" : 0,\n    \"views\" : 6\n  }\n}", Map.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
