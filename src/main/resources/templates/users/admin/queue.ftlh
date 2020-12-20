<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign scriptsVar>
    <script nonce="${nonce}">
        <#outputformat "JavaScript">
        window.MAX_REVIEW_TIME = ${config.queue.maxReviewTime.toMillis()};
        </#outputformat>
    </script>
    <script type="text/javascript" src="${hangar.url("js/queue.js")}"></script>
</#assign>

<#assign message><@spring.message "user.queue" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar>

    <div class="row">
        <div class="col-md-12 header-flags">
            <h2><@spring.message "user.queue.progress" /></h2>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title float-left"><@spring.message "user.queue.progress" /></h4>
                    <div class="clearfix"></div>
                </div>
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>Project version</th>
                            <th>Queued by</th>
                            <th style="text-align: right; max-width: 40px"></th>
                            <th>Status</th>
                            <th ></th>
                        </tr>
                    </thead>
                    <tbody>
                        <#if !underReview?has_content>
                            <tr>
                                <th rowspan="5">
                                    <h3><@spring.message "queue.review.none" /></h3>
                                </th>
                            </tr>
                        </#if>
                        <#list underReview as entry>
                        <tr <#if entry.unfinished && headerData.isCurrentUser(entry.reviewerId)>class="warning"</#if>>
                        <td>
                            <a href="${Routes.VERSIONS_SHOW.getRouteUrl(entry.author, entry.slug, entry.versionStringUrl)}">
                                ${entry.namespace}
                            </a>
                            <br>
                            ${entry.versionString}
                            <span class="channel" style="background-color: ${entry.channelColor.hex}">${entry.channelName}</span>
                        </td>
                        <td>
                            <#if entry.versionAuthor??>
                                <a href="https://papermc.io/forums/users/${entry.versionAuthor}">
                                ${entry.versionAuthor}
                                </a>
                            <#else>
                                Unknown
                            </#if>
                        <br>
                        ${utils.prettifyDateTime(entry.versionCreatedAt)}
                        </td>
                        <td style="text-align: right; max-width: 40px">
                        <#if entry.unfinished>
                            <#if headerData.isCurrentUser(entry.reviewerId)>
                                <i class="status fas fa-fw fa-play-circle fa-2x" style="color: green"></i>
                            <#else>
                                <i class="status fas fa-fw fa-cog fa-2x" style="color: black"></i>
                            </#if>
                        <#else>
                            <i class="status fas fa-fw fa-pause-circle fa-2x" style="color: orange"></i>
                        </#if>
                        </td>
                        <td <#if entry.unfinished>style="color: darkred"</#if> >
                        <#if entry.unfinished>
                            ${entry.reviewerName}
                            <br>
                            <span data-ago="${entry.reviewStarted.toInstant().toEpochMilli()}" data-title="started "></span>
                        <#else>
                            <strike>${entry.reviewerName}</strike>
                            <br>
                            <span data-ago="${entry.reviewStarted.toInstant().toEpochMilli()}" data-title="abandoned "></span>
                        </#if>
                        </td>
                        <td style="vertical-align: middle; text-align: right; padding-right: 15px;">
                            <a href="/${utils.urlEncode(entry.author)}/${utils.urlEncode(entry.slug)}/versions/${entry.versionString}/reviews"><i class="fas fa-2x fa-fw fa-info"></i></a>
                        </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>

        </div>
    </div>
    <div class="row">
        <div class="col-md-12 header-flags">
            <h2><@spring.message "user.queue.open" /></h2>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title float-left"><@spring.message "user.queue.open" /></h4>
                    <div class="clearfix"></div>
                </div>
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th></th>
                            <th>Project</th>
                            <th>Version</th>
                            <th>Queued by</th>
                            <th style="text-align: right">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#if !versions?has_content>
                            <tr>
                                <th rowspan="5">
                                    <h3><i class="fas fa-thumbs-o-up"></i> <@spring.message "user.queue.none" /></h3>
                                </th>
                            </tr>
                        </#if>
                        <#list versions?sort_by("versionCreatedAt") as entry>
                        <tr data-version="${utils.urlEncode(entry.author)}/${utils.urlEncode(entry.slug)}/versions/${entry.versionString}">
                            <td>
                                <#import "*/utils/userAvatar.ftlh" as userAvatar>
                                <@userAvatar.userAvatar userName=entry.author avatarUrl=utils.avatarUrl(entry.author) clazz="user-avatar-xs"></@userAvatar.userAvatar>
                            </td>
                            <td>
                                <a href="${Routes.VERSIONS_SHOW.getRouteUrl(entry.author, entry.slug, entry.versionStringUrl)}">
                                    ${entry.namespace}
                                </a>
                            </td>
                            <td>
                                <span class="faint">${utils.prettifyDateTime(entry.versionCreatedAt)}</span>
                                <span class="minor">${entry.versionString}</span>
                                <span class="channel" style="background-color: ${entry.channelColor.hex};">${entry.channelName}</span>
                            </td>
                            <td>
                                <#if entry.versionAuthor??>
                                    <a href="https://papermc.io/forums/users/${entry.versionAuthor}">
                                    ${entry.versionAuthor}
                                    </a>
                                </#if>
                            <br>
                        <td style="vertical-align: middle; text-align: right">
                            <a class="btn btn-success" href="/${utils.urlEncode(entry.author)}/${utils.urlEncode(entry.slug)}/versions/${entry.versionString}/reviews">Start review</a>
                        </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>

        </div>
    </div>
</@base.base>
