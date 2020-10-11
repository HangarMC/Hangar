<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/form.ftlh" as form>
<#import "*/utils/csrf.ftlh" as csrf>
<#import "*/projects/view.ftlh" as projects />
<#import "*/projects/helper/btnHide.ftlh" as btnHide />
<#import "*/projects/helper/inputSettings.ftlh" as inputSettings />
<#import "*/utils/userAvatar.ftlh" as userAvatar />
<#import "*/users/memberList.ftlh" as memberList />

<#assign Permission=@helper["io.papermc.hangar.model.Permission"] />
<#assign Role=@helper["io.papermc.hangar.model.Role"] />
<#assign scriptsVar>
    <script>
        window.PROJECT = ${mapper.valueToTree(p)};
        window.DEPLOYMENT_KEY = ${mapper.valueToTree(deploymentKey)};

        window.FILTERED_MEMBERS = ${utils.serializeMap(p.filteredMembers(headerData))};
        window.PERMISSIONS = {
            SEE_HIDDEN: ${headerData.globalPerm(Permission.SeeHidden)?c},
            MANAGE_MEMBERS: ${sp.perms(Permission.ManageSubjectMembers)?c},
            EDIT_API_KEYS: ${sp.perms(Permission.EditApiKeys)?c},
            DELETE_PROJECT: ${sp.perms(Permission.DeleteProject)?c},
            HARD_DELETE_PROJECT: ${headerData.globalPerm(Permission.HardDeleteProject)?c},
        }
        window.POSSIBLE_ROLES = ${mapper.valueToTree(Role.values()?filter(role -> role.category == p.roleCategory && role.isAssignable())?sort_by("permissions")?reverse)};
        window.PROJECT_OWNER_PERM = ${Permission.IsSubjectOwner.value};
    </script>
    <script type="text/javascript" src="<@hangar.url "js/project-settings.js" />"></script>
</#assign>
<#assign styleVar>
    <link rel="stylesheet" href="<@hangar.url "css/project-settings.css" />">
</#assign>

