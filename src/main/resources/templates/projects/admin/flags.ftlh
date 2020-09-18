<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign message><@spring.message "project.flag.plural" /></#assign>
<@base.base title="${message}">
    <div class="row">
        <div class="col-md-12  header-flags">
            <div class="clearfix">
                <h1 class="float-left"><@spring.message "project.flag.plural" /> for <a href="${Routes.PROJECTS_SHOW.getRouteUrl(p.project.ownerName, p.project.slug)}">${p.project.ownerName}/${p.project.slug}</a></h1>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <#if p.flagCount == 0>
                <div class="alert-review alert alert-info" role="alert">
                    <i class="fas fa-info-circle"></i>
                    No flags found
                </div>
            <#else>
                <div class="card">
                    <div class="card-header">
                        <h4 class="card-title float-left"><@spring.message "project.flag.plural" /></h4>
                        <div class="clearfix"></div>
                    </div>
                    <table class="table table-sm setting-no-border table-review-log">
                        <thead>
                            <tr>
                                <th>Submitter</th>
                                <th>Reason</th>
                                <th>When</th>
                                <th>Resolved</th>
                            </tr>
                        </thead>
                        <tbody>
                        <#list p.flags as flag>
                            <tr>
                                <td>${flag.reportedBy}</td>
                                <td>${flag.flag.reason.title}, ${flag.flag.comment}</td>
                                <td>${utils.prettifyDateTime(flag.flag.createdAt)}</td>
                                <#if flag.flag.isResolved>
                                    <td>${flag.resolvedBy} at ${utils.prettifyDateTime(flag.flag.resolvedAt)}</td>
                                <#else>
                                    <td>-not resolved-</td>
                                </#if>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
             </#if>
            </div>
        </div>
    </div>
</@base.base>
