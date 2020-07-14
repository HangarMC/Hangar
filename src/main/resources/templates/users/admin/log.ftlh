<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

@import controllers.sugar.Requests.OreRequest
@import ore.OreConfig
@import ore.db.{DbRef, Model}
@import ore.models.admin.LoggedActionViewModel
@import ore.models.project.{Page, Project, Version}
@import ore.models.user.{LoggedActionType, LoggedActionContext, User}
@import util.StringFormatterUtils._

@(actions: Seq[Model[LoggedActionViewModel[_]]], limit: Int, offset: Int, page: Int, size: Int, userFilter: Option[String],
        projectFilter: Option[String], versionFilter: Option[String], pageFilter: Option[DbRef[Page]],
        actionFilter: Option[String], subjectFilter: Option[String], canViewIP: Boolean)(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "diff-match-patch/javascript/diff_match_patch.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/actionLog.js" />"></script>
</#assign>

<#assign message><@spring.message "admin.log.title" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar>

    <div class="row">
        <div class="col-md-12  header-flags">
            <div class="clearfix">
                <h1 class="pull-left"><@spring.message "admin.log.title" /></h1>
            </div>
        </div>
        <div class="col-md-3">
            <h4>Active Filters:</h4>
            <table class="table table-super-condensed">
                <tr><td class="filter-user">User</td> <td>${userFilter!"-"}</td></tr>
                <tr><td class="filter-project">Project</td> <td>${projectFilter!"-"}</td></tr>
                <tr><td class="filter-version">Version</td> <td>${versionFilter!"-"}</td></tr>
                <tr><td class="filter-page">Page</td><td>${pageFilter!"-"} </td></tr>
                <tr><td class="filter-action">Action</td><td>${LoggedActionType.withValueOpt(actionFilter!"")!"-"}</td></tr>
                <tr><td class="filter-subject">Subject</td><td>${subjectFilter!"-"}</td></tr>
            </table>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title pull-left"><@spring.message "admin.log.title" /> &middot; Page #${page}</h4>
                    <div class="clearfix"></div>
                </div>
                <div class="table-responsive">
                    <table class="table table-condensed setting-no-border">
                        <tbody>
                            <tr>
                                <td><b>#</b></td>
                                <td><b>User</b></td>
                                <#if canViewIP>
                                    <td><b>IP Address</b></td>
                                </#if>
                                <td><b>Time</b></td>
                                <td><b>Action</b></td>
                                <td><b>Context</b></td>
                                <td><b>Old State</b></td>
                                <td><b>New State</b></td>
                            </tr>
                            <#if actions.isEmpty>
                                <tr><td><b>No activities founds</b></td></tr>
                            </#if>
                            @actions.zipWithIndex.map { case (action, index) =>
                            <tr>
                                <td><b>${offset + index + 1}</b></td>
                                <td>
                                    <a href="${routes.getRouteUrl("users.showProjects", action.userName)}">${action.userName}</a>
                                    <small class="filter-user">(<a href="${routes.getRouteUrl("showLog", page, action.userName, projectFilter, versionFilter, pageFilter, actionFilter, subjectFilter)}">${action.userName}</a>)</small>
                                </td>

                                <#if canViewIP>
                                    <td>${action.address.value}</td>
                                </#if>
                                <td>${action.createdAt?string.long}</td>
                                <td>
                                    ${action.action}
                                <small class="filter-action">(<a href="${routes.getRouteUrl("showLog", page, userFilter, projectFilter, versionFilter, pageFilter, action.action.value, subjectFilter)}">${action.action.value}</a>)</small>
                                </td>
                                <#if action.actionContext == LoggedActionContext.User || action.actionContext == LoggedActionContext.Organization>
                                    <td>
                                        <a href="${routes.getRouteUrl("users.showProjects", action.subject.username!"Unknown")}">${action.subject.username}</a>
                                        <small class="filter-subject">(<a href="${routes.getRouteUrl("showLog", page, userFilter, projectFilter, versionFilter, pageFilter, actionFilter, action.subject.username)}">${action.subject.username}</a>)</small>
                                    </td>
                                <#elseif action.project.id.isEmpty>
                                    <td>
                                        Resource deleted
                                        <#if action.actionContext == LoggedActionContext.Project || action.actionContext == LoggedActionContext.ProjectPage>
                                            <small class="filter-project">(<a href="${routes.getRouteUrl("showLog", page, userFilter, action.project.pluginId, versionFilter, pageFilter, actionFilter, subjectFilter)}">${action.project.pluginId}</a>)</small>
                                        <#else>
                                            <small class="filter-version">(<a href="${routes.getRouteUrl("showLog", page, userFilter, projectFilter, action.version.versionString, pageFilter, actionFilter, subjectFilter)}">${action.version.versionString}</a>)</small>
                                        </#if>
                                    </td>
                                <#elseif action.actionContext == LoggedActionContext.Project>
                                    <td>
                                        <a href="${routes.getRouteUrl("projects.show", action.project.ownerName!"Unknown", action.project.slug!"Unknown")}">${action.project.ownerName}/${action.project.slug!"Unknown"}</a>
                                        <small class="filter-project">(<a href="${routes.getRouteUrl("showLog", page, userFilter, action.project.pluginId, versionFilter, pageFilter, actionFilter, subjectFilter)}">${action.project.pluginId}</a>)</small>
                                    </td>
                                <#elseif action.actionContext == LoggedActionContext.ProjectPage>
                                    <td>
                                        <a href="${routes.getRouteUrl("pages.show", action.project.ownerName!"Unknown", action.project.slug!"Unknown", action.page.slug!"Unknown")}">${action.project.ownerName!"Unknown"}/${action.project.slug!"Unknown"}/${action.page.slug!"Unknown"}</a>
                                        <small class="filter-project">(<a href="${routes.getRouteUrl("showLog", page, userFilter, action.project.pluginId, versionFilter, pageFilter, actionFilter, subjectFilter)}">${action.project.pluginId}</a>)</small>
                                        <small class="filter-page">(<a href="${routes.getRouteUrl("showLog", page, userFilter, projectFilter, versionFilter, action.page.id, actionFilter, subjectFilter)}">${action.page.id}</a>)</small>
                                    </td>
                                <#elseif action.actionContext == LoggedActionContext.Version>
                                    <td>
                                        <a href="${routes.getRouteUrl("versions.show", action.project.ownerName!"Unknown", action.project.slug!"Unknown", action.version.versionString!"Unknown")}">${action.project.ownerName!"Unknown"}/${action.project.slug!"Unknown"}/${action.version.versionString!"Unknown"}</a>
                                        <small class="filter-project">(<a href="${routes.getRouteUrl("showLog", page, userFilter, action.project.pluginId, versionFilter, pageFilter, actionFilter, subjectFilter)}">${action.project.pluginId}</a>)</small>
                                        <small class="filter-version">(<a href="${routes.getRouteUrl("showLog", page, userFilter, projectFilter, action.version.versionString, pageFilter, actionFilter, subjectFilter)}">${action.version.versionString}</a>)</small>
                                    </td>
                                </#if>
                                @* Show diff only when page edit *@
                                <#if action.action == LoggedActionType.ProjectPageEdited || action.action == LoggedActionType.VersionDescriptionEdited>
                                    <td>
                                        <a href="#" class="data-view-old" data-view="${offset + index}">View</a>
                                        <textarea style="display: none" data-oldstate="${offset + index}">${action.oldState}</textarea>
                                    </td>
                                    <td>
                                        <a href="#" class="data-view-new" data-view="${offset + index}">View</a> <small>(<a href="#" class="data-diff" data-diff="${offset + index}">Show diff</a>)</small>
                                        <textarea style="display: none" data-newstate="${offset + index}">${action.newState}</textarea>
                                    </td>
                                <#else>
                                    <td>${action.oldState}</td>
                                    <td>${action.newState}</td>
                                </#if>
                            </tr>
                            }
                        </tbody>
                    </table>
                </div>
                <div class="panel-heading" style="display:block">
                    <#if page gt 1>
                        <h4 class="panel-title pull-left"><a href="${routes.getRouteUrl("showLog", page-1, userFilter, projectFilter, versionFilter, pageFilter, actionFilter, subjectFilter)}">&leftarrow; ${page - 1}</a></h4>
                    </#if>
                    <#if offset + limit < size>
                        <h4 class="panel-title pull-right"><a href="${routes.getRouteUrl("showLog", page+1, userFilter, projectFilter, versionFilter, pageFilter, actionFilter, subjectFilter)}">${page + 1} &rightarrow;</a></h4>
                    </#if>
                    <div class="clearfix"></div>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="modal-view" tabindex="-1" role="dialog">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title">View</h4>
                </div>
                <div class="modal-body" id="modal-view-body">
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</@base.base>
