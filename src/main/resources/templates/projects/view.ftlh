<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base>

<#--
Base template for Project overview.
-->
<#assign Visibility=@helper["me.minidigger.hangar.model.Visibility"]>
<#assign Permission=@helper["me.minidigger.hangar.model.Permission"]>
<#assign FlagReason=@helper["me.minidigger.hangar.model.FlagReason"]>
<#macro view p sp active noButtons=false additionalScripts="" additionalStyling="">
    <#-- @ftlvariable name="sp" type="me.minidigger.hangar.model.viewhelpers.ScopedProjectData" -->
    <#assign scriptsVar>
        <script type="text/javascript" src="<@hangar.url "javascripts/projectDetail.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "javascripts/pageEdit.js" />"></script>
        ${additionalScripts}
        <script <#-- @CSPNonce.attr -->>
                var projectOwner = "${p.project.ownerName}";
                var projectSlug = "${p.project.slug}";
                var projectId = "${p.project.pluginId}";
                var alreadyStarred = ${sp.starred?c};
                $(function() { $(".nav").find("${active}").addClass("active"); });
        </script>
    </#assign>

    <#assign metaVar>
        <meta property="og:title" content="${p.project.ownerName} / ${p.project.name}" />
        <meta property="og:type" content="website" />
        <meta property="og:url" content="${routes.getRouteUrl("projects.show", p.project.ownerName, p.project.slug)}" />
        <meta property="og:image" content="${p.iconUrl}" />
        <meta property="og:site_name" content="<@spring.message "general.appName" />" />
        <meta property="og:description" content="${p.project.description!}" />
    </#assign>

    <@base.base title=(p.project.ownerName + " / " + p.project.name) additionalScripts=scriptsVar additionalStyling=additionalStyling additionalMeta=metaVar>
        <div class="project-header-container">
            <#if p.visibility != Visibility.PUBLIC>
                <div class="row">
                    <div class="col-xs-12">
                        <div class="alert alert-danger" role="alert" style="margin: 0.2em 0 0 0">
                            <#if p.visibility == Visibility.NEEDSCHANGES>
                                <#if sp.perms(Permission.EditPage)>
                                    <a class="btn btn-success pull-right" href="${p.fullSlug}/manage/sendforapproval">Send for approval</a>
                                </#if>
                                    <strong><@spring.message "visibility.notice." + p.visibility.getName() /></strong>
                                    <br>
                                    ${p.renderVisibilityChange!"Unknown"}
                            <#else>
                                <#if p.visibility == Visibility.SOFTDELETE>
                                    <@spring.messageArgs "visibility.notice." + p.visibility.getName(), [p.lastVisibilityChangeUser] />
                                <#else>
                                    <@spring.message "visibility.notice." + p.visibility.getName() />
                                    ${p.renderVisibilityChange!}
                                </#if>
                            </#if>
                        </div>
                    </div>
                </div>
            </#if>

            <!-- Header -->
            <div class="row">
                <div class="col-md-6">
                    <div class="project-header">
                        <div class="project-path">
                            <a href="${routes.getRouteUrl("users.showProjects", p.project.ownerName)}">${p.project.ownerName}</a>
                            /
                            <a class="project-name" href="${routes.getRouteUrl("projects.show", p.project.ownerName, p.project.slug)}">${p.project.name}</a>
                        </div>
                        <div>
                            <#if p.project.description??>
                                <i class="minor" title="${p.project.description}">${p.project.description}</i>
                            </#if>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <#if !noButtons>
                        <div class="pull-right project-controls">

                            <#-- TODO
                            @flash.get("reported").map { _ =>
                            <span class="flag-msg">
                                <i class="fas fa-thumbs-up"></i> Flag submitted for review
                            </span>
                            }
                            -->

                            <#if p.visibility != Visibility.SOFTDELETE>
                                <#if headerData.hasUser() && p.isOwner(headerData.getCurrentUser())>
                                    <button class="btn btn-default btn-star">
                                        <i id="icon-star" <#if sp.starred>
                                           class="fas fa-star"
                                           <#else>
                                           class="far fa-star"
                                           </#if>></i>
                                        <span class="starred">${p.starCount}</span>
                                    </button>

                                    <button class="btn btn-watch btn-default <#if sp.watching> watching </#if>">
                                        <i class="fas <#if sp.watching> fa-eye-slash <#else> fa-eye </#if>"></i>
                                        <span class="watch-status"> <#if sp.watching> Unwatch <#else> Watch </#if></span>
                                    </button>
                                <#else>
                                    <span class="minor stars-static">
                                        <i class="fas fa-star"></i> ${p.starCount}
                                    </span>
                                </#if>
                            </#if>

                            <!-- Flag button -->
                            <#if headerData.hasUser() && !p.isOwner(headerData.getCurrentUser())
                                && !sp.uprojectFlags
                                && p.getVisibility() != Visibility.SOFTDELETE>
                                <button data-toggle="modal" data-target="#modal-flag" class="btn btn-default">
                                    <i class="fas fa-flag"></i> <@spring.message "project.flag" />
                                </button>
                                <div class="modal fade" id="modal-flag" tabindex="-1" role="dialog"
                                     aria-labelledby="label-flag">
                                    <div class="modal-dialog" role="document">
                                        <div class="modal-content">
                                            <div class="modal-header">
                                                <button type="button" class="close" data-dismiss="modal"
                                                        aria-label="Close">
                                                    <span aria-hidden="true">&times;</span>
                                                </button>
                                                <h4 class="modal-title" id="label-flag">Flag project</h4>
                                            </div>
                                            <#import "*/utils/form.ftlh" as form>
                                            <@form.form action=routes.getRouteUrl("projects.flag", p.project.ownerName, p.project.slug) method="POST">
                                                <#import "*/utils/csrf.ftlh" as csrf>
                                                <@csrf.formField />
                                                <div class="modal-body">
                                                    <ul class="list-group list-flags">
                                                        <#list FlagReason.values() as reason>
                                                            <#-- @ftlvariable name="reason" type="me.minidigger.hangar.model.FlagReason" -->
                                                            <li class="list-group-item">
                                                                <span>${reason.title}</span>
                                                                <span class="pull-right">
                                                                    <input type="radio" required value="${reason.name()}" name="flag-reason" />
                                                                </span>
                                                            </li>
                                                        </#list>
                                                    </ul>
                                                    <input class="form-control" name="comment" type="text"
                                                           maxlength="255" required="required"
                                                           placeholder="<@spring.message "ph.comment" />&hellip;" />
                                                </div>
                                                <div class="modal-footer">
                                                    <button type="button" class="btn btn-default" data-dismiss="modal">
                                                        Close
                                                    </button>
                                                    <button type="submit" class="btn btn-primary">Flag</button>
                                                </div>
                                            </@form.form>
                                        </div>
                                    </div>
                                </div>
                            </#if>

                            <#if headerData.hasUser() && (headerData.globalPerm(Permission.ModNotesAndFlags) || headerData.globalPerm(Permission.ViewLogs))>
                            <button class="btn btn-alert dropdown-toggle" type="button" id="admin-actions" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                                Admin actions
                                <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu" aria-labelledby="admin-actions">
                                <#if headerData.globalPerm(Permission.ModNotesAndFlags)>
                                    <li><a href="${routes.getRouteUrl("projects.showFlags", p.project.ownerName, p.project.slug)}">
                                        Flag history (${p.flagCount}) </a></li>
                                </#if>
                                <#if headerData.globalPerm(Permission.ModNotesAndFlags)>
                                <li><a href="${routes.getRouteUrl("projects.showNotes", p.project.ownerName, p.project.slug)}">
                                        Staff notes (${p.noteCount}) </a></li>
                                </#if>
                                <#if headerData.globalPerm(Permission.ViewLogs)>
                                <li><a href="${routes.getRouteUrl("showLog", "", "", p.project.pluginId, "", "", "", "")}">
                                        User Action Logs</a></li>
                                </#if>
                                <li><a href="https://papermc.io/forums/${p.project.ownerName}">Owner on forum <i class="fas fa-external-link-alt" aria-hidden="true"></i></a></li>
                            </ul>
                            </#if>
                        </div>
                    </#if>
                </div>
            </div>

                <!-- Nav -->
            <div class="row row-nav">
                <div class="col-md-12">
                    <div class="navbar navbar-default project-navbar pull-left">
                        <div class="navbar-inner">
                            <ul class="nav navbar-nav">
                                <!-- Tabs -->
                                <li id="docs" class="">
                                    <a href="${routes.getRouteUrl("projects.show", p.project.ownerName, p.project.slug)}">
                                        <i class="fas fa-book"></i> <@spring.message "project.docs" /></a>
                                </li>

                                <li id="versions" class="">
                                    <a href="${routes.getRouteUrl("versions.showList", p.project.ownerName, p.project.slug)}">
                                        <i class="fas fa-download"></i> <@spring.message "project.versions" />
                                    </a>
                                </li>

                                <#if p.project.topicId??>
                                    <li id="discussion" class="">
                                        <a href="${routes.getRouteUrl("projects.showDiscussion", p.project.ownerName, p.project.slug)}">
                                            <i class="fas fa-users"></i> <@spring.message "project.discuss" />
                                        </a>
                                    </li>
                                </#if>

                                <#if sp.perms(Permission.EditProjectSettings)>
                                    <#-- Show manager if permitted -->
                                    <li id="settings" class="">
                                        <a href="${routes.getRouteUrl("projects.showSettings", p.project.ownerName, p.project.slug)}">
                                            <i class="fas fa-cog"></i> <@spring.message "project.settings" />
                                        </a>
                                    </li>
                                </#if>

                                <#if p.project.homepage??>
                                    <#assign homepage>${p.project.homepage}</#assign> <#-- move these assigns? -->
                                    <li id="homepage">
                                        <a title="${homepage}" target="_blank" rel="noopener"
                                           href="<@hangar.linkout homepage/>">
                                            <i class="fas fa-home"></i> Homepage <i class="fas fa-external-link-alt"></i></a>
                                    </li>
                                </#if>

                                <#if p.project.issues??>
                                    <#assign issues>${p.project.issues}</#assign>
                                    <li id="issues">
                                        <a title="${issues}" target="_blank" rel="noopener"
                                        href="<@hangar.linkout issues/>">
                                            <i class="fas fa-bug"></i> Issues <i class="fas fa-external-link-alt"></i></a>
                                    </li>
                                </#if>

                                <#if p.project.source??>
                                    <#assign source>${p.project.source}</#assign>
                                    <li id="source">
                                        <a title="${source}" target="_blank" rel="noopener"
                                        href="<@hangar.linkout source/>">
                                            <i class="fas fa-code"></i> Source <i class="fas fa-external-link-alt"></i>
                                        </a>
                                    </li>
                                </#if>

                                <#if p.project.support??>
                                    <#assign support>${p.project.support}</#assign>
                                    <li id="support">
                                        <a title="${support}" target="_blank" rel="noopener"
                                        href="<@hangar.linkout support/>">
                                            <i class="fas fa-question-circle"></i> Support <i class="fas fa-external-link-alt"></i>
                                        </a>
                                    </li>
                                </#if>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <#nested>

    </@base.base>
</#macro>