<@projects.view p=p sp=sp active="#settings" additionalScripts=scriptsVar additionalStyling=styleVar>
    <div id="project-settings">
        <div class="row">
            <div class="col-md-8">

                <!-- Main settings -->
                <div class="card card-settings">
                    <div class="card-header">
                        <h3 class="card-title float-left"><@spring.message "project.settings" /></h3>
                    </div>

                    <div class="card-body">
                        <@inputSettings.inputSettings
                        "save"
                        p.settings.homepage
                        p.settings.issues
                        p.settings.source
                        p.settings.support
                        p.settings.licenseName
                        p.settings.licenseUrl
                        p.project.category
                        p.settings.forumSync
                        p.settings.keywords
                        />
                        <!-- Description -->
                        <div class="setting">
                            <div class="setting-description">
                                <h4>Description</h4>
                                <p>A short description of your project (max ${config.projects.maxDescLen})</p>
                            </div>
                            <input
                                    type="text"
                                    form="save"
                                    class="form-control"
                                    id="description"
                                    name="description"
                                    maxlength="${config.projects.maxDescLen}"
                                    <#if p.project.description?has_content>
                                        value="${p.project.description}"
                                    <#else>
                                        placeholder="<@spring.message "version.create.noDescription" />"
                                    </#if>
                            >
                            <div class="clearfix"></div>
                        </div>

                        <!-- Project icon -->
                        <div class="setting setting-icon">
                            <form id="form-icon" enctype="multipart/form-data">
                                <@csrf.formField />
                                <div class="setting-description">
                                    <h4>Icon</h4>
                                    <@userAvatar.userAvatar userName=p.projectOwner.name avatarUrl=utils.avatarUrl(p.projectOwner.name) imgSrc=iconUrl clazz="user-avatar-md" />

                                    <input class="form-control-static" type="file" id="icon" name="icon" />
                                </div>
                                <div class="setting-content">
                                    <div class="icon-description">
                                        <p>Upload an image representative of your project.</p>
                                        <div class="btn-group float-right">
                                            <button class="btn btn-default btn-reset">Reset</button>
                                            <button class="btn btn-info btn-upload float-right" disabled>
                                                <i class="fas fa-upload"></i> Upload
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                <div class="clearfix"></div>
                            </form>
                        </div>

                        <#if sp.perms(Permission.EditApiKeys)>
                            <div class="setting">
                                <div class="setting-description">
                                    <h4><@spring.message "project.settings.deployKey" /></h4>
                                    <p>
                                        <@spring.message "project.settings.deployKey.info" />
                                        <#--TODO I think this link is supposed to show some info or something-->
                                        <a href="#"><i class="fas fa-question-circle"></i></a>
                                    </p>
                                    <#if deploymentKey??>
                                        <input class="form-control input-key" type="text" value="${deploymentKey.value}" readonly />
                                    <#else>
                                        <input class="form-control input-key" type="text" value="" readonly />
                                    </#if>
                                </div>
                                <div class="setting-content">
                                    <#if deploymentKey??>
                                        <button class="btn btn-danger btn-block btn-key-revoke" data-key-id="${deploymentKey.id}">
                                            <span class="spinner" style="display: none;"><i class="fas fa-spinner fa-spin"></i></span>
                                            <span class="text"><@spring.message "project.settings.revokeKey" /></span>
                                        </button>
                                    <#else>
                                        <button class="btn btn-info btn-block btn-key-gen">
                                            <span class="spinner" style="display: none;"><i class="fas fa-spinner fa-spin"></i></span>
                                            <span class="text"><@spring.message "project.settings.genKey" /></span>
                                        </button>
                                    </#if>
                                </div>
                                <div class="clearfix"></div>
                            </div>
                        </#if>

                        <!-- Rename -->
                        <div class="setting">
                            <div class="setting-description">
                                <h4 class="danger"><@spring.message "project.rename" /></h4>
                                <p><@spring.message "project.rename.info" /></p>
                            </div>
                            <div class="setting-content">
                                <input form="rename" id="name" name="name" class="form-control" type="text" value="${p.project.name}" maxlength="@config.ore.projects.maxNameLen">
                                <button id="btn-rename" data-toggle="modal" data-target="#modal-rename" class="btn btn-warning" disabled>
                                    <@spring.message "project.rename" />
                                </button>
                            </div>
                            <div class="clearfix"></div>
                        </div>

                        <!-- Delete -->
                        <#if sp.perms(Permission.DeleteProject)>
                            <div class="setting">
                                <div class="setting-description">
                                    <h4 class="danger">Delete</h4>
                                    <p>Once you delete a project, it cannot be recovered.</p>
                                </div>
                                <div class="setting-content">
                                    <button class="btn btn-delete btn-danger" data-toggle="modal"
                                            data-target="#modal-delete">
                                        Delete
                                    </button>
                                </div>
                                <div class="clearfix"></div>
                            </div>
                        </#if>

                        <#if headerData.globalPerm(Permission.HardDeleteProject)>
                            <div class="setting striped">
                                <div class="setting-description">
                                    <h4 class="danger">Hard Delete</h4>
                                    <p>Once you delete a project, it cannot be recovered.</p>
                                </div>
                                <div class="setting-content">
                                    <button class="btn btn-delete btn-danger btn-visibility-change" data-project="${p.project.ownerName}/${p.project.slug}" data-level="-99" data-modal="true">
                                        <strong>Hard Delete</strong>
                                    </button>
                                </div>
                                <div class="clearfix"></div>
                            </div>
                        </#if>

                        <@form.form action=Routes.PROJECTS_SAVE.getRouteUrl(p.project.ownerName, p.project.slug) method="POST" id="save" class="float-right">
                            <@csrf.formField />
                            <input type="hidden" id="update-icon" name="update-icon" value="false" />
                            <button type="submit" name="save" class="btn btn-success btn-spinner" data-icon="fa-check">
                                <i class="fas fa-check"></i> Save changes
                            </button>
                        </@form.form>
                    </div>
                </div>
            </div>

            <!-- Side panel -->
            <div class="col-md-4">
                <@memberList.memberList project=p perms=sp.permissions editable=true removeCall=Routes.PROJECTS_REMOVE_MEMBER.getRouteUrl(p.project.ownerName, p.project.slug) settingsCall=Routes.PROJECTS_SHOW_SETTINGS.getRouteUrl(p.project.ownerName, p.project.slug) />
            </div>
        </div>
    </div>

</@projects.view>
