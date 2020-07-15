<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />
<#import "*/utils/modal.ftlh" as modal />
<#import "*/utils/prompt.ftlh" as prompt />
<#import "*/utils/userAvatar.ftlh" as userAvatar />
<#import "*/utils/form.ftlh" as form>
<#import "*/utils/csrf.ftlh" as csrf>

<#--
@(u: UserData, o: Option[(OrganizationData, ScopedOrganizationData)], additionalScripts: Html = Html(""))(content: Html)(implicit messages: Messages, flash: Flash, request: OreRequest[_], config: OreConfig, assetsFinder: AssetsFinder)
-->

<#macro view u o additionalScripts="">
    <#--@canEditOrgSettings = @{
    u.isOrga &&
    o.exists { case (_, scopedData) =>
    scopedData.permissions.has(Permission.EditOrganizationSettings)
    }
    }-->

    <#assign scriptsVar>
        <script type="text/javascript" src="<@hangar.url "javascripts/userPage.js" />"></script>
        <script <#-- @CSPNonce.attr -->>
                window.USERNAME ='${u.user.name}';
                <#list Category.values as category> <#-- todo -->
                    CATEGORY_TITLE['${category.apiName}'] = '${category.title}';
                    CATEGORY_ICON['${category.apiName}'] = '${category.icon}';
                </#list>
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
                    <#assign avatarClass>
                        user-avatar-md <#if canEditOrgSettings>organization-avatar</#if>
                    </#assign>
                    <@userAvatar.userAvatar userName=u.user.name avatarUrl=u.user.avatarUrl clazz=avatarClass />

                    <#if canEditOrgSettings>
                        <div class="edit-avatar" style="display: none;">
                            <a href="${routes.getRouteUrl("org.updateAvatar", u.user.name)}"><i class="fas fa-edit"></i> <@spring.message "user.editAvatar" /></a>
                        </div>

                        <#if !u.currentUser.readPrompts.contains(Prompt.ChangeAvatar)>
                            <@prompt.prompt prompt=Prompt.ChangeAvatar id="popover-avatar" />
                        </#if>
                    </#if>

                    <span class="user-title">
                        <h1 class="username">
                            ${u.user.name}

                            <#if u.isCurrent && !u.isOrga>
                                <a class="user-settings" href="${config.security.api.url}/accounts/settings">
                                    <i class="fas fa-cog" data-toggle="tooltip"
                                    data-placement="top" title="Settings"></i>
                                </a>

                                <span data-toggle="modal" data-target="#modal-lock">
                                    <i class="fas @{if (u.user.isLocked) "fa-lock" else "fa-unlock-alt"} action-lock-account" data-toggle="tooltip"
                                    data-placement="top" title="<@spring.message "user.lock" />"></i>
                                </span>

                                <a class="action-api" href="${routes.getRouteUrl("users.editApiKeys", u.user.name)}">
                                    <i class="fas fa-key" data-toggle="tooltip" data-placement="top" title="API Keys"></i>
                                </a>
                            </#if>

                            <#if u.hasUser>
                                <#if u.userPerm.has(Permission.ModNotesAndFlags) || u.userPerm.has(Permission.Reviewer)>
                                    <a class="user-settings" href="${routes.getRouteUrl("showActivities", u.user.name)}">
                                        <i class="fas fa-calendar" data-toggle="tooltip"
                                        data-placement="top" title="Activity"></i>
                                    </a>
                                </#if>
                            </#if>

                            <#if request.headerData.globalPerm(Permission.EditAllUserSettings)>
                                <a class="user-settings" href="${routes.getRouteUrl("userAdmin", u.user.name)}">
                                    <i class="fas fa-wrench" data-toggle="tooltip"
                                    data-placement="top" title="User Admin"></i>
                                </a>
                            </#if>
                        </h1>

                        <div class="user-tag">
                            <i class="minor">
                            <#if u.user.tagline??>
                                ${u.user.tagline.get}
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
                        <li class="user-role channel" style="background-color: ${role.color.hex}">${role.title}</li>
                    }
                }
                </ul>

                <div class="user-info">
                    <i class="minor">${u.projectCount}&nbsp;<#if u.projectCount == 1>project<#else>projects</#if></i><br/>
                    <i class="minor">
                    <@spring.messageArgs "user.memberSince" u.user.joinDate.map(prettifyDate)!prettifyDate(u.user.createdAt) />
                    </i><br/>
                    <a href="https://papermc.io/forums/users/${u.user.name}">
                        <@spring.message "user.viewOnForums" /> <i class="fas fa-external-link-alt"></i>
                    </a>
                </div>
            </div>
        </div>

        <#nested />

        <#assign lockModalTitle>
            <#if u.user.isLocked>
                user.unlock
            <#else>
                user.lock
            </#if>
        </#assign>
        <@modal.modal lockModalTitle "modal-lock" "label-lock">
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
                <@form.form method="POST" action=routes.getRouteUrl("users.verify", routes.getRouteUrl("users.setLocked", u.user.name, !u.user.isLocked, "", "")) class="form-inline">
                    <@csrf.formField />
                    <button type="submit" class="btn btn-primary"><@spring.message "general.continue" /></button>
                </@form.form>
            </div>
        </@modal.modal>

        <@modal.modal "user.tagline.edit" "modal-tagline" "label-tagline">
            <@form.form action=routes.getRouteUrl("users.saveTagline", u.user.name) method="POST">
                <@csrf.formField />
                <div class="modal-body">
                    <div class="setting setting-no-border">
                        <div class="setting-description">
                            <h4><@spring.message "user.tagline" /></h4>
                            <p><@spring.message "user.tagline.info" /></p>
                        </div>
                        <input class="form-control" type="text" value="${u.user.tagline!""}" id="tagline"
                               name="tagline" maxlength="${config.ore.users.maxTaglineLen}" />
                    </div>
                    <div class="clearfix"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <@spring.message "general.close" />
                    </button>
                    <input type="submit" value="<@spring.message "general.save" />" class="btn btn-primary" />
                </div>
            </@form.form>
        </@modal.modal>
    </@base.base>
</#macro>
