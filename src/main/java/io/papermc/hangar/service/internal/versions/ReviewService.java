package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarReviewsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionReviewsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.model.common.ReviewAction;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.reviews.ProjectVersionReviewMessageTable;
import io.papermc.hangar.model.db.versions.reviews.ProjectVersionReviewTable;
import io.papermc.hangar.model.internal.versions.HangarReview;
import io.papermc.hangar.service.HangarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService extends HangarService {

    private final ProjectVersionReviewsDAO projectVersionReviewsDAO;
    private final HangarReviewsDAO hangarReviewsDAO;
    private final ProjectVersionsDAO projectVersionsDAO;

    @Autowired
    public ReviewService(HangarDao<ProjectVersionReviewsDAO> projectVersionReviewsDAO, HangarDao<HangarReviewsDAO> hangarReviewsDAO, HangarDao<ProjectVersionsDAO> projectVersionsDAO) {
        this.projectVersionReviewsDAO = projectVersionReviewsDAO.get();
        this.hangarReviewsDAO = hangarReviewsDAO.get();
        this.projectVersionsDAO = projectVersionsDAO.get();
    }

    public List<HangarReview> getHangarReviews(long versionId) {
        return hangarReviewsDAO.getReviews(versionId);
    }

    public void startReview(long versionId, String msg) {
        ProjectVersionReviewTable projectVersionReviewTable = projectVersionReviewsDAO.insert(new ProjectVersionReviewTable(versionId, getHangarPrincipal().getUserId()));
        projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(projectVersionReviewTable.getId(), msg, ReviewAction.START));
        ProjectVersionTable projectVersionTable = projectVersionsDAO.getProjectVersionTable(versionId);
        projectVersionTable.setReviewState(ReviewState.UNDER_REVIEW);
        projectVersionsDAO.update(projectVersionTable);
    }
}
