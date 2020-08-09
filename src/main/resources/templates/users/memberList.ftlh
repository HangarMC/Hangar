<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/form.ftlh" as form>
<#import "*/utils/csrf.ftlh" as csrf>
<#import "*/utils/userAvatar.ftlh" as userAvatar>
<#import "*/users/invite/userSearch.ftlh" as userSearch>
<#import "*/users/invite/roleSelect.ftlh" as roleSelect>

<!-- TODO: Pagination -->
<#-- @ftlvariable name="perms" type="me.minidigger.hangar.model.Permission" -->
<#assign Permission=@helper["me.minidigger.hangar.model.Permission"]>
<#macro memberList project perms editable=false removeCall="" settingsCall="" saveCall="">
    <#-- @ftlvariable name="project" type="me.minidigger.hangar.model.viewhelpers.ProjectData" -->
    <#if project.members?size != 0>

        <#if editable && perms.has(Permission.ManageSubjectMembers)>
            <@roleSelect.roleSelect roleCategory=project.roleCategory id="select-role" classes="pull-right" hidden=true />

            <!-- Row template -->
            <ul style="display: none;">
                <li id="row-user" class="list-group-item">
                    <input type="hidden" />
                    <@userAvatar.userAvatar clazz="user-avatar-xs"></@userAvatar.userAvatar>
                    <a class="username"></a>
                    <i class="fas fa-times user-cancel"></i>
                    <@roleSelect.roleSelect roleCategory=project.roleCategory classes="pull-right" />
                </li>
            </ul>

            <!-- Delete modal -->
            <div class="modal fade" id="modal-user-delete" tabindex="-1" role="dialog" aria-labelledby="label-user-delete">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal"
                                    aria-label="<@spring.message "general.close" />">
                                <span aria-hidden="true">&times;</span>
                            </button>
                            <h4 class="modal-title" id="label-user-delete"><@spring.message "project.removeMember" /></h4>
                        </div>
                        <div class="modal-body"><@spring.message "project.removeMember.confirm" /></div>
                        <div class="modal-footer">
                            <@form.form action=removeCall method="POST" class="form-inline">
                                <@csrf.formField />
                                <input type="hidden" name="username" />
                                <button type="button" class="btn btn-default" data-dismiss="modal">
                                    <@spring.message "general.close" />
                                </button>
                                <button type="submit" class="btn btn-danger"><@spring.message "general.remove" /></button>
                            </@form.form>
                        </div>
                    </div>
                </div>
            </div>
        </#if>

        <div class="alert alert-danger member-error" style="display: none">
            <span>error</span>
        </div>

        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="pull-left panel-title"><@spring.message "project.settings.members" /></h3>

                <#if perms.has(Permission.ManageSubjectMembers)>
                    <div class="pull-right">
                        <#if !editable && settingsCall?has_content>
                            <a href="${settingsCall}"
                               class="btn yellow btn-xs">
                                <i class="fas fa-pencil-alt"></i>
                            </a>
                        </#if>

                        <#if saveCall?has_content>
                            <@form.form action=saveCall method="POST" id="save">
                                <@csrf.formField />
                                <button class="btn-members-save btn btn-default btn-panel btn-xs" data-toggle="tooltip"
                                        data-placement="top" data-title="<@spring.message "org.users.save" />" style="display: none;">
                                    <i class="fas fa-paper-plane"></i>
                                </button>
                            </@form.form>
                        </#if>
                    </div>
                </#if>
            </div>

            <ul class="list-members list-group">
                <!-- Member list -->
                <#-- @ftlvariable name="projectMember" type="me.minidigger.hangar.model.viewhelpers.ProjectMember" -->
                <#-- @ftlvariable name="user" type="me.minidigger.hangar.db.model.UsersTable" -->
                <#list project.filteredMembers(headerData) as projectMember, user>
                    <li class="list-group-item">
                        <@userAvatar.userAvatar userName=user.name avatarUrl=utils.avatarUrl(user.name) clazz="user-avatar-xs"></@userAvatar.userAvatar>
                        <a class="username" href="${routes.getRouteUrl("users.showProjects", user.name)}">
                            ${user.name}
                        </a>
                        <p style="display: none;" class="role-id">${projectMember.role.roleId}</p>
                        <#if editable && perms.has(Permission.ManageSubjectMembers) && !projectMember.role.permissions.has(Permission.IsOrganizationOwner)>
                            <a href="#">
                                <i style="padding-left:5px" class="fas fa-trash" data-toggle="modal" data-target="#modal-user-delete"></i>
                            </a>
                            <a href="#"><i style="padding-left:5px" class="fas fa-edit"></i></a>
                        </#if>

                        <span class="minor pull-right">
                            <#if !projectMember.isAccepted>
                                <span class="minor">(Invited as ${projectMember.role.title})</span>
                            <#else>
                                ${projectMember.role.title}
                            </#if>
                        </span>
                    </li>
                </#list>

                <!-- User search -->
                <#if perms.has(@helper["me.minidigger.hangar.model.Permission"].ManageSubjectMembers) && editable>
                    <li class="list-group-item">
                        <@userSearch.userSearch />
                    </li>
                </#if>

            </ul>
        </div>

    </#if>
</#macro>

