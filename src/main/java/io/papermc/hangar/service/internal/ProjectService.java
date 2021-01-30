package io.papermc.hangar.service.internal;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.ProjectDAO;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.VisibilityService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class ProjectService extends HangarService {

    public static final String AUTHOR = "author";
    public static final String SLUG = "slug";

    private final ProjectDAO projectDAO;
    private final VisibilityService visibilityService;

    @Autowired
    public ProjectService(HangarDao<ProjectDAO> projectDAO, VisibilityService visibilityService) {
        this.projectDAO = projectDAO.get();
        this.visibilityService = visibilityService;
    }

//    @Bean
//    @RequestScope
//    public Supplier<ProjectTable> projectTableSupplier() {
//        Optional<ProjectTable> projectTable = RequestUtil.requirePathParams(List.of(AUTHOR, SLUG), params -> this.getProjectTable(params[0], params[1]));
//        if (projectTable.isPresent()) {
//            return projectTable::get;
//        } else {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//        }
//    }

    @Nullable
    public ProjectTable getProjectTable(@Nullable Long projectId) {
        return getProjectTable(projectId, projectDAO::getById);
    }

    public ProjectTable getProjectTable(@Nullable String author, @Nullable String name) {
        return getProjectTable(author, name, projectDAO::getBySlug);
    }


    @Nullable
    private <T> ProjectTable getProjectTable(@Nullable T identifier, @NotNull Function<T, ProjectTable> projectTableFunction) {
        if (identifier == null) {
            return null;
        }
        return visibilityService.checkVisibility(projectTableFunction.apply(identifier), ProjectTable::getId);
    }

    @Nullable
    private <T> ProjectTable getProjectTable(@Nullable T identifierOne, @Nullable T identifierTwo, @NotNull BiFunction<T, T, ProjectTable> projectTableFunction) {
        if (identifierOne == null || identifierTwo == null) {
            return null;
        }
        return visibilityService.checkVisibility(projectTableFunction.apply(identifierOne, identifierTwo), ProjectTable::getId);
    }


}
