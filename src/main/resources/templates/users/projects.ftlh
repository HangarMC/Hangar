<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/users/view.ftlh" as users />
<#import "*/users/memberList.ftlh" as memberList />

<#--@import controllers.Routes.IO_PAPERMC_HANGAR_MODEL__PERMISSION.getRouteUrl(.Requests.OreRequest
@import models.viewhelper.{OrganizationData, ScopedOrganizationData, UserData}
@import ore.OreConfig
@import ore.permission.Permission
@import ore.permission.role.Role
@import util.syntax._
@import views.html.utils.userAvatar
@(u: UserData, o: Option[(OrganizationData, ScopedOrganizationData)])(
        implicit messages: Messages, flash: Flash, request: OreRequest[_], config: OreConfig, assetsFinder: AssetsFinder)-->
<#assign Permission=@helper["io.papermc.hangar.model.Permission"] />
<#assign Role=@helper["io.papermc.hangar.model.Role"] />

<#function canEditOrgMembers>
    <#return u.isOrga() && o?? && so.permissions.has(Permission.ManageOrganizationMembers)>
</#function>

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "build/user-profile.js" />"></script>
    <#if u.isOrga() && o?? && canEditOrgMembers()>
    <script type="text/javascript" src="<@hangar.url "javascripts/orgInvites.js" />"></script>
    </#if>
    <script type="text/javascript" src="<@hangar.url "javascripts/userSearch.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/memberList.js" />"></script>
</#assign>

<@users.view u=u o=o additionalScripts=scriptsVar>
    <div class="row">
        <div class="col-md-8">
            <div id="user-profile"></div>
        </div>
        <div class="col-md-4">
            <#if u.isOrga() && o?? && canEditOrgMembers()>
                <div class="card-user-info card">
                    <div class="card-header">
                        <h3 class="card-title"><@spring.message "project.manager" /></h3>
                    </div>
                    <table class="table card-body">
                        <tbody>
                            <#list o.projectRoles as role, project>
                                <tr>
                                    <td>
                                        <a href="${Routes.PROJECTS_SHOW.getRouteUrl(project.ownerName, project.slug)}">${project.ownerName}/${project.slug}</a>
                                        <span class="minor">${role.role.title}</span>
                                    </td>
                                    <td>
                                        <#if role.role != Role.PROJECT_OWNER>
                                            <#if role.isAccepted>
                                                <button class="btn btn-sm btn-danger float-right btn-invite" data-invite-id="${role.id}" data-invite-behalf="${u.user.name}" data-invite-accepted="decline">Leave</button>
                                            <#else>
                                                <button class="btn btn-sm btn-info float-right btn-invite" data-invite-id="${role.id}" data-invite-behalf="${u.user.name}" data-invite-accepted="accept">Join</button>
                                            </#if>
                                        </#if>
                                    </td>
                                </tr>
                            </#list>
                        </tbody>
                    </table>
                </div>
            </#if>
            <#if !u.isOrga()>
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title"> <@spring.message "org.plural" /></h3>
                    </div>
                    <table class="table card-body">
                        <tbody>
                        <#if u.orgas?size == 0>
                            <tr><td><i class="minor"><i class="fas fa-star"></i> <@spring.messageArgs code="user.noOrganizations" args=[u.user.name] /></i></td></tr>
                        <#else>
                            <#list u.orgas as organization, role>
                            <tr>
                                <td>
                                    <#import "*/utils/userAvatar.ftlh" as userAvatar>
                                    <@userAvatar.userAvatar userName=organization.org.name avatarUrl=utils.avatarUrl(organization.org.name) clazz="user-avatar-xxs"></@userAvatar.userAvatar>
                                <a href="${Routes.USERS_SHOW_PROJECTS.getRouteUrl(organization.org.name)}">
                                ${organization.org.name}
                                </a>
                                <div class="float-right">
                                ${role.role.title}
                            </div>
                            </td>
                            </tr>
                            </#list>
                        </#if>
                        </tbody>
                    </table>

                    <div class="card-footer">
                        <div class="clearfix"></div>
                    </div>
                </div>

                <div class="card-user-info card" data-action="starred">
                    <div class="card-header">
                        <h3 class="card-title"><i class="fas fa-star"></i> <@spring.message "project.starred" /></h3>
                    </div>
                    <table class="table card-body">
                        <tbody>
                        </tbody>
                    </table>

                    <div class="card-footer">
                        <div class="float-right">
                            <a class="prev" href="#" style="display: none;">&laquo;</a>
                            <a class="next" href="#" >&raquo;</a>
                        </div>
                        <div class="clearfix"></div>
                    </div>
                </div>

                <div class="card-user-info card" data-action="watching">
                    <div class="card-header">
                        <h3 class="card-title"><i class="fas fa-eye"></i> <@spring.message "project.watching" /></h3>
                    </div>
                    <table class="table card-body">
                        <tbody>
                        </tbody>
                    </table>

                    <div class="card-footer">
                        <div class="float-right">
                            <a class="prev" href="#" style="display: none;">&laquo;</a>
                            <a class="next" href="#" >&raquo;</a>
                        </div>
                        <div class="clearfix"></div>
                    </div>
                </div>
            <#else>
                <#assign orgData=o> <#-- todo fix scopeddata -->
                <@memberList.memberList project=orgData editable=true perms=so.permissions
                saveCall=Routes.ORG_UPDATE_MEMBERS.getRouteUrl(orgData.org.name)
                removeCall=Routes.ORG_REMOVE_MEMBER.getRouteUrl(orgData.org.name) />
            </#if>
        </div>
    </div>
</@users.view>
