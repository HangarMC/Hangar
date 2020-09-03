<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/form.ftlh" as form>
<#import "*/utils/csrf.ftlh" as csrf>
<#import "*/projects/view.ftlh" as projects />
<#import "*/projects/helper/btnHide.ftlh" as btnHide />
<#import "*/projects/helper/inputSettings.ftlh" as inputSettings />
<#import "*/utils/userAvatar.ftlh" as userAvatar />
<#import "*/users/memberList.ftlh" as memberList />

<#--
@(p: ProjectData, sp: ScopedProjectData, deploymentKey: Option[Model[ProjectApiKey]], iconUrl: String)(implicit messages: Messages, flash: Flash,
        request: OreRequest[_], config: OreConfig, renderer: MarkdownRenderer, assetsFinder: AssetsFinder)-->

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/projectManage.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/hideProject.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/iconUpload.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/keyGen.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/userSearch.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/memberList.js" />"></script>
    <script <#--@CSPNonce.attr -->>
        projectName = "${p.project.name}";
        PROJECT_OWNER = "${p.project.ownerName}";
        PROJECT_SLUG = "${p.project.slug}";
        pluginId = "${p.project.pluginId}";
        keyGenText = "<@spring.message "project.settings.genKey" />";
        keyRevokeText = "<@spring.message "project.settings.revokeKey" />";
    </script>
</#assign>

<#assign Permission=@helper["io.papermc.hangar.model.Permission"]>
<@projects.view p=p sp=sp active="#settings" additionalScripts=scriptsVar>

    <div class="row">
        <div class="col-md-8">

            <!-- Main settings -->
            <div class="panel panel-default panel-settings">
                <div class="panel-heading">
                    <h3 class="panel-title pull-left"><@spring.message "project.settings" /></h3>
                    <#if headerData.globalPerm(Permission.SeeHidden)>
                        <@btnHide.btnHide p.namespace, p.visibility />

                        <div class="modal fade" id="modal-visibility-comment" tabindex="-1" role="dialog" aria-labelledby="modal-visibility-comment">
                            <div class="modal-dialog" role="document">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <button type="button" class="close" data-dismiss="modal" aria-label="<@spring.message "general.close" />">
                                            <span aria-hidden="true">&times;</span>
                                        </button>
                                        <h4 class="modal-title" style="color:black;">Comment</h4>
                                    </div>
                                    <div class="modal-body">
                                        <textarea class="textarea-visibility-comment form-control" rows="3"></textarea>
                                    </div>
                                    <div class="modal-footer">
                                        <button class="btn btn-default" data-dismiss="modal"><@spring.message "general.close" /></button>
                                        <button class="btn btn-visibility-comment-submit btn-primary"><i class="fas fa-pencil-alt"></i> Submit</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </#if>
                </div>

                <div class="panel-body">
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
                                    <div class="btn-group pull-right">
                                        <button class="btn btn-default btn-reset">Reset</button>
                                        <button class="btn btn-info btn-upload pull-right" disabled>
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
                            <input form="rename" id="name" name="name" class="form-control" type="text"
                            value="${p.project.name}"
                            maxlength="@config.ore.projects.maxNameLen">
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

                    <@form.form action=routes.getRouteUrl("projects.save", p.project.ownerName, p.project.slug) method="POST" id="save" class="pull-right">
                        <@csrf.formField />
                        <input type="hidden" id="update-icon" name="update-icon" value="false" />
                        <button type="submit" name="save" class="btn btn-success btn-spinner" data-icon="fa-check">
                            <i class="fas fa-check"></i> Save changes
                        </button>
                    </@form.form>

                    <script>
                        // Basically, hides the form value if its empty. Makes the controller simpler
                        $("#save").submit(function() {
                            $(":input[form=save]").filter(function () {
                                return !this.value;
                            }).attr("disabled", true);

                            return true;
                        });
                    </script>
                </div>
            </div>
        </div>

            <!-- Side panel -->
        <div class="col-md-4">
            <@memberList.memberList project=p perms=sp.permissions editable=true removeCall=routes.getRouteUrl("projects.removeMember", p.project.ownerName, p.project.slug) settingsCall=routes.getRouteUrl("projects.showSettings", p.project.ownerName, p.project.slug) />
        </div>
    </div>
    </div>

    <div class="modal fade" id="modal-rename" tabindex="-1" role="dialog" aria-labelledby="label-rename">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"
                    aria-label="<@spring.message "general.cancel" />">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title" id="label-rename"><@spring.message "project.rename.title" /></h4>
                </div>
                <div class="modal-body">
                <@spring.message "project.rename.info" />
                </div>
                <div class="modal-footer">
                    <div class="form-inline">
                        <@form.form action=routes.getRouteUrl("projects.rename", p.project.ownerName, p.project.slug) method="POST" id="rename">
                            <@csrf.formField />
                            <button type="button" class="btn btn-default" data-dismiss="modal">
                                <@spring.message "channel.edit.close" />
                            </button>
                            <input type="submit" name="rename" value="<@spring.message "project.rename" />" class="btn btn-warning">
                        </@form.form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="modal-delete" tabindex="-1" role="dialog" aria-labelledby="label-delete">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title" id="label-delete"><@spring.message "project.delete.title" /></h4>
                </div>
                <@form.form action=routes.getRouteUrl("projects.softDelete", p.project.ownerName, p.project.slug) method="POST">
                    <div class="modal-body">
                        <@spring.message "project.delete.info" />
                        <br>
                        <textarea name="comment" class="textarea-delete-comment form-control" rows="3"></textarea>
                        <br>
                        <div class="alert alert-warning">
                        <@spring.messageArgs code="project.delete.info.uniqueid" args=[p.project.name] />
                        </div>
                    </div>
                    <div class="modal-footer">
                        <div class="form-inline">
                            <@csrf.formField />
                            <button type="button" class="btn btn-default" data-dismiss="modal">
                            <@spring.message "channel.edit.close" />
                            </button>
                            <input type="submit" name="delete"
                            value="<@spring.message "general.delete" />" class="btn btn-danger">
                        </div>
                    </div>
                </@form.form>
            </div>
        </div>

</@projects.view>
