<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

@import controllers.sugar.Requests.OreRequest
@import ore.OreConfig
@import ore.db.Model
@import ore.db.impl.common.Visitable
@import ore.models.user.notification.{InviteFilter, NotificationFilter}
@import ore.models.user.role.UserRoleModel
@import ore.models.user.{Notification, User}
@import util.syntax._
@import views.html.utils.userAvatar

@import cats.data.{NonEmptyList => NEL}
@(notifications: Seq[(Model[Notification], Option[User])], invites: Seq[(Model[UserRoleModel[_]], Visitable)], notificationFilter: NotificationFilter,
        inviteFilter: InviteFilter)(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)

@notificationFilterOption(option: NotificationFilter) = @{
    if (option == notificationFilter) {
        Html("selected")
    }
}

@inviteFilterOption(option: InviteFilter) = @{
    if (option == inviteFilter) {
        Html("selected")
    }
}

@formatNotification(notification: Notification) = @{
    notification.messageArgs match {
        case NEL(head, Nil) => Html(messages(head))
        case NEL(head, tail) => Html(messages(head, tail: _*))
    }
}

<#assign scriptsVar>
    <script type="text/javascript" src="<@spring.url "javascripts/notifications.js" />"></script>
</#assign>

<#assign message><@spring.message "notification.plural" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar>

    <div class="row">
        <div class="col-xs-12 col-md-8 header-flags">
                <!-- Notifications header -->
            <div class="row">
                <div class="col-xs-12 header-flags">
                    <div class="pull-left">
                        <h2><@spring.message "notification.plural" /></h2>
                    </div>
                    <div class="pull-right form-inline notification-controls">
                        <select class="form-control select-notifications">
                            <option @notificationFilterOption(NotificationFilter.Unread)>
                            <@spring.message NotificationFilter.Unread.title />
                            </option>
                            <option @notificationFilterOption(NotificationFilter.Read)>
                            <@spring.message NotificationFilter.Read.title />
                            </option>
                            <option @notificationFilterOption(NotificationFilter.All)>
                            <@spring.message NotificationFilter.All.title />
                            </option>
                        </select>

                        <#if notifications.nonEmpty && notificationFilter.equals(NotificationFilter.Unread)>
                            <button class="btn-mark-all-read btn btn-primary">
                                <i class="fas fa-check"></i> <strong><@spring.message "notification.markAllRead" /></strong>
                            </button>
                        </#if>
                    </div>
                </div>
            </div>

                <!-- Notifications content -->
            <div class="row">
                <div class="col-xs-12">
                    <div class="list-group-item no-notifications" @if(notifications.nonEmpty) {
                        style="display: none; }">
                        <h3 class="minor">
                            <i class="far fa-thumbs-up"></i> <@spring.message notificationFilter.emptyMessage />
                        </h3>
                    </div>

                    <ul class="list-group">
                    @notifications.map { case (notification, origin) =>
                    <li class="list-group-item notification" data-action="@notification.action.getOrElse("none")"
                    data-id="@notification.id">
                        @origin.map { origin =>
                            @userAvatar(Some(origin.name), origin.avatarUrl, clazz = "user-avatar-s")
                        }
                        @formatNotification(notification)

                        <#if !notification.isRead>
                            <span class="btn-mark-read">
                                <i class="minor pull-right fas fa- sm fa-check"></i>
                            </span>
                        </#if>
                    </li>
                    }
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-xs-12 col-md-4">
                <!-- Invites header -->
            <div class="row">
                <div class="col-xs-12 form-inline">
                    <h2 class="pull-left"><@spring.message "notification.invites" /></h2>
                    <select class="pull-right form-control notification-controls select-invites">
                        <option @inviteFilterOption(InviteFilter.All)>
                        <@spring.message InviteFilter.All.title />
                        </option>
                        <option @inviteFilterOption(InviteFilter.Projects)>
                        <@spring.message InviteFilter.Projects.title />
                        </option>
                        <option @inviteFilterOption(InviteFilter.Organizations)>
                        <@spring.message InviteFilter.Organizations.title />
                        </option>
                    </select>
                </div>
            </div>
            <div class="row">
                <!-- Invites content -->
                <div class="col-xs-12">

                <#assign i = 0>
                <#list invites.zipWithIndex as invite, subject>
                    <#if i % 2 == 0>
                        <div class="row">
                    </#if>
                    <#assign i = i + 1>

                    <div class="invite col-xs-12 col-md-6">
                        <div class="invite-content" data-id="@invite.id"
                        data-type="@subject.getClass.getSimpleName.toLowerCase">
                            <span class="minor">
                                <i class="dismiss pull-left fas fa-times" style="display: none;"></i>
                                <span class="pull-right"><i class="fas fa-tag"></i> @subject.getClass.getSimpleName</span>
                            </span>
                            <br/>

                            <div class="invite-message invite-choice" style="display: auto;">
                                <p>
                                    <@spring.message "notification.invite", subject.getClass.getSimpleName.toLowerCase />
                                <a href="@subject.url">@subject.name</a>.
                                </p>
                                <button class="btn btn-invite btn-accept btn-sm btn-success">
                                <@spring.message "notification.invite.accept" />
                                </button>
                                <button class="btn btn-invite btn-decline btn-sm btn-danger">
                                @Html(messages("notification.invite.decline"))
                                </button>
                            </div>

                            <div class="invite-message invite-accepted" style="display: none;">
                                <i class="minor fas fa-3x fa-birthday-cake"></i><br/>
                                @Html(messages("notification.invite.joined", subject.name))<br/>
                                <a href="@subject.url" class="btn btn-sm btn-primary">
                                <@spring.message "notification.invite.visit" />
                                </a>
                                <button class="btn btn-undo btn-sm btn-info">
                                <@spring.message "notification.invite.undo" />
                                </button>
                            </div>

                            <div class="invite-message invite-declined" style="display: none;">
                                <i class="minor fas fa-3x fa-times"></i>
                                <p>@Html(messages("notification.invite.declined", subject.name))</p>
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
