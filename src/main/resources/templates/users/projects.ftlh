<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/users/view.ftlh" as users />
<#import "*/users/memberList.ftlh" as memberList />

<#assign Permission=@helper["io.papermc.hangar.model.Permission"] />
<#assign Role=@helper["io.papermc.hangar.model.Role"] />

<#function canEditOrgMembers>
    <#return u.isOrga() && o.present && so.get().permissions.has(Permission.ManageOrganizationMembers)>
</#function>

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "js/user-profile.js" />"></script>
    <#if u.isOrga()>
        <script>
            window.JOINABLE = ${mapper.valueToTree(o.get())};
            window.CAN_MANAGE_MEMBERS = ${so.get().permissions.has(Permission.ManageSubjectMembers)?c};
            window.ORG_OWNER_PERM = ${Permission.IsOrganizationOwner.value?c};
            window.FILTERED_MEMBERS = ${utils.serializeMap(o.get().filteredMembers(headerData))};
            window.AVATAR_URL = '${config.security.api.avatarUrl}';
        </script>
        <#if o.present && canEditOrgMembers()>
            <script>
                window.POSSIBLE_ROLES = ${mapper.valueToTree(Role.values()?filter(role -> role.category == o.get().roleCategory && role.isAssignable())?sort_by("permissions")?reverse)};

                window.ORG_NAME = '${u.user.name}';
                window.PROJECT_ROLES = ${utils.serializeMap(o.get().projectRoles)};
            </script>
        </#if>
        <script type="text/javascript" src="<@hangar.url "js/org-sidebar.js" />"></script>
    </#if>
</#assign>

<#assign styleVar>
    <#if u.isOrga()>
        <link rel="stylesheet" href="<@hangar.url "css/org-sidebar.css" />">
    </#if>
</#assign>

<@users.view u=u o=o so=so additionalScripts=scriptsVar additionalStyling=styleVar>
    <div class="row">
        <div class="col-md-8">
            <div id="user-profile"></div>
        </div>
        <div class="col-md-4">
            <#if u.isOrga() && o.present && canEditOrgMembers()>
                <div id="org-project-manager">
                    <div class="card-user-info card">
                        <div class="card-header">
                            <h3 class="card-title"><@spring.message "project.manager" /></h3>
                        </div>
                        <table class="table card-body">
                            <tbody>
                            <#list o.get().projectRoles as role, project>
                                <tr>
                                    <td>
                                        <a href="${Routes.PROJECTS_SHOW.getRouteUrl(project.ownerName, project.slug)}">${project.ownerName}/${project.slug}</a>
                                        <span class="minor" style="font-size: 12px">${role.role.title}</span>
                                    </td>
                                    <td></td>
                                </tr>
                            </#list>
                            </tbody>
                        </table>
                    </div>
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
                <div id="org-member-list">
                    <@memberList.memberList project=o.get() editable=true perms=so.get().permissions
                    saveCall=Routes.ORG_UPDATE_MEMBERS.getRouteUrl(o.get().org.name)
                    removeCall=Routes.ORG_REMOVE_MEMBER.getRouteUrl(o.get().org.name) includeModal=false />
                </div>
            </#if>
        </div>
    </div>
</@users.view>
