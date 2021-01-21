<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign prefix>
    Edit ${u.isOrga()?string("Organization", "User")}
</#assign>

<#assign Role=@helper["io.papermc.hangar.model.Role"] />
<#assign RoleCategory=@helper["io.papermc.hangar.db.customtypes.RoleCategory"] />
<#assign orgRoles=Role.values()?filter(role -> role.category == RoleCategory.ORGANIZATION) />
<#assign projectRoles=Role.values()?filter(role -> role.category == RoleCategory.PROJECT) />

<#assign scriptsVar>
    <script nonce="${nonce}">
        <#outputformat "JavaScript">
        window.IS_ORGANIZATION = ${u.isOrga()?c};
        window.ROLES = {
            <#if u.isOrga()>
            MEMBERS: ${utils.serializeMap(orga.adminTable())},
            <#else>
            ORGANIZATIONS: ${utils.serializeMap(u.orgas)},
            PROJECTS: ${utils.serializeMap(userProjectRoles)},
            </#if>
        };
        window.ROLE_TYPES = {
            <#if !u.isOrga()>
            PROJECT: ${mapper.valueToTree(projectRoles)},
            </#if>
            ORGANIZATION: ${mapper.valueToTree(orgRoles)},
        }
        </#outputformat>
    </script>
    <script type="text/javascript" src="${hangar.url("js/user-admin.js")}"></script>
</#assign>

<@base.base title=(prefix + u.user.name) additionalScripts=scriptsVar>

    <div class="row">
        <div class="col-md-12">
            <h1>${prefix}<a href="${Routes.USERS_SHOW_PROJECTS.getRouteUrl(u.user.name)}">${u.user.name}</a></h1>
        </div>
    </div>

    <div class="row">
        <div class="col-md-9">
            <div id="user-admin">
                <div class="row">
                    <#if !u.isOrga()>
                        <@card "Organizations">
                            <@roleTable keyName="Organization" tableType="orgRole" withOwner=true roles=u.orgas roleTypes=orgRoles ; org, role>
                            <#-- @ftlvariable name="org" type="io.papermc.hangar.modelold.viewhelpers.OrganizationData" -->
                                <a href="${Routes.USER_ADMIN.getRouteUrl(org.org.name)}">
                                    ${org.org.name}
                                </a>
                            </@roleTable>
                        </@card>
                    <#else>
                        <@card "Members">
                            <@roleTable keyName="User" tableType="memberRole" withOwner=false roles=orga.adminTable() roleTypes=orgRoles ; memberUser, role>
                            <#-- @ftlvariable name="memberUser" type="io.papermc.hangar.db.modelold.UsersTable" -->
                                <a href="${Routes.USER_ADMIN.getRouteUrl(memberUser.name)}">
                                    ${memberUser.name}
                                </a>
                            </@roleTable>
                        </@card>
                    </#if>
                </div>
                <#if !u.isOrga()>
                    <div class="row">
                        <@card "Projects">
                            <@roleTable keyName="Project" tableType="projectRole" withOwner=true roles=userProjectRoles roleTypes=projectRoles ; project, role>
                            <#-- @ftlvariable name="project" type="io.papermc.hangar.modelold.viewhelpers.ProjectData" -->
                                <a href="${Routes.PROJECTS_SHOW.getRouteUrl(project.projectOwner.name, project.project.slug)}">
                                    ${project.project.name}
                                </a>
                            </@roleTable>
                        </@card>
                    </div>
                </#if>
            </div>
        </div>

        <@card title="Other Administration" size=3>
            <div class="list-group">
                <div class="list-group-item">
                    <a href="${config.authUrl}/admin/accounts/user/${u.user.id}/change/">HangarAuth Profile</a>
                </div>
                <div class="list-group-item">
                    <a href="https://papermc.io/forums/u/${u.user.name}">Forum Profile</a>
                </div>
            </div>
        </@card>

    </div>

</@base.base>

<#macro roleTable keyName tableType withOwner roles roleTypes>
<#-- @ftlvariable name="withOwner" type="java.lang.Boolean" -->
    <table class="table role-table">
        <tr><th>${keyName}</th><#if withOwner><th>Owner</th></#if><th>Role</th><th>Accepted</th></tr>
        <#list roles as object, role>
        <#-- @ftlvariable name="role" type="io.papermc.hangar.modelold.viewhelpers.UserRole" -->
            <tr data-role-type="${tableType}" data-role-id="${role.role.roleId}">
                <td><#nested object, role /></td>
                <#if withOwner>
                <#-- @ftlvariable name="object" type="io.papermc.hangar.modelold.viewhelpers.JoinableData" -->
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

<#macro card title size=12>
    <div class="col-md-${size}">
        <div class="card">
            <div class="card-header">
                <h4 class="card-title">${title}</h4>
            </div>
            <#nested />
        </div>
    </div>
</#macro>
