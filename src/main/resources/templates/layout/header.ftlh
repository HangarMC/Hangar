<#import "/spring.ftl" as spring />

<#macro header authEnabled=true>
    <nav id="topbar" class="navbar-main navbar-inverse">
        <div class="container">
            <#-- Left navbar -->
            <div id="sp-logo-container">
                <a class="logo" href="<@spring.message "routes.showHome" />">
                    <img src="https://papermc.io/images/logo-marker.svg" alt="Paper logo">
                    <i class="fas fa-fw fa-chevron-down"></i>
                </a>

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
                        <li class="active"><a href="<@spring.message "routes.showHome" />">
                                <img src="<@spring.url "images/ore-nav.svg" />" alt=""
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
            <#if authEnabled>
                <ul class="nav navbar-nav navbar-collapse collapse navbar-right">
                    <#if request.headerData.currentUser??>
                        <#assign cu=request.headerData.currentUser>
                        <li class="dropdown nav-icon new-controls">
                            <a href="#" class="drop-down-toggle" data-toggle="dropdown">
                                <i class="icon fas fa-plus"></i>
                                <span class="caret"></span>
                            </a>
                            <ul class="user-dropdown dropdown-menu" aria-label="Create new&hellip;">
                                <li>
                                    <a href="<@spring.message "routes.projects.showCreator" />">
                                        <i class="fas fa-book fa-fw mr-1"></i>
                                        <span><@spring.message "project.create"/></span>
                                    </a>
                                    <a href="<@spring.message "routes.org.showCreator" />">
                                        <i class="fas fa-users fa-fw mr-1"></i>
                                        <span><@spring.message "org.create" /></span>
                                    </a>
                                </li>
                            </ul>
                        </li>

                        <#assign title><@spring.message "project.viewAuthors"/></#assign>
                        <li class="nav-icon authors-icon" <@tooltip title=title />>
                            <a href="<@spring.message "routes.users.showAuthors" />">
                                <i class="icon fas fa-users"></i>
                            </a>
                        </li>

                        <#if request.headerData.globalPerm(Permission.IsStaff)>
                            <#assign title><@spring.message "project.viewStaff"/></#assign>
                            <li class="nav-icon staff-icon" <@tooltip title=title />>
                                <a href="<@spring.message "routes.users.showStaff" />">
                                    <i class="icon fas fa-user-tie"></i>
                                </a>
                            </li>
                        </#if>

                    <#-- Show user controls -->
                        <li class="dropdown user-controls nav-icon">
                            <a href="#" class="drop-down-toggle user-toggle" data-toggle="dropdown">
                                <#if request.headerData.hasNotice>
                                    <span class="unread"></span>
                                </#if>
                                <img height="32" width="32" class="user-avatar" src="${cu.avatarUrl}" alt="@cu.name"/>
                                <span class="caret"></span>
                            </a>
                            <ul class="user-dropdown dropdown-menu" aria-label="<@spring.messageArgs "aria.dropdown.menu", 1 />">
                                <li>
                                    <a href="<@spring.message "routes.users.showProjects" />${cu.name}">
                                        <i class="fas fa-user mr-1"></i>
                                        <span>${cu.name}</span>
                                    </a>
                                </li>

                                <li>
                                    <a href="<@spring.message "routes.users.showNotifications" />">
                                        <i class="fas fa-bell mr-1"></i>
                                        <span><@spring.message "notification.plural" />
                                            <#if request.headerData.hasUnreadNotifications>
                                                <span class="unread"></span>
                                            </#if>
                                </span>
                                    </a>
                                </li>

                                <#if request.headerData.globalPerm(Permission.ModNotesAndFlags)>
                                    <li>
                                        <a href="<@spring.message "routes.showFlags" />">
                                            <i class="fas fa-flag mr-1"></i>
                                            <span><@spring.message "project.flag.plural" />
                                                <#if request.headerData.unresolvedFlags>
                                                    <span class="unread"></span>
                                                </#if>
                                    </span>
                                        </a>
                                    </li>
                                </#if>

                                <#if request.headerData.globalPerm(Permission.ModNotesAndFlags)>
                                    <li>
                                        <a href="${@routes.Application.showProjectVisibility()}">
                                            <i class="fas fa-thumbs-up mr-1"></i>
                                            <span>
                                        Project approvals
                                       <#if request.headerData.hasProjectApprovals>
                                           <span class="unread"></span>
                                       </#if>
                                    </span>
                                        </a>
                                    </li>
                                </#if>

                                <#if request.headerData.globalPerm(Permission.Reviewer)>
                                    <li>
                                        <a href="${@routes.Application.showQueue()}">
                                            <i class="far fa-thumbs-up mr-1"></i>
                                            <span><@spring.message "user.queue" />
                                                <#if request.headerData.hasReviewQueue>
                                                    <span class="unread"></span>
                                                </#if>
                                    </span>
                                        </a>
                                    </li>
                                </#if>

                                <#if request.headerData.globalPerm(Permission.ViewStats)>
                                    <li>
                                        <a href="${@routes.Application.showStats(None, None)}">
                                            <i class="fas fa-chart-area mr-1"></i>
                                            <span><@spring.message "user.stats" />
                                    </span>
                                        </a>
                                    </li>
                                </#if>

                                <#if request.headerData.globalPerm(Permission.ViewHealth)>
                                    <li>
                                        <a href="${@routes.Application.showHealth()}">
                                            <i class="fas fa-heartbeat mr-1"></i>
                                            <span><@spring.message "admin.health.title" />
                                    </span>
                                        </a>
                                    </li>
                                </#if>

                                <#if request.headerData.globalPerm(Permission.ViewLogs)>
                                    <li>
                                        <a href="${@routes.Application.showLog(None, None, None, None, None, None, None)}">
                                            <i class="fas fa-list mr-1"></i>
                                            <span><@spring.message "admin.log.title" />
                                    </span>
                                        </a>
                                    </li>
                                </#if>

                                <li role="separator" class="divider"></li>
                                <li>
                                    <a href="${@routes.Users.logOut()}">
                                        <i class="fas fa-sign-out-alt mr-1"></i>
                                        <span><@spring.message "general.signout" /></span>
                                    </a>
                                </li>
                            </ul>
                        </li>
                    <#else>
                        <li>
                            <div class="btn-group-login">
                                <a href="${@routes.Users.signUp()}"
                                   class="btn btn-primary navbar-btn"><@spring.message "general.signup" /></a>
                                <a href="@routes.Users.logIn(None, None, Some(request.path))"
                                   class="btn btn-primary navbar-btn"><@spring.message "general.login" /></a>
                            </div>
                        </li>
                    </#if>

                </ul>
            </#if>
        </div>
    </nav>
</#macro>

<#macro tooltip title>
    data-toggle="tooltip" data-placement="bottom" title="${title}"
</#macro>
