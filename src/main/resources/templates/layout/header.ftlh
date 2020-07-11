<#--
@(authEnabled: Boolean = true)(implicit messages: Messages, request: OreRequest[_], config: OreConfig, assetsFinder: AssetsFinder)
-->

<#macro header authEnabled=true>
    <nav id="topbar" class="navbar-main navbar-inverse">
        <div class="container">
            <#-- Left navbar -->
            <div id="sp-logo-container">
                <a class="logo" href="${@routes.Application.showHome()}">
                    <img src="https://papermc.io/images/logo-marker.svg" alt="Paper logo">
                    <i class="fas fa-fw fa-chevron-down"></i>
                </a>

                <div id="sp-logo-menu">
                    <ul id="sp-logo-dropdown">
                        <li><a href="https://www.papermc.io">
                                <i class="fa-fw fas fa-home"></i>${@messages("general.home")}
                            </a></li>
                        <li><a href="https://papermc.io/forums">
                                <i class="fa-fw fas fa-comments"></i>${@messages("general.forums")}
                            </a></li>
                        <li><a href="https://github.com/PaperMC">
                                <i class="fa-fw fas fa-code"></i>${@messages("general.code")}
                            </a></li>
                        <li><a href="https://paper.readthedocs.io">
                                <i class="fa-fw fas fa-book"></i>${@messages("general.docs")}
                            </a></li>
                        <li><a href="https://papermc.io/javadocs">
                                <i class="fa-fw fas fa-graduation-cap"></i>${@messages("general.javadocs")}
                            </a></li>
                        <li class="active"><a href="${@routes.Application.showHome()}">
                                <img src="${@assetsFinder.path("images/ore-nav.svg")}" alt=""
                                     class="fa-fw ore-nav">${@messages("general.plugins")}
                                (${@messages("general.appName")})
                            </a></li>
                        <li><a href="https://papermc.io/downloads">
                                <i class="fa-fw fas fa-download"></i>${@messages("general.getsponge")}
                            </a></li>
                        <li><a href="https://papermc.io/community">
                                <i class="fa-fw fas fa-comment"></i>${@messages("general.irc")}
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
                                    <a href="${@projectRoutes.Projects.showCreator()}">
                                        <i class="fas fa-book fa-fw mr-1"></i>
                                        <span>${}@messages("project.create")}</span>
                                    </a>
                                    <a href="${@routes.Organizations.showCreator()}">
                                        <i class="fas fa-users fa-fw mr-1"></i>
                                        <span>${@messages("org.create")}</span>
                                    </a>
                                </li>
                            </ul>
                        </li>

                        <li class="nav-icon authors-icon" <@tooltip title=@messages("project.viewAuthors")/>>
                            <a href="${@routes.Users.showAuthors(None, None)}">
                                <i class="icon fas fa-users"></i>
                            </a>
                        </li>

                        <#if request.headerData.globalPerm(Permission.IsStaff)>
                            <li class="nav-icon staff-icon" <@tooltip title=@messages("project.viewStaff")/>>
                                <a href="${@routes.Users.showStaff(None, None)}">
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
                            <ul class="user-dropdown dropdown-menu" aria-label="${@messages("aria.dropdown.menu", 1)}">
                                <li>
                                    <a href="${@routes.Users.showProjects(cu.name)}">
                                        <i class="fas fa-user mr-1"></i>
                                        <span>${cu.name}</span>
                                    </a>
                                </li>

                                <li>
                                    <a href="${@routes.Users.showNotifications(None, None)}">
                                        <i class="fas fa-bell mr-1"></i>
                                        <span>${@messages("notification.plural")}
                                            <#if request.headerData.hasUnreadNotifications>
                                                <span class="unread"></span>
                                            </#if>
                                </span>
                                    </a>
                                </li>

                                <#if request.headerData.globalPerm(Permission.ModNotesAndFlags)>
                                    <li>
                                        <a href="${@routes.Application.showFlags()}">
                                            <i class="fas fa-flag mr-1"></i>
                                            <span>${@messages("project.flag.plural")}
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
                                            <span>${@messages("user.queue")}
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
                                            <span>${@messages("user.stats")}
                                    </span>
                                        </a>
                                    </li>
                                </#if>

                                <#if request.headerData.globalPerm(Permission.ViewHealth)>
                                    <li>
                                        <a href="${@routes.Application.showHealth()}">
                                            <i class="fas fa-heartbeat mr-1"></i>
                                            <span>${@messages("admin.health.title")}
                                    </span>
                                        </a>
                                    </li>
                                </#if>

                                <#if request.headerData.globalPerm(Permission.ViewLogs)>
                                    <li>
                                        <a href="${@routes.Application.showLog(None, None, None, None, None, None, None)}">
                                            <i class="fas fa-list mr-1"></i>
                                            <span>${@messages("admin.log.title")}
                                    </span>
                                        </a>
                                    </li>
                                </#if>

                                <li role="separator" class="divider"></li>
                                <li>
                                    <a href="${@routes.Users.logOut()}">
                                        <i class="fas fa-sign-out-alt mr-1"></i>
                                        <span>${@messages("general.signout")}</span>
                                    </a>
                                </li>
                            </ul>
                        </li>
                    <#else>
                        <li>
                            <div class="btn-group-login">
                                <a href="${@routes.Users.signUp()}"
                                   class="btn btn-primary navbar-btn">${@messages("general.signup")}</a>
                                <a href="@routes.Users.logIn(None, None, Some(request.path))"
                                   class="btn btn-primary navbar-btn">${@messages("general.login")}</a>
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
