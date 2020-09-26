<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />
<#import "*/utils/userAvatar.ftlh" as userAvatar>

<#macro notificationFilterOption option>
    <#if option == notificationFilter>
        selected
    </#if>
</#macro>

<#macro inviteFilterOption option>
    <#if option == inviteFilter>
        selected
    </#if>
</#macro>

<#macro formatNotification notification>
<#-- @ftlvariable name="notification" type="io.papermc.hangar.db.model.NotificationsTable" -->
    <#if notification.messageArgs?size gt 1>
        <@spring.messageArgs notification.messageArgs[0] notification.messageArgs[1..] /> <#--this might not work? idk if freemarker has a array spread operator, couldn't find one-->
    <#else>
        <@spring.message notification.messageArgs[0] />
    </#if>
</#macro>

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "js/notifications.js" />"></script>
</#assign>

<#assign NotificationFilter=@helper["io.papermc.hangar.model.NotificationFilter"] />
<#assign InviteFilter=@helper["io.papermc.hangar.model.InviteFilter"] />
<#assign message><@spring.message "notification.plural" /></#assign>
<@base.base title=message additionalScripts=scriptsVar>

    <div class="row">
        <div class="col-12 col-md-8 header-flags">
                <!-- Notifications header -->
            <div class="row">
                <div class="col-12 header-flags">
                    <div class="float-left">
                        <h2><@spring.message "notification.plural" /></h2>
                    </div>
                    <div class="float-right form-inline notification-controls">
                        <select class="form-control select-notifications">
                            <option <@notificationFilterOption NotificationFilter.UNREAD />>
                            <@spring.message NotificationFilter.UNREAD.title />
                            </option>
                            <option <@notificationFilterOption NotificationFilter.READ />>
                            <@spring.message NotificationFilter.READ.title />
                            </option>
                            <option <@notificationFilterOption NotificationFilter.ALL />>
                            <@spring.message NotificationFilter.ALL.title />
                            </option>
                        </select>
                        <#if notifications?size gt 0 && notificationFilter.equals(NotificationFilter.UNREAD)>
                            <button class="btn-mark-all-read btn btn-primary">
                                <i class="fas fa-check"></i> <strong><@spring.message "notification.markAllRead" /></strong>
                            </button>
                        </#if>
                    </div>
                </div>
            </div>

                <!-- Notifications content -->
            <div class="row">
                <div class="col-12">
                    <div class="list-group-item no-notifications" <#if notifications?size gt 0>
                        style="display: none; </#if>">
                        <h3 class="minor">
                            <i class="far fa-thumbs-up"></i> <@spring.message notificationFilter.emptyMessage />
                        </h3>
                    </div>

                    <ul class="list-group">
                        <#list notifications as notification, origin>
                            <li class="list-group-item notification" data-action="${notification.action!"none"}" data-id="${notification.id}">
                                <#if origin??><@userAvatar.userAvatar userName=origin.user.name avatarUrl=utils.avatarUrl(origin.user.name) clazz="user-avatar-s" /></#if>
                                <@formatNotification notification />
                                <#if !notification.read>
                                    <span class="btn-mark-read">
                                        <i class="minor float-right fas fa- sm fa-check"></i>
                                    </span>
                                </#if>
                            </li>
                        </#list>
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-12 col-md-4">
                <!-- Invites header -->
            <div class="row">
                <div class="col-12 form-inline">
                    <h2 class="float-left"><@spring.message "notification.invites" /></h2>
                    <select class="float-right form-control notification-controls select-invites">
                        <option <@inviteFilterOption InviteFilter.ALL />>
                        <@spring.message InviteFilter.ALL.title />
                        </option>
                        <option <@inviteFilterOption InviteFilter.PROJECTS />>
                        <@spring.message InviteFilter.PROJECTS.title />
                        </option>
                        <option <@inviteFilterOption InviteFilter.ORGANIZATIONS />)>
                        <@spring.message InviteFilter.ORGANIZATIONS.title />
                        </option>
                    </select>
                </div>
            </div>
            <div class="row">
                <!-- Invites content -->
                <div class="col-12">

                <#assign i = 0>
                <#list invites as invite, subject>
                    <#if i % 2 == 0>
                        <div class="row">
                    </#if>
                    <#assign i = i + 1>

                    <div class="invite col-12 col-md-6">
                        <div class="invite-content" data-id="${invite.userRole.id}"
                        data-type="${subject.type}">
                            <span class="minor">
                                <i class="dismiss float-left fas fa-times" style="display: none;"></i>
                                <span class="float-right"><i class="fas fa-tag"></i> ${subject.type?capitalize}</span>
                            </span>
                            <br/>

                            <div class="invite-message invite-choice" style="display: auto;">
                                <p>
                                    <@spring.messageArgs code="notification.invite" args=[subject.type] />
                                <a href=${subject.url}>${subject.name}</a>.
                                </p>
                                <button class="btn btn-invite btn-accept btn-sm btn-success">
                                    <@spring.message "notification.invite.accept" />
                                </button>
                                <button class="btn btn-invite btn-decline btn-sm btn-danger">
                                    <@spring.message "notification.invite.decline" />
                                </button>
                            </div>

                            <div class="invite-message invite-accepted" style="display: none;">
                                <i class="minor fas fa-3x fa-birthday-cake"></i><br/>
                                <@spring.messageArgs code="notification.invite.joined" args=[subject.name] /><br/>
                                <a href="${subject.url}" class="btn btn-sm btn-primary">
                                    <@spring.message "notification.invite.visit" />
                                </a>
                                <button class="btn btn-undo btn-sm btn-info">
                                    <@spring.message "notification.invite.undo" />
                                </button>
                            </div>

                            <div class="invite-message invite-declined" style="display: none;">
                                <i class="minor fas fa-3x fa-times"></i>
                                <#assign args=[subject.name] />
                                <p><@spring.messageArgs "notification.invite.declined" args /></p>
                            </div>

                            <i class="minor invite-loading fas fa-5x fa-spinner fa-spin" style="display: none;"></i>
                        </div>
                    </div>

                    <#if (i + 1) % 2 == 0>
                    </div>
                    </#if>
                </#list>
                </div>
            </div>
        </div>
    </div>

</@base.base>
