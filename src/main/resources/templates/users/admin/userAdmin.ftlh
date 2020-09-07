<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#--
@import controllers.sugar.Requests.OreRequest
@import models.viewhelper.{OrganizationData, UserData}
@import ore.OreConfig
@import ore.db.Model
@import ore.models.organization.Organization
@import ore.models.project.Project
@import ore.models.user.User
@import ore.models.user.role.{OrganizationUserRole, ProjectUserRole, UserRoleModel}
@import ore.permission.role.Role
@(user: UserData, orga: Option[OrganizationData], userProjectRoles: Seq[(Project, Model[ProjectUserRole])])(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)
-->
<#assign prefix>
    Edit ${u.isOrga()?string("Organization", "User")}
</#assign>

<#assign Role=@helper["io.papermc.hangar.model.Role"] />
<#assign RoleCategory=@helper["io.papermc.hangar.db.customtypes.RoleCategory"] />
<#assign orgRoles=Role.values()?filter(role -> role.category == RoleCategory.ORGANIZATION) />
<#assign projectRoles=Role.values()?filter(role -> role.category == RoleCategory.PROJECT) />

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/userAdmin.js" />"></script>
</#assign>

<@base.base title=(prefix + u.user.name) additionalScripts=scriptsVar>

    <div class="row">
        <div class="col-md-12">
            <h1>${prefix}<a href="${Routes.USERS_SHOW_PROJECTS.getRouteUrl(u.user.name)}">${u.user.name}</a></h1>
        </div>
    </div>

    <div class="row">
        <div class="col-md-9">
            <div class="row">
                <#if !u.isOrga()>
                    <@panel "Organizations">
                        <@roleTable keyName="Organization" tableType="orgRole" withOwner=true roles=u.orgas roleTypes=orgRoles ; org, role>
                        <#-- @ftlvariable name="org" type="io.papermc.hangar.model.viewhelpers.OrganizationData" -->
                            <a href="${Routes.USER_ADMIN.getRouteUrl(org.org.name)}">
                                ${org.org.name}
                            </a>
                        </@roleTable>
                    </@panel>
                <#else>
                    <@panel "Members">
                        <#-- TODO organization members map-->
                        <@roleTable keyName="User" tableType="memberRole" withOwner=false roles=orga.adminTable() roleTypes=orgRoles ; member, role>
                            <a href="${Routes.USER_ADMIN.getRouteUrl(member.user.name)}">
                                ${member.user.name}
                            </a>
                        </@roleTable>
                    </@panel>
                </#if>
            </div>
            <#if !u.isOrga()>
                <div class="row">
                    <@panel "Projects">
                        <@roleTable keyName="Project" tableType="projectRole" withOwner=true roles=userProjectRoles roleTypes=projectRoles ; project, role>
                        <#-- @ftlvariable name="project" type="io.papermc.hangar.model.viewhelpers.ProjectData" -->
                            <a href="${Routes.PROJECTS_SHOW.getRouteUrl(project.projectOwner.name, project.project.slug)}">
                                ${project.project.name}
                            </a>
                        </@roleTable>
                    </@panel>
                </div>
            </#if>
        </div>

        <@panel title="Other Administration" size=3>
            <div class="list-group">
                <div class="list-group-item">
                    <a href="${config.authUrl}/admin/accounts/user/${u.user.id}/change/">HangarAuth Profile</a>
                </div>
                <div class="list-group-item">
                    <a href="https://papermc.io/forums${u.user.name}">Forum Profile</a>
                </div>
            </div>
        </@panel>

    </div>

</@base.base>

<#macro roleTable keyName tableType withOwner roles roleTypes>
<#-- @ftlvariable name="withOwner" type="java.lang.Boolean" -->
    <table class="table role-table">
        <tr><th>${keyName}</th><#if withOwner><th>Owner</th></#if><th>Role</th><th>Accepted</th></tr>
        <#list roles as object, role>
        <#-- @ftlvariable name="role" type="io.papermc.hangar.model.viewhelpers.UserRole" -->
            <tr data-role-type="${tableType}" data-role-id="${role.role.roleId}">
                <td><#nested object, role /></td>
                <#if withOwner>
                    <#-- @ftlvariable name="object" type="io.papermc.hangar.model.viewhelpers.JoinableData" -->
                    <td><a href="${Routes.USER_ADMIN.getRouteUrl(object.ownerName)}">${object.ownerName}</a></td>
                </#if>
                <#if !role.role.assignable>
                    <td>
                    ${role.role.title}
                    </td>
                    <td colspan="2"><input type="checkbox" class="role-accepted" ${role.accepted?string('checked', '')} disabled></td>
                <#else>
                    <td><select class="select-role">
                    <#list roleTypes as roleType>
                        <option value="${roleType.value}" <#if role == roleType>selected</#if>
                        <#if roleType == Role.PROJECT_OWNER || roleType == Role.ORGANIZATION_OWNER>data-refresh="true"</#if>>${roleType.title}</option>
                    </#list>
                    </select></td>
                    <td><input type="checkbox" class="role-accepted" ${role.accepted?string('checked', '')}></td>
                    <td><a href="#" class="delete-role"><i class="fas fa-trash"></i></a></td>
                </#if>
            </tr>
        </#list>
    </table>
</#macro>

<#macro panel title size=12>
    <div class="col-md-${size}">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4 class="panel-title">${title}</h4>
            </div>
            <#nested />
        </div>
    </div>
</#macro>
