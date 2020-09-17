<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro header authEnabled=true>
    <nav id="topbar" class="navbar-main navbar-inverse navbar-expand-lg">
        <div class="container">
            <#-- Left navbar -->
            <div id="sp-logo-container" class="float-left">
                <div class="logo">
                    <a href="${Routes.SHOW_HOME.getRouteUrl()}">
                        <img src="https://papermc.io/images/logo-marker.svg" alt="Paper logo">
                    </a>
                    <i class="fas fa-fw fa-chevron-down"></i>
                </div>

                <div id="sp-logo-menu">
                    <ul id="sp-logo-dropdown">
                        <li><a href="https://www.papermc.io">
                                <i class="fa-fw fas fa-home"></i><@spring.message "general.home" />
                            </a></li>
                        <li><a href="https://papermc.io/forums">
                                <i class="fa-fw fas fa-comments"></i><@spring.message "general.forums" />
                            </a></li>
                        <li><a href="https://github.com/PaperMC">
                                <i class="fa-fw fas fa-code"></i><@spring.message "general.code" />
                            </a></li>
                        <li><a href="https://paper.readthedocs.io">
                                <i class="fa-fw fas fa-book"></i><@spring.message "general.docs" />
                            </a></li>
                        <li><a href="https://papermc.io/javadocs">
                                <i class="fa-fw fas fa-graduation-cap"></i><@spring.message "general.javadocs" />
                            </a></li>
                        <li class="active"><a href="${Routes.SHOW_HOME.getRouteUrl()}">
                                <img src="<@hangar.url "images/ore-nav.svg" />" alt=""
                                     class="fa-fw ore-nav"><@spring.message "general.plugins" />
                                (<@spring.message "general.appName" />)
                            </a></li>
                        <li><a href="https://papermc.io/downloads">
                                <i class="fa-fw fas fa-download"></i><@spring.message "general.getsponge" />
                            </a></li>
                        <li><a href="https://papermc.io/community">
                                <i class="fa-fw fas fa-comment"></i><@spring.message "general.irc" />
                            </a></li>
                    </ul>
                </div>
            </div>

            <#-- Right navbar -->
            <#assign Permission=@helper["io.papermc.hangar.model.Permission"] />
            <#-- @ftlvariable name="Permission" type="io.papermc.hangar.model.Permission" -->
            <#if authEnabled>
                <div class="collapse navbar-collapse float-right">
                    <ul class="nav navbar-nav">
                        <#if cu??>
                            <li class="dropdown nav-icon new-controls">
                                <a href="#" class="drop-down-toggle" data-toggle="dropdown">
                                    <i class="icon fas fa-plus"></i>
                                    <span class="caret"></span>
                                </a>
                                <ul class="user-dropdown dropdown-menu" aria-label="Create new&hellip;">
                                    <li>
                                        <a href="${Routes.PROJECTS_SHOW_CREATOR.getRouteUrl()}">
                                            <i class="fas fa-book fa-fw mr-1"></i>
                                            <span><@spring.message "project.create"/></span>
                                        </a>
                                        <a href="${Routes.ORG_SHOW_CREATOR.getRouteUrl()}">
                                            <i class="fas fa-users fa-fw mr-1"></i>
                                            <span><@spring.message "org.create" /></span>
                                        </a>
                                    </li>
                                </ul>
                            </li>

                            <#assign title><@spring.message "project.viewAuthors"/></#assign>
                            <li class="nav-icon authors-icon" <@tooltip title=title />>
                                <a href="${Routes.USERS_SHOW_AUTHORS.getRouteUrl("", "")}">
                                    <i class="icon fas fa-users"></i>
                                </a>
                            </li>

                            <#if headerData.globalPerm(Permission.IsStaff)>
                                <#assign title><@spring.message "project.viewStaff"/></#assign>
                                <li class="nav-icon staff-icon" <@tooltip title=title />>
                                    <a href="${Routes.USERS_SHOW_STAFF.getRouteUrl("", "")}">
                                        <i class="icon fas fa-user-tie"></i>
                                    </a>
                                </li>
                            </#if>

                        <#-- Show user controls -->
                            <li class="dropdown user-controls nav-icon">
                                <a href="#" class="drop-down-toggle user-toggle" data-toggle="dropdown">
                                    <#if headerData.hasNotice()>
                                        <span class="unread"></span>
                                    </#if>
                                    <img height="32" width="32" class="user-avatar" src="${utils.avatarUrl(cu.name)}" alt="${cu.name}"/>
                                    <span class="caret"></span>
                                </a>
                                <ul class="user-dropdown dropdown-menu"
                                    aria-label="<@spring.messageArgs code="aria.dropdown.menu" args=[1] />">
                                    <li>
                                        <a href="${Routes.USERS_SHOW_PROJECTS.getRouteUrl("")}${cu.name}">
                                            <i class="fas fa-user mr-1"></i>
                                            <span>${cu.name}</span>
                                        </a>
                                    </li>

                                    <li>
                                        <a href="${Routes.USERS_SHOW_NOTIFICATIONS.getRouteUrl("", "")}">
                                            <i class="fas fa-bell mr-1"></i>
                                            <span>
                                        <@spring.message "notification.plural" />
                                                <#if headerData.hasUnreadNotifications()>
                                                    <span class="unread"></span>
                                                </#if>
                                    </span>
                                        </a>
                                    </li>
                                    <#if headerData.globalPerm(Permission.ModNotesAndFlags)>
                                        <li>
                                            <a href="${Routes.SHOW_FLAGS.getRouteUrl()}">
                                                <i class="fas fa-flag mr-1"></i>
                                                <span>
                                                <@spring.message "project.flag.plural" />
                                                    <#if headerData.hasUnresolvedFlags()>
                                                        <span class="unread"></span>
                                                    </#if>
                                            </span>
                                            </a>
                                        </li>
                                        <li>
                                            <a href="${Routes.SHOW_PROJECT_VISIBILITY.getRouteUrl()}">
                                                <i class="fas fa-thumbs-up mr-1"></i>
                                                <span>
                                                Project approvals
                                               <#if headerData.hasProjectApprovals()>
                                                   <span class="unread"></span>
                                               </#if>
                                            </span>
                                            </a>
                                        </li>
                                    </#if>
                                    <#if headerData.globalPerm(Permission.Reviewer)>
                                        <li>
                                            <a href="${Routes.SHOW_QUEUE.getRouteUrl()}">
                                                <i class="far fa-thumbs-up mr-1"></i>
                                                <span>
                                                <@spring.message "user.queue" />
                                                    <#if headerData.hasReviewQueue()>
                                                        <span class="unread"></span>
                                                    </#if>
                                            </span>
                                            </a>
                                        </li>
                                    </#if>
                                    <#if headerData.globalPerm(Permission.ViewStats)>
                                        <li>
                                            <a href="${Routes.SHOW_STATS.getRouteUrl("", "")}">
                                                <i class="fas fa-chart-area mr-1"></i>
                                                <span><@spring.message "user.stats" /></span>
                                            </a>
                                        </li>
                                    </#if>
                                    <#if headerData.globalPerm(Permission.ViewHealth)>
                                        <li>
                                            <a href="${Routes.SHOW_HEALTH.getRouteUrl()}">
                                                <i class="fas fa-heartbeat mr-1"></i>
                                                <span><@spring.message "admin.health.title" /></span>
                                            </a>
                                        </li>
                                    </#if>
                                    <#if headerData.globalPerm(Permission.ViewLogs)>
                                        <li>
                                            <a href="${Routes.SHOW_LOG.getRouteUrl("", "", "", "", "", "", "")}">
                                                <i class="fas fa-list mr-1"></i>
                                                <span><@spring.message "admin.log.title" /></span>
                                            </a>
                                        </li>
                                    </#if>
                                    <#if headerData.globalPerm(Permission.ManualValueChanges)>
                                        <li>
                                            <a href="${Routes.SHOW_PLATFORM_VERSIONS.getRouteUrl()}">
                                                <i class="fas fa-tags mr-1"></i>
                                                <span><@spring.message "admin.platformVersions.title" /></span>
                                            </a>
                                        </li>
                                    </#if>

                                    <li role="separator" class="divider"></li>
                                    <li>
                                        <a href="${Routes.USERS_LOGOUT.getRouteUrl()}">
                                            <i class="fas fa-sign-out-alt mr-1"></i>
                                            <span><@spring.message "general.signout" /></span>
                                        </a>
                                    </li>
                                </ul>
                            </li>
                        <#else>
                            <li>
                                <div class="btn-group-login">
                                    <a href="${Routes.USERS_SIGN_UP.getRouteUrl()}"
                                       class="btn btn-primary navbar-btn"><@spring.message "general.signup" /></a>
                                    <a href="${Routes.USERS_LOGIN.getRouteUrl("", "", rc.requestUri?url)}"
                                       class="btn btn-primary navbar-btn"><@spring.message "general.login" /></a>
                                </div>
                            </li>
                        </#if>
                    </ul>
                </div>
            </#if>
        </div>
    </nav>
</#macro>

<#macro tooltip title>
    data-toggle="tooltip" data-placement="bottom" title="${title}"
</#macro>
