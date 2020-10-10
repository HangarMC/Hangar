<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />
<#import "*/utils/userAvatar.ftlh" as userAvatar>

<#-- @ftlvariable name="NotificationFilter" type="io.papermc.hangar.model.NotificationFilter" -->
<#-- @ftlvariable name="InviteFilter" type="io.papermc.hangar.model.InviteFilter" -->
<#assign NotificationFilter=@helper["io.papermc.hangar.model.NotificationFilter"] />
<#assign InviteFilter=@helper["io.papermc.hangar.model.InviteFilter"] />
<#assign scriptsVar>
    <script>
        window.NOTIFICATION_FILTER = ${mapper.valueToTree(notificationFilter)};
        window.INVITE_FILTER = ${mapper.valueToTree(inviteFilter)};
        window.NOTIFICATIONS = ${utils.serializeMap(notifications)};
        window.INVITES = ${utils.serializeMap(invites)};

        window.FILTERS = {
            NOTIFICATIONS: ${mapper.valueToTree(NotificationFilter.getValues())},
            INVITES: ${mapper.valueToTree(InviteFilter.getValues())},
        };
    </script>
    <script type="text/javascript" src="<@hangar.url "js/notifications.js" />"></script>
</#assign>

<#assign styleVar>
    <link rel="stylesheet" href="<@hangar.url "css/notifications.css" />">
</#assign>

<#assign message><@spring.message "notification.plural" /></#assign>
<@base.base title=message additionalScripts=scriptsVar additionalStyling=styleVar>
    <div id="notifications"></div>
</@base.base>
