<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />
<#import "*/utils/modal.ftlh" as modal />
<#import "*/utils/prompt.ftlh" as prompt />
<#import "*/utils/userAvatar.ftlh" as userAvatar />
<#import "*/utils/form.ftlh" as form>
<#import "*/utils/csrf.ftlh" as csrf>
<#import "*/utils/prompt.ftlh" as promptView>

<#function canEditOrgSettings u o so>
<#-- @ftlvariable name="u" type="io.papermc.hangar.modelold.viewhelpers.UserData" -->
<#-- @ftlvariable name="o" type="java.util.Optional<io.papermc.hangar.modelold.viewhelpers.OrganizationData>" -->
<#-- @ftlvariable name="so" type="java.util.Optional<io.papermc.hangar.modelold.viewhelpers.ScopedOrganizationData>" -->
<#-- @ftlvariable name="Permission" type="io.papermc.hangar.model.common.Permission" -->
    <#assign Permission=@helper["io.papermc.hangar.model.Permission"]>
    <#return u.isOrga() && o.present && so.get().permissions.has(Permission.EditOrganizationSettings)>
</#function>

<#macro view u o so additionalScripts="" additionalStyling="">
<#-- @ftlvariable name="u" type="io.papermc.hangar.modelold.viewhelpers.UserData" -->
<#-- @ftlvariable name="o" type="java.util.Optional<io.papermc.hangar.modelold.viewhelpers.OrganizationData>" -->
<#-- @ftlvariable name="so" type="java.util.Optional<io.papermc.hangar.modelold.viewhelpers.ScopedOrganizationData>" -->
<#-- @ftlvariable name="Permission" type="io.papermc.hangar.model.common.Permission" -->
    <#assign Permission=@helper["io.papermc.hangar.model.Permission"]>
    <#assign scriptsVar>
        <script nonce="${nonce}">
            <#outputformat "JavaScript">
            window.USERNAME = '${u.user.name}';
            window.NO_ACTION_MESSAGE = {};
            window.CATEGORY_TITLE = {};
            window.CATEGORY_ICON = {};
            <#assign Category=@helper["io.papermc.hangar.model.Category"]>
            <#list Category.values() as category>
            window.CATEGORY_TITLE['${category.apiName}'] = '${category.title}';
            window.CATEGORY_ICON['${category.apiName}'] = '${category.icon}';
            </#list>
            window.NO_ACTION_MESSAGE.starred = '<@spring.messageArgs code="user.noStars" args=[u.user.name] />';
            window.NO_ACTION_MESSAGE.watching = '<@spring.messageArgs code="user.noWatching" args=[u.user.name] />';
            </#outputformat>
        </script>
        <script type="text/javascript" src="${hangar.url("js/userPage.js")}"></script>
        ${additionalScripts}
    </#assign>

    <@base.base title=u.user.name additionalScripts=scriptsVar additionalStyling=additionalStyling>
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
                        user-avatar-md <#if canEditOrgSettings(u, o, so)>organization-avatar</#if>
                    </#assign>
                    <@userAvatar.userAvatar userName=u.user.name avatarUrl=utils.avatarUrl(u.user.name) clazz=avatarClass />

                    <#if canEditOrgSettings(u, o, so)>
                        <div class="edit-avatar" style="display: none;">
                            <a href="${Routes.ORG_UPDATE_AVATAR.getRouteUrl(u.user.name)}"><i
                                        class="fas fa-edit"></i> <@spring.message "user.editAvatar" /></a>
                        </div>

                        <#assign Prompt=@helper["io.papermc.hangar.model.Prompt"] />
                    <#-- @ftlvariable name="Prompt" type="io.papermc.hangar.modelold.Prompt" -->
                        <#if !u.headerData.currentUser.readPrompts?seq_contains(Prompt.CHANGE_AVATAR.ordinal())>
                            <@promptView.prompt prompt=Prompt.CHANGE_AVATAR id="popover-avatar" />
                        </#if>
                    </#if>
                    <div>
                        <div class="user-title">
                            <h1 class="username float-left">
                                ${u.user.name}
                            </h1>
                            <div class="user-actions">
                                <#if u.isCurrent() && !u.isOrga()>
                                    <a class="user-settings" href="${config.getAuthUrl()}/accounts/settings">
                                        <i class="fas fa-cog" data-toggle="tooltip" data-placement="top" title="Settings"></i>
                                    </a>
                                    <span data-toggle="modal" data-target="#modal-lock">
                                        <i class="fas <#if u.user.isLocked()>fa-lock<#else>fa-unlock-alt</#if> action-lock-account"
                                           data-toggle="tooltip"
                                           data-placement="top"
                                           title="<#if !u.user.isLocked()><@spring.message "user.lock" /><#else><@spring.message "user.unlock" /></#if>"></i>
                                    </span>

                                    <a class="action-api" href="${Routes.USERS_EDIT_API_KEYS.getRouteUrl(u.user.name)}">
                                        <i class="fas fa-key" data-toggle="tooltip" data-placement="top" title="API Keys"></i>
                                    </a>
                                </#if>
                                <#if u.hasUser()>
                                    <#if u.userPerm.has(Permission.ModNotesAndFlags) || u.userPerm.has(Permission.Reviewer)>
                                        <a class="user-settings" href="${Routes.SHOW_ACTIVITIES.getRouteUrl(u.user.name)}">
                                            <i class="fas fa-calendar" data-toggle="tooltip" data-placement="top" title="Activity"></i>
                                        </a>
                                    </#if>
                                </#if>
                                <#if u.headerData.globalPerm(Permission.EditAllUserSettings)>
                                    <a class="user-settings" href="${Routes.USER_ADMIN.getRouteUrl(u.user.name)}">
                                        <i class="fas fa-wrench" data-toggle="tooltip" data-placement="top" title="User Admin"></i>
                                    </a>
                                </#if>
                            </div>
                            <div class="clearfix"></div>
                        </div>
                        <div class="user-tag">
                            <i class="minor">
                            <#if u.user.tagline??>
                                ${u.user.tagline}
                            <#elseif u.isCurrent() || canEditOrgSettings(u, o, so)>
                                Add a tagline
                            </#if>
                            </i>

                            <#if u.isCurrent() || canEditOrgSettings(u, o, so)>
                                <a href="#" data-toggle="modal" data-target="#modal-tagline">
                                    <i class="fas fa-edit"></i>
                                </a>
                            </#if>
                        </div>
                    </div>
                </span>

                <!-- Roles -->
                <ul class="user-roles">
                    <#list u.globalRoles?sort_by("value") as role>
                        <li class="user-role channel" style="background-color: ${role.color.hex}">${role.title}</li>
                    </#list>
                </ul>

                <div class="user-info">
                    <i class="minor">${u.projectCount}&nbsp;<#if u.projectCount == 1>project<#else>projects</#if></i><br/>
                    <i class="minor">
                        <@spring.messageArgs code="user.memberSince" args=[utils.prettifyDate(u.user.joinDate!u.user.createdAt)] />
                    </i><br/>
                    <a href="https://papermc.io/forums/users/${u.user.name}">
                        <@spring.message "user.viewOnForums" /> <i class="fas fa-external-link-alt"></i>
                    </a>
                </div>
            </div>
        </div>

        <#nested />

        <@modal.modal lockModalTitle() "modal-lock" "label-lock">
            <div class="modal-body">
                <p class="minor">
                    <#if u.user.isLocked()>
                        <@spring.message "user.unlock.confirm" />
                    <#else>
                        <@spring.message "user.lock.confirm" />
                    </#if>
                </p>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default"
                        data-dismiss="modal"><@spring.message "general.close" /></button>
                <@form.form method="POST" action=Routes.USERS_VERIFY.getRouteUrl(Routes.USERS_SET_LOCKED.getRouteUrl(u.user.name, (!u.user.isLocked())?string, "", "")) class="form-inline">
                    <@csrf.formField />
                    <button type="submit" class="btn btn-primary"><@spring.message "general.continue" /></button>
                </@form.form>
            </div>
        </@modal.modal>

        <@modal.modal "user.tagline.edit" "modal-tagline" "label-tagline">
            <@form.form action=Routes.USERS_SAVE_TAGLINE.getRouteUrl(u.user.name) method="POST">
                <@csrf.formField />
                <div class="modal-body">
                    <div class="setting setting-no-border">
                        <div class="setting-description">
                            <h4><@spring.message "user.tagline" /></h4>
                            <p><@spring.message "user.tagline.info" /></p>
                        </div>
                        <input class="form-control" type="text" value="${u.user.tagline!""}" id="tagline"
                               name="tagline" maxlength="${config.getUser().maxTaglineLen}"/>
                    </div>
                    <div class="clearfix"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <@spring.message "general.close" />
                    </button>
                    <input type="submit" value="<@spring.message "general.save" />" class="btn btn-primary"/>
                </div>
            </@form.form>
        </@modal.modal>
    </@base.base>
</#macro>

<#function lockModalTitle>
    <#if u.user.isLocked()>
        <#return "user.unlock">
    <#else>
        <#return "user.lock">
    </#if>
</#function>
