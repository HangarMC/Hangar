<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/form.ftlh" as form>
<#import "*/utils/csrf.ftlh" as csrf>
<#import "*/utils/userAvatar.ftlh" as userAvatar>
<#import "*/users/invite/userSearch.ftlh" as userSearch>
<#import "*/users/invite/roleSelect.ftlh" as roleSelect>

<!-- TODO: Pagination -->
<#-- @ftlvariable name="perms" type="io.papermc.hangar.model.common.Permission" -->
<#assign Permission=@helper["io.papermc.hangar.model.Permission"]>
<#macro memberList project perms editable=false removeCall="" settingsCall="" saveCall="" includeModal=true>
<#-- @ftlvariable name="project" type="io.papermc.hangar.modelold.viewhelpers.JoinableData" -->
    <#if project.members?size != 0>

        <#if editable && perms.has(Permission.ManageSubjectMembers)>
            <@roleSelect.roleSelect roleCategory=project.roleCategory id="select-role" classes="float-right" hidden=true />

            <!-- Row template -->
            <ul style="display: none;">
                <li id="row-user" class="list-group-item">
                    <input type="hidden" />
                    <@userAvatar.userAvatar clazz="user-avatar-xs"></@userAvatar.userAvatar>
                    <a class="username"></a>
                    <i class="fas fa-times user-cancel"></i>
                    <@roleSelect.roleSelect roleCategory=project.roleCategory classes="float-right" />
                </li>
            </ul>

            <!-- Delete modal -->
            <#if includeModal>
                <div class="modal fade" id="modal-user-delete" tabindex="-1" role="dialog" aria-labelledby="label-user-delete">
                    <div class="modal-dialog" role="document">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="label-user-delete"><@spring.message "project.removeMember" /></h4>
                                <button type="button" class="close" data-dismiss="modal"
                                        aria-label="<@spring.message "general.close" />">
                                    <span aria-hidden="true">&times;</span>
                                </button>
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
        </#if>

        <div class="alert alert-danger member-error" style="display: none">
            <span>error</span>
        </div>

        <div class="card">
            <div class="card-header">
                <h3 class="float-left card-title"><@spring.message "project.settings.members" /></h3>

                <#if perms.has(Permission.ManageSubjectMembers)>
                    <div class="float-right">
                        <#if !editable && settingsCall?has_content>
                            <a href="${settingsCall}"
                               class="btn btn-warning btn-sm">
                                <i class="fas fa-pencil-alt">Edit</i>
                            </a>
                        </#if>

                        <#if saveCall?has_content>
                            <@form.form action=saveCall method="POST" id="save">
                                <@csrf.formField />
                                <button class="btn-members-save btn btn-card btn-sm" data-tooltip-toggle
                                        data-placement="top" data-title="<@spring.message "org.users.save" />" style="display: none;">
                                    <i class="fas fa-save"></i>
                                </button>
                            </@form.form>
                        </#if>
                    </div>
                </#if>
            </div>

            <ul class="list-members list-group">
                <!-- Member list -->
                <#list project.filteredMembers(headerData) as subjectRole, user>
                <#-- @ftlvariable name="subjectRole" type="io.papermc.hangar.db.modelold.RoleTable" -->
                <#-- @ftlvariable name="user" type="io.papermc.hangar.db.modelold.UsersTable" -->
                    <li class="list-group-item">
                        <@userAvatar.userAvatar userName=user.name avatarUrl=utils.avatarUrl(user.name) clazz="user-avatar-xs"></@userAvatar.userAvatar>
                        <a class="username" href="${Routes.USERS_SHOW_PROJECTS.getRouteUrl(user.name)}">
                            ${user.name}
                        </a>
                        <p style="display: none;" class="role-id">${subjectRole.role.roleId}</p>
                        <#if editable && perms.has(Permission.ManageSubjectMembers) && !subjectRole.role.permissions.has(Permission.IsOrganizationOwner)>
                            <a href="#">
                                <i style="padding-left:5px" class="fas fa-trash" data-toggle="modal" data-target="#modal-user-delete"></i>
                            </a>
                            <a href="#"><i style="padding-left:5px" class="fas fa-edit"></i></a>
                        </#if>

                        <span class="minor float-right">
                            <#if !subjectRole.isAccepted>
                                <span class="minor">(Invited as ${subjectRole.role.title})</span>
                            <#else>
                                ${subjectRole.role.title}
                            </#if>
                        </span>
                    </li>
                </#list>

                <!-- User search -->
                <#if perms.has(@helper["io.papermc.hangar.model.Permission"].ManageSubjectMembers) && editable>
                    <li class="list-group-item">
                        <@userSearch.userSearch />
                    </li>
                </#if>

            </ul>
        </div>

    </#if>
</#macro>

