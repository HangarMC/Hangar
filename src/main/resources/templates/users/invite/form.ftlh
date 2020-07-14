<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/userAvatar.ftlh" as userAvatar />
<#import "*/users/invite/userSearch.ftlh" as userSearch />
<#import "*/users/invite/roleSelect.ftlh" as roleSelect />

<#--
@import ore.OreConfig
@import ore.db.Model
@import ore.models.user.User
@import ore.permission.role.RoleCategory
@import util.syntax._
@import views.html.utils.userAvatar

@(owner: User, loadedUsers: Seq[Model[User]] = Seq(), roleCategory: RoleCategory)(implicit messages: Messages, config: OreConfig)
-->

<#macro form owner={} roleCategory="" loadedUsers=[]>

<#-- Template row -->
<table style="display: none;">
    <tbody>
        <tr id="result-row">
            <td>
                <input type="hidden"/>
                <@userAvatar.userAvatar clazz = "user-avatar-xs"/>
                <i class="fas fa-times user-cancel"></i>
                <a class="username" target="_blank" rel="noopener" href=""></a>
                <span>@users.invite.roleSelect(roleCategory)</span>
            </td>
        </tr>
    </tbody>
</table>

<#-- User not found alert (hidden) -->
<div class="alert alert-danger alert-dismissible" role="alert" style="display: none;">
    <button type="button" class="close" data-dismiss="alert" aria-label="Close">
        <span aria-hidden="true">&times;</span>
    </button>
    <@spring.message "user.notFound" /> "<span class="error-username"></span>"
</div>

<div class="member-table-container">
    <table class="table table-members">
        <tbody>
            <#-- Owner (not submitted) -->
            <tr>
                <td>
                    <@userAvatar.userAvatar userName=owner.name avatarUrl=owner.avatarUrl clazz="user-avatar-xs" />
                    <#if owner.name?has_content>
                        <strong>${owner.name}</strong>
                    </#if>
                    <span><i class="minor"><@spring.message "project.owner" /></i></span>
                </td>
            </tr>

            <#list loadedUsers as user>
                <tr>
                    <td>
                        <input form="form-continue" type="hidden" value="${user.id}" />
                        <@userAvatar.userAvatar userName=user.name avatarUrl=user.avatarUrl clazz="user-avatar-xs" />
                        <a target="_blank" rel="noopener" href="${routes.getRouteUrl("users.showProjects", user.name)}">
                            ${user.name}
                        </a>
                        <span><@roleSelect.roleSelect  roleCategory=roleCategory /></span>
                    </td>
                </tr>
            </#list>

            <#-- User search -->
            <tr>
                <td><@userSearch.userSearch /></td>
            </tr>

        </tbody>
    </table>
</div>
</#macro>
