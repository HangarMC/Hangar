<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base>

<#--
Base template for Project overview.
-->
<#assign Visibility=@helper["io.papermc.hangar.model.Visibility"]>
<#assign Permission=@helper["io.papermc.hangar.model.Permission"]>
<#assign FlagReason=@helper["io.papermc.hangar.model.FlagReason"]>
<#-- @ftlvariable name="FlagReason" type="io.papermc.hangar.model.FlagReason" -->
<#macro view p sp active noButtons=false additionalScripts="" additionalStyling="">
<#-- @ftlvariable name="p" type="io.papermc.hangar.model.viewhelpers.ProjectData" -->
<#-- @ftlvariable name="sp" type="io.papermc.hangar.model.viewhelpers.ScopedProjectData" -->
    <#assign scriptsVar>
        <script <#-- @CSPNonce.attr -->>
            window.PROJECT = ${mapper.valueToTree(p.project)};
            window.CAN_EDIT_PAGES = ${sp.perms(Permission.EditPage)?c};


            window.PROJECT_OWNER = "${p.project.ownerName}";
            window.PROJECT_SLUG = "${p.project.slug}";
            window.NAMESPACE = "${p.project.ownerName}/${p.project.slug}";
            window.ALREADY_STARRED = ${sp.starred?c};

            window.ACTIVE_NAV = "${active}";
        </script>
        <script type="text/javascript" src="<@hangar.url "js/project-controls.js" />"></script>
        ${additionalScripts}
    </#assign>

    <#assign styleVar>
        <link rel="stylesheet" href="<@hangar.url "css/project-controls.css" />">
        ${additionalStyling}
    </#assign>

    <#assign metaVar>
        <meta property="og:title" content="${p.project.ownerName}/${p.project.name}"/>
        <meta property="og:type" content="website"/>
        <meta property="og:url" content="${Routes.PROJECTS_SHOW.getRouteUrl(p.project.ownerName, p.project.slug)}"/>
        <meta property="og:image" content="${p.iconUrl}"/>
        <meta property="og:site_name" content="<@spring.message "general.appName" />"/>
        <meta property="og:description" content="${p.project.description!}"/>
        <meta name="description" content="${p.project.description!}" />
    </#assign>

    <@base.base title=(p.project.ownerName + " / " + p.project.name) additionalScripts=scriptsVar additionalStyling=styleVar additionalMeta=metaVar>
        <div class="project-header-container">
            <#if p.visibility != Visibility.PUBLIC>
                <div class="row">
                    <div class="col-12">
                        <div class="alert alert-danger" role="alert" style="margin: 0.2em 0 0 0">
                            <#if p.visibility == Visibility.NEEDSCHANGES>
                                <#if sp.perms(Permission.EditPage)>
                                    <a class="btn btn-success float-right" href="/${p.fullSlug}/manage/sendforapproval">Send
                                        for approval</a>
                                </#if>
                                <strong><@spring.message "visibility.notice." + p.visibility.getName() /></strong>
                                <br>
                                ${p.renderVisibilityChange(markdownService, "Unknown")}
                            <#else>
                                <#if p.visibility == Visibility.SOFTDELETE>
                                    <@spring.messageArgs code="visibility.notice." + p.visibility.getName() args=[p.lastVisibilityChangeUser] />
                                <#else>
                                    <@spring.message "visibility.notice." + p.visibility.getName() />
                                    ${p.renderVisibilityChange(markdownService, "")}
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
                            <a href="${Routes.USERS_SHOW_PROJECTS.getRouteUrl(p.project.ownerName)}">${p.project.ownerName}</a>
                            /
                            <a class="project-name"
                               href="${Routes.PROJECTS_SHOW.getRouteUrl(p.project.ownerName, p.project.slug)}">${p.project.name}</a>
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
                        <div
                                id="project-controls"
                                data-visibility="${p.visibility.getName()}"
                                data-has-user="${headerData.hasUser()?c}"
                                data-is-owner="${p.isOwner(headerData.getCurrentUser())?c}"
                                data-is-starred="${sp.starred?c}"
                                data-is-watching="${sp.watching?c}"
                                data-has-user-flags="${sp.uprojectFlags?c}"
                                data-star-count="${p.starCount?c}"
                                data-flag-count="${p.flagCount?c}"
                                data-note-count="${p.noteCount?c}"
                                data-has-mod-notes="${headerData.globalPerm(Permission.ModNotesAndFlags)?c}"
                                data-has-view-logs="${headerData.globalPerm(Permission.ViewLogs)?c}"
                                data-flag-reported="${false?c}" <#--TODO flasing on flag reporting -->
                        ></div>
                        <div class="modal fade" id="modal-flag" tabindex="-1" role="dialog"
                             aria-labelledby="label-flag">
                            <div class="modal-dialog" role="document">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h4 class="modal-title" id="label-flag">Flag project</h4>
                                        <button type="button" class="close" data-dismiss="modal"
                                                aria-label="Close">
                                            <span aria-hidden="true">&times;</span>
                                        </button>
                                    </div>
                                    <#import "*/utils/form.ftlh" as form>
                                    <@form.form action=Routes.PROJECTS_FLAG.getRouteUrl(p.project.ownerName, p.project.slug) method="POST">
                                        <#import "*/utils/csrf.ftlh" as csrf>
                                        <@csrf.formField />
                                        <div class="modal-body">
                                            <ul id="list-flags" class="list-group list-flags">
                                                <#list FlagReason.getValues() as reason>
                                                    <#-- @ftlvariable name="reason" type="io.papermc.hangar.model.FlagReason" -->
                                                    <li class="list-group-item">
                                                        <span>${reason.title}</span>
                                                        <span class="float-right">
                                                                    <input type="radio" required
                                                                           value="${reason.name()}" name="flag-reason"/>
                                                                </span>
                                                    </li>
                                                </#list>
                                            </ul>
                                            <input class="form-control" name="comment" type="text"
                                                   maxlength="255" required="required"
                                                   placeholder="<@spring.message "ph.comment" />&hellip;"/>
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
                </div>
            </div>

            <!-- Nav -->
            <div class="row row-nav">
                <div class="col-md-12">
                    <div class="navbar navbar-light project-navbar float-left navbar-expand-md">
                        <div class="navbar-inner">
                            <ul class="nav navbar-nav">
                                <!-- Tabs -->
                                <li id="docs" class="nav-item <#if active='#docs'>active</#if>">
                                    <a href="${Routes.PROJECTS_SHOW.getRouteUrl(p.project.ownerName, p.project.slug)}"
                                       class="nav-link">
                                        <i class="fas fa-book"></i> <@spring.message "project.docs" /></a>
                                </li>

                                <li id="versions" class="nav-item <#if active='#versions'>active</#if>">
                                    <a href="${Routes.VERSIONS_SHOW_LIST.getRouteUrl(p.project.ownerName, p.project.slug)}"
                                       class="nav-link">
                                        <i class="fas fa-download"></i> <@spring.message "project.versions" />
                                    </a>
                                </li>

                                <#if p.project.topicId??>
                                    <li id="discussion" class="nav-item <#if active='#discussion'>active</#if>">
                                        <a href="${Routes.PROJECTS_SHOW_DISCUSSION.getRouteUrl(p.project.ownerName, p.project.slug)}"
                                           class="nav-link">
                                            <i class="fas fa-users"></i> <@spring.message "project.discuss" />
                                        </a>
                                    </li>
                                </#if>

                                <#if sp.perms(Permission.EditProjectSettings)>
                                <#-- Show manager if permitted -->
                                    <li id="settings" class="nav-item <#if active='#settings'>active</#if>">
                                        <a href="${Routes.PROJECTS_SHOW_SETTINGS.getRouteUrl(p.project.ownerName, p.project.slug)}"
                                           class="nav-link">
                                            <i class="fas fa-cog"></i> <@spring.message "project.settings" />
                                        </a>
                                    </li>
                                </#if>

                                <#if p.project.homepage??>
                                    <#assign homepage>${p.project.homepage}</#assign> <#-- move these assigns? -->
                                    <li id="homepage" class="nav-item">
                                        <a title="${homepage}" target="_blank" rel="noopener"
                                           href="<@hangar.linkout homepage/>" class="nav-link">
                                            <i class="fas fa-home"></i> Homepage <i
                                                    class="fas fa-external-link-alt"></i></a>
                                    </li>
                                </#if>

                                <#if p.project.issues??>
                                    <#assign issues>${p.project.issues}</#assign>
                                    <li id="issues" class="nav-item">
                                        <a title="${issues}" target="_blank" rel="noopener"
                                           href="<@hangar.linkout issues/>" class="nav-link">
                                            <i class="fas fa-bug"></i> Issues <i
                                                    class="fas fa-external-link-alt"></i></a>
                                    </li>
                                </#if>

                                <#if p.project.source??>
                                    <#assign source>${p.project.source}</#assign>
                                    <li id="source" class="nav-item">
                                        <a title="${source}" target="_blank" rel="noopener"
                                           href="<@hangar.linkout source/>" class="nav-link">
                                            <i class="fas fa-code"></i> Source <i class="fas fa-external-link-alt"></i>
                                        </a>
                                    </li>
                                </#if>

                                <#if p.project.support??>
                                    <#assign support>${p.project.support}</#assign>
                                    <li id="support" class="nav-item">
                                        <a title="${support}" target="_blank" rel="noopener"
                                           href="<@hangar.linkout support/>" class="nav-link">
                                            <i class="fas fa-question-circle"></i> Support <i
                                                    class="fas fa-external-link-alt"></i>
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
