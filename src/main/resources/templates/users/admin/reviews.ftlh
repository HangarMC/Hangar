<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign scripts>
    <script nonce="${nonce}">
        <#outputformat "JavaScript">
        window.PROJECT = ${mapper.valueToTree(project.project)};
        window.VERSION = ${mapper.valueToTree(version.v)};
        window.MOST_RECENT_UNFINISHED_REVIEW = ${mapper.valueToTree(mostRecentUnfinishedReview)};
        <#if mostRecentUnfinishedReview??>
        window.MOST_RECENT_IS_CURRENT_USER = ${headerData.isCurrentUser(mostRecentUnfinishedReview.userId)?c};
        </#if>
        </#outputformat>
    </script>
    <script nonce="${nonce}" type="text/javascript" src="${hangar.url("js/version-reviews.js")}"></script>
</#assign>

<#assign ReviewState=@helper["io.papermc.hangar.model.generated.ReviewState"] />
<#-- @ftlvariable name="ReviewState" type="io.papermc.hangar.model.generated.ReviewState" -->
<#assign message><@spring.messageArgs code="review.title" args=[project.project.name, version.v.versionString] /></#assign>
<@base.base title=message additionalScripts=scripts>
    <div id="review-btn-group" class="row">
        <div class="col-md-12 header-flags">
            <div class="clearfix">
                <h1 class="float-left">
                    <a class="btn btn-primary" href="${Routes.VERSIONS_SHOW.getRouteUrl(project.project.ownerName, project.project.slug, version.v.versionStringUrl)}">
                        <i class="fas fa-arrow-left"></i>
                    </a>
                    ${project.project.name}
                    <i>${version.v.versionString}</i>
                </h1>
            </div>
            <p class="user date float-left">
                <a href="${Routes.USERS_SHOW_PROJECTS.getRouteUrl(project.project.ownerName)}">
                    <strong>${project.project.ownerName}</strong>
                </a>
                released this version on ${utils.prettifyDateTime(version.v.createdAt)}
            </p>
            <#if !version.v.reviewState.checked>
                <div class="float-right">
                    <span class="btn-group-sm">
                        <a href="#" class="btn btn-info btn-skip-review"><#if version.v.reviewState != ReviewState.BACKLOG> Remove from queue <#else> Add to queue </#if></a>
                        <a href="${Routes.PROJECTS_SHOW.getRouteUrl(project.project.ownerName, project.project.slug)}" class="btn btn-info">Project Page</a>
                        <a href="${Routes.VERSIONS_DOWNLOAD_JAR.getRouteUrl(project.project.ownerName, project.project.slug, version.v.versionStringUrl, "")}" class="btn btn-info">Download File</a>
                    </span>
                    <span class="btn-group-sm">
                    <#if mostRecentUnfinishedReview??>
                        <#if headerData.isCurrentUser(mostRecentUnfinishedReview.userId)>
                            <button class="btn btn-review-stop btn-danger"><i class="fas fa-stop-circle"></i> <@spring.message "review.stop" /></button>
                            <button class="btn btn-review-approve btn-success"><i class="fas fa-thumbs-up"></i> <@spring.message "user.queue.approve" /></button>
                            <button class="btn btn-review-approve-partial btn-success"><i class="fas fa-thumbs-up"></i> <@spring.message "user.queue.approvePartial" /></button>
                        <#else>
                            <button class="btn btn-review-takeover btn-danger"><i class="fas fa-clipboard"></i> <@spring.message "review.takeover" /></button>
                        </#if>
                    <#else>
                        <button class="btn btn-review-start btn-success"><i class="fas fa-terminal"></i> <@spring.message "review.start" /></button>
                    </#if>
                    </span>
                </div>
            <#else>
                <div class="float-right">
                    <span class="btn-group-sm">
                        <a href="" class="btn btn-danger btn-review-reopen">Undo approval</a>
                    </span>
                </div>
            </#if>
        </div>
    </div>
    <#if mostRecentUnfinishedReview??>
        <#if headerData.isCurrentUser(mostRecentUnfinishedReview.userId)>
            <div id="review-new-message" class="row">
                <div class="col-md-12">
                    <div class="input-group" style="width: 100%; margin-top: 1em; border-radius: 4px 4px 0 0;">
                        <div class="input-group-prepend">
                            <span class="input-group-text">Review Message</span>
                        </div>
                        <textarea type="text" class="form-control textarea-addmessage" autocomplete="off" aria-label="Review Message"></textarea>
                        <div class="input-group-append">
                            <button class="btn btn-review-addmessage-submit btn-primary">
                                <i class="fas fa-clipboard"></i>
                                <@spring.message "review.addmessage" />
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </#if>
    </#if>
    <div class="row version-description mt-2">
        <div class="col-md-12">
            <h2><@spring.message "review.log" /></h2>
        </div>
        <#if !reviews?has_content>
            <div class="col-md-12">
                <div class="alert-review alert alert-info" role="alert">
                    <i class="fas fa-info-circle"></i>
                    This version is not under review
                </div>
            </div>
        </#if>

        <div class="col-md-12">
            <table class="table table-sm setting-no-border table-review-log">
                <tbody>
                <#-- @ftlvariable name="review" type="io.papermc.hangar.model.viewhelpers.VersionReview" -->
                <#list reviews as review>
                    <#if review.endedAt??>
                        <#if reviews?size gt (reviews?size - review?index)>
                            <#if review.endedAt.toEpochSecond() == reviews?reverse[reviews?size - review?index - 1].endedAt.toEpochSecond()>
                                <tr>
                                    <td>${utils.prettifyDateTime(review.endedAt)}</td>
                                    <td>
                                        <strong>${review.userName!"Unknown"}</strong>
                                        took over from
                                        <strong>${review.userName!"Unknown"}</strong>
                                        <#if !review.messages?filter(m -> m.takeover)?has_content>
                                            <i>- no message provided -</i>
                                        <#else>
                                            <i>${markdownService.render(review.messages?filter(m -> m.takeover)?first.message)}</i>
                                        </#if>
                                    </td>
                                </tr>
                            <#else>
                                <tr>
                                    <td>${utils.prettifyDateTime(review.endedAt)}</td>
                                    <td><strong>${review.userName!"Unknown"}</strong> stopped</td>
                                </tr>
                            </#if>
                        <#else>
                            <#if version.approvedAt??>
                                <tr>
                                    <td>${utils.prettifyDateTime(review.endedAt)}</td>
                                    <td><strong>${review.userName!"Unknown"}</strong> approved</td>
                                </tr>
                            <#else>
                                <tr>
                                    <td>${utils.prettifyDateTime(review.endedAt)}</td>
                                    <td>
                                        <strong>${review.userName!"Unknown"}</strong>
                                        stopped
                                        <br>
                                        <#if !review.messages?filter(m -> m.stop)?has_content>
                                            <i>- no message provided -</i>
                                        <#else>
                                            <i>${markdownService.render(review.messages?filter(m -> m.stop)?first.message)}</i>
                                        </#if>
                                    </td>
                                </tr>
                            </#if>
                        </#if>
                    </#if>
                    <#list review.getMessages()?filter(m -> !m.takeover && !m.stop)?reverse as message>
                    <#-- @ftlvariable name="message" type="io.papermc.hangar.model.viewhelpers.VersionReviewMessage" -->
                        <tr>
                            <td>${message.getFormattedTime()}</td> <#-- TODO per locale-->
                            <td>
                                <strong>${review.userName!"Unknown"}</strong>
                                added message
                                <br>
                                <i>${markdownService.render(message.message)}</i>
                            </td>
                        </tr>
                    </#list>
                    <tr>
                        <td>${utils.prettifyDateTime(review.createdAt)}</td>
                        <td><strong>${review.userName!"Unknown"}</strong> started a review</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</@base.base>
