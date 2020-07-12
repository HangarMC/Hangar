<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#--
@(u: UserData, o: Option[(OrganizationData, ScopedOrganizationData)], additionalScripts: Html = Html(""))(content: Html)(implicit messages: Messages, flash: Flash, request: OreRequest[_], config: OreConfig, assetsFinder: AssetsFinder)
-->

@canEditOrgSettings = @{
u.isOrga &&
o.exists { case (_, scopedData) =>
scopedData.permissions.has(Permission.EditOrganizationSettings)
}
}

<#assign scriptsVar>
    <script type="text/javascript" src="<@spring.url "javascripts/userPage.js" />"></script>
    <script <#-- @CSPNonce.attr -->>
            window.USERNAME ='${u.user.name}';
            @for(category <- Category.values) {
                CATEGORY_TITLE['${category.apiName}'] = '${category.title}';
                CATEGORY_ICON['${category.apiName}'] = '${category.icon}';
            }
            NO_ACTION_MESSAGE.starred = '<@spring.messageArgs "user.noStars" u.user.name />';
            NO_ACTION_MESSAGE.watching = '<@spring.messageArgs "user.noWatching" u.user.name />';
    </script>
</#assign>

<@base.base title=u.user.name additionalScripts=scriptsVar>
    <!-- Updated by JS -->
    <div class="alert alert-success alert-dismissable" role="alert" style="display: none;">
        <button type="button" class="close" data-dismiss="alert" aria-label="<@spring.message "general.close" />">
            <span aria-hidden="true">&times;</span>
        </button>
        <strong>Success!</strong> <span class="success"></span>
    </div>

        <!-- Header -->
    <div class="row user-header">
        <div class="header-body">
                <!-- Title -->
            <span class="user-badge">
                @userAvatar(
                    userName = u.user.name,
                    avatarUrl = u.user.avatarUrl,
                    clazz = "user-avatar-md" + (if (canEditOrgSettings) " organization-avatar" else ""))

                <#if canEditOrgSettings>
                    <div class="edit-avatar" style="display: none;">
                        <a href="${routes.Organizations.updateAvatar(u.user.name)}"><i class="fas fa-edit"></i> <@spring.message "user.editAvatar" /></a>
                    </div>

                    <#if !u.currentUser.get.readPrompts.contains(Prompt.ChangeAvatar)>
                        @prompt(Prompt.ChangeAvatar, "popover-avatar")
                    </#if>
                </#if>

                <span class="user-title">
                    <h1 class="username">
                        @u.user.name

                        <#if u.isCurrent && !u.isOrga>
                            <a class="user-settings" href="@config.security.api.url/accounts/settings">
                                <i class="fas fa-cog" data-toggle="tooltip"
                                data-placement="top" title="Settings"></i>
                            </a>

                            <span data-toggle="modal" data-target="#modal-lock">
                                <i class="fas @{if (u.user.isLocked) "fa-lock" else "fa-unlock-alt"} action-lock-account" data-toggle="tooltip"
                                data-placement="top" title="<@spring.message "user.lock" />"></i>
                            </span>

                            <a class="action-api" href="@routes.Users.editApiKeys(u.user.name)">
                                <i class="fas fa-key" data-toggle="tooltip" data-placement="top" title="API Keys"></i>
                            </a>
                        </#if>

                        <#if u.hasUser>
                            <#if u.userPerm.has(Permission.ModNotesAndFlags) || u.userPerm.has(Permission.Reviewer)>
                                <a class="user-settings" href="@routes.Application.showActivities(u.user.name)">
                                    <i class="fas fa-calendar" data-toggle="tooltip"
                                    data-placement="top" title="Activity"></i>
                                </a>
                            </#if>
                        </#if>

                        <#if request.headerData.globalPerm(Permission.EditAllUserSettings)>
                            <a class="user-settings" href="@routes.Application.userAdmin(u.user.name)">
                                <i class="fas fa-wrench" data-toggle="tooltip"
                                data-placement="top" title="User Admin"></i>
                            </a>
                        </#if>
                    </h1>

                    <div class="user-tag">
                        <i class="minor">
                        <#if u.user.tagline.isDefined>
                            @u.user.tagline.get
                        <#elseif u.isCurrent || canEditOrgSettings>
                            Add a tagline
                        </#if>
                        </i>

                        <#if u.isCurrent || canEditOrgSettings>
                            <a href="#" data-toggle="modal" data-target="#modal-tagline">
                                <i class="fas fa-edit"></i>
                            </a>
                        </#if>
                    </div>
                </span>
            </span>

            <!-- Roles -->
            <ul class="user-roles">
            @defining(u.globalRoles.toSeq.sortBy(_.permissions: Long).lastOption) { roleOpt =>
                @roleOpt.map { role =>
                    <li class="user-role channel" style="background-color: @role.color.hex">@role.title</li>
                }
            }
            </ul>

            <div class="user-info">
                <i class="minor">@u.projectCount&nbsp;@if(u.projectCount == 1){project}else{projects}</i><br/>
                <i class="minor">
                @messages(
                    "user.memberSince",
                    u.user.joinDate.map(prettifyDate).getOrElse(prettifyDate(u.user.createdAt)))
                </i><br/>
                <a href="https://papermc.io/forums/users/${u.user.name}">
                    <@spring.message "user.viewOnForums" /> <i class="fas fa-external-link-alt"></i>
                </a>
            </div>
        </div>
    </div>

    @content

    @modal("modal-lock", "label-lock", if (u.user.isLocked) "user.unlock" else "user.lock") {
        <div class="modal-body">
            <p class="minor">
                <#if u.user.isLocked>
                    <@spring.message "user.unlock.confirm" />
                <#else>
                    <@spring.message "user.lock.confirm" />
                </#if>
            </p>
        </div>

        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal"><@spring.message "general.close" /></button>
            @form(action = routes.Users.verify(Some(routes.Users.setLocked(
                user = u.user.name,
                locked = !u.user.isLocked,
                sso = None,
                sig = None
            ).path())), Symbol("class") -> "form-inline") {
                @CSRF.formField
                <button type="submit" class="btn btn-primary"><@spring.message "general.continue" /></button>
            }
        </div>
    }

    @modal("modal-tagline", "label-tagline", "user.tagline.edit") {
        @form(action = routes.Users.saveTagline(u.user.name)) {
            @CSRF.formField
            <div class="modal-body">
                <div class="setting setting-no-border">
                    <div class="setting-description">
                        <h4><@spring.message "user.tagline" /></h4>
                        <p><@spring.message "user.tagline.info" /></p>
                    </div>
                    <input class="form-control" type="text" value="@u.user.tagline.getOrElse("")" id="tagline"
                           name="tagline" maxlength="@config.ore.users.maxTaglineLen" />
                </div>
                <div class="clearfix"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">
                    <@spring.message "general.close" />
                </button>
                <input type="submit" value="<@spring.message "general.save" />" class="btn btn-primary" />
            </div>
        }
    }
</@base.base>
