package io.papermc.hangar.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.papermc.hangar.db.dao.ProjectVersionReviewDao;
import io.papermc.hangar.model.viewhelpers.VersionReview;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.model.ProjectVersionReviewsTable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final HangarDao<ProjectVersionReviewDao> projectVersionReviewDao;

    @Autowired
    public ReviewService(HangarDao<ProjectVersionReviewDao> projectVersionReviewDao) {
        this.projectVersionReviewDao = projectVersionReviewDao;
    }

    public ProjectVersionReviewsTable insert(ProjectVersionReviewsTable projectVersionReviewsTable) {
        return projectVersionReviewDao.get().insert(projectVersionReviewsTable);
    }

    public void update(ProjectVersionReviewsTable projectVersionReviewsTable) {
        projectVersionReviewDao.get().update(projectVersionReviewsTable);
    }

    public VersionReview getById(long reviewId) {
        Entry<String, ProjectVersionReviewsTable> entry = projectVersionReviewDao.get().getById(reviewId);
        if (entry == null) return null;
        try {
            return new VersionReview(entry.getKey(), entry.getValue());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<VersionReview> getRecentReviews(long versionId) {
       return projectVersionReviewDao.get().getMostRecentReviews(versionId).stream().map(entry -> {
           try {
               return new VersionReview(entry.getKey(), entry.getValue());
           } catch (JsonProcessingException e) {
               e.printStackTrace();
               return null;
           }
       }).collect(Collectors.toList());
    }

    public List<VersionReview> getUnfinishedReviews(long versionId) {
        return projectVersionReviewDao.get().getUnfinishedReviews(versionId).stream().map(entry -> {
            try {
                return new VersionReview(entry.getKey(), entry.getValue());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());
    }

    public VersionReview getMostRecentUnfinishedReview(long versionId) {
        return getUnfinishedReviews(versionId).stream().findFirst().orElse(null);
    }
}
