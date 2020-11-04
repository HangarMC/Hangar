<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign scriptsVar>
    <script type="text/javascript" src="${hangar.url("js/actionLog.js")}"></script>
</#assign>

<#assign message><@spring.message "admin.log.title" /></#assign>
<#-- @ftlvariable name="canViewIP" type="java.lang.Boolean" -->
<#-- @ftlvariable name="limit" type="java.lang.Integer" -->
<#-- @ftlvariable name="offset" type="java.lang.Integer" -->
<#-- @ftlvariable name="page" type="java.lang.Integer" -->
<#-- @ftlvariable name="size" type="java.lang.Integer" -->
<@base.base title="${message}" additionalScripts=scriptsVar>
    <div class="row">
        <div class="col-md-12  header-flags">
            <div class="clearfix">
                <h1 class="float-left"><@spring.message "admin.log.title" /></h1>
            </div>
        </div>
        <div class="col-md-3">
            <h4>Active Filters:</h4>
            <table class="table table-super-condensed">
                <tr><td class="filter-user">User</td> <td>${userFilter!"-"}</td></tr>
                <tr><td class="filter-project">Project</td> <td>${projectFilter!"-"}</td></tr>
                <tr><td class="filter-version">Version</td> <td>${versionFilter!"-"}</td></tr>
                <tr><td class="filter-page">Page</td><td>${pageFilter!"-"} </td></tr>
                <tr><td class="filter-action">Action</td><td>${actionFilter!"-"}</td></tr>
                <tr><td class="filter-subject">Subject</td><td>${subjectFilter!"-"}</td></tr>
            </table>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title float-left"><@spring.message "admin.log.title" /> &middot; Page #${page}</h4>
                    <div class="clearfix"></div>
                </div>
                <div class="table-responsive">
                    <table class="table table-sm setting-no-border">
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
                            <#if !actions?has_content>
                                <tr><td><b>No activities founds</b></td></tr>
                            </#if>
                            <#list actions as action>
                            <tr>
                                <td><b>${offset + action?index + 1}</b></td>
                                <td>
                                    <a href="${Routes.USERS_SHOW_PROJECTS.getRouteUrl(action.userName)}">${action.userName}</a>
                                    <small class="filter-user">(<a href="${Routes.SHOW_LOG.getRouteUrl(page?string, action.userName, projectFilter, versionFilter, pageFilter, actionFilter, subjectFilter)}">${action.userName}</a>)</small>
                                </td>

                                <#if canViewIP>
                                    <td>${action.address}</td>
                                </#if>
                                <td>${utils.prettifyDateTime(action.createdAt)}</td>
                                <td>
                                    ${action.action.description}
                                <small class="filter-action">(<a href="${Routes.SHOW_LOG.getRouteUrl(page?string, userFilter, projectFilter, versionFilter, pageFilter, action.action.value, subjectFilter)}">${action.action.value}</a>)</small>
                                </td>
                                <#if action.actionContext.class.simpleName == "UserContext" || action.actionContext.class.simpleName == "OrganizationContext">
                                    <td>
                                        <a href="${Routes.USERS_SHOW_PROJECTS.getRouteUrl(action.subject.username!"Unknown")}">${action.subject.username}</a>
                                        <small class="filter-subject">(<a href="${Routes.SHOW_LOG.getRouteUrl(page?string, userFilter, projectFilter, versionFilter, pageFilter, actionFilter, action.subject.username)}">${action.subject.username}</a>)</small>
                                    </td>
                                <#elseif !action.project.id??>
                                    <td>
                                        Resource deleted
                                        <#if action.actionContext.class.simpleName == "ProjectContext" || action.actionContext.class.simpleName == "ProjectPageContext">
                                            <small class="filter-project">(<a href="${Routes.SHOW_LOG.getRouteUrl(page?string, userFilter, action.project.slug, versionFilter, pageFilter, actionFilter, subjectFilter)}">${action.project.slug}</a>)</small>
                                        <#else>
                                            <small class="filter-version">(<a href="${Routes.SHOW_LOG.getRouteUrl(page?string, userFilter, projectFilter, action.version.versionString, pageFilter, actionFilter, subjectFilter)}">${action.version.versionString}</a>)</small>
                                        </#if>
                                    </td>
                                <#elseif action.actionContext.class.simpleName == "ProjectContext">
                                    <td>
                                        <a <#if action.project.owner?? && action.project.slug??>href="${Routes.PROJECTS_SHOW.getRouteUrl(action.project.owner!"Unknown", action.project.slug!"Unknown")}"</#if>>${action.project.owner!"Unknown"}/${action.project.slug!"Unknown"}</a>
                                        <small class="filter-project">(<a <#if action.project.slug??>href="${Routes.SHOW_LOG.getRouteUrl(page?string, userFilter, action.project.slug, versionFilter, pageFilter, actionFilter, subjectFilter)}"</#if>>${action.project.slug!"Unknown"}</a>)</small>
                                    </td>
                                <#elseif action.actionContext.class.simpleName == "ProjectPageContext">
                                    <td>
                                        <a href="${Routes.PAGES_SHOW.getRouteUrl(action.project.owner!"Unknown", action.project.slug!"Unknown", action.page.slug!"Unknown")}">${action.project.owner!"Unknown"}/${action.project.slug!"Unknown"}/${action.page.slug!"Unknown"}</a>
                                        <small class="filter-project">(<a <#if action.project.slug??>href="${Routes.SHOW_LOG.getRouteUrl(page?string, userFilter, action.project.slug, versionFilter, pageFilter, actionFilter, subjectFilter)}"</#if>>${action.project.slug!"Unknown"}</a>)</small>
                                        <small class="filter-page">(<a href="${Routes.SHOW_LOG.getRouteUrl(page?string, userFilter, projectFilter, versionFilter, action.page.id?string, actionFilter, subjectFilter)}">${action.page.id}</a>)</small>
                                    </td>
                                <#elseif action.actionContext.class.simpleName == "VersionContext">
                                    <td>
                                        <a <#if action.project.owner?? && action.project.slug?? && action.project.versionString??>href="${Routes.VERSIONS_SHOW.getRouteUrl(action.project.owner!"Unknown", action.project.slug!"Unknown", action.version.versionStringUrl!"Unknown")}"</#if>>${action.project.owner!"Unknown"}/${action.project.slug!"Unknown"}/${action.version.versionString!"Unknown"}</a>
                                        <small class="filter-project">(<a <#if action.project.slug??>href="${Routes.SHOW_LOG.getRouteUrl(page?string, userFilter, action.project.slug, versionFilter, pageFilter, actionFilter, subjectFilter)}"</#if>>${action.project.slug!"Unknown"}</a>)</small>
                                        <small class="filter-version">(<a <#if action.version.versionString??>href="${Routes.SHOW_LOG.getRouteUrl(page?string, userFilter, projectFilter, action.version.versionString, pageFilter, actionFilter, subjectFilter)}"</#if>>${action.version.versionString!"Unknown"}</a>)</small>
                                    </td>
                                </#if>
                                <#-- Show diff only when page edit -->
                                <#if action.action.name == "ProjectPageEdited" || action.action.name == "ProjectPageDeleted" || action.action.name == "ProjectPageCreated" || action.action.name == "VersionDescriptionEdited">
                                    <td>
                                        <a href="#" class="data-view-old" data-view="${offset + action?index}">View</a>
                                        <textarea style="display: none" data-oldstate="${offset + action?index}">${action.oldState}</textarea>
                                    </td>
                                    <td>
                                        <a href="#" class="data-view-new" data-view="${offset + action?index}">View</a> <small>(<a href="#" class="data-diff" data-diff="${offset + action?index}">Show diff</a>)</small>
                                        <textarea style="display: none" data-newstate="${offset + action?index}">${action.newState}</textarea>
                                    </td>
                                <#else>
                                    <td>${action.oldState?has_content?string(action.oldState, "<i>none</i>")}</td>
                                    <td>${action.newState?has_content?string(action.newState, "<i>none</i>")}</td>
                                </#if>
                            </tr>
                            </#list>
                        </tbody>
                    </table>
                </div>
                <div class="card-header" style="display:block">
                    <#if page gt 1>
                        <h4 class="card-title float-left"><a href="${Routes.SHOW_LOG.getRouteUrl((page-1)?string, userFilter, projectFilter, versionFilter, pageFilter, actionFilter, subjectFilter)}">&leftarrow; ${page - 1}</a></h4>
                    </#if>
                    <#if offset + limit < size>
                        <h4 class="card-title float-right"><a href="${Routes.SHOW_LOG.getRouteUrl((page+1)?string, userFilter, projectFilter, versionFilter, pageFilter, actionFilter, subjectFilter)}">${page + 1} &rightarrow;</a></h4>
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
