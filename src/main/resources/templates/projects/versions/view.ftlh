<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/form.ftlh" as form>
<#import "*/utils/csrf.ftlh" as csrf>
<#import "*/projects/view.ftlh" as projects />
<#import "*/utils/editor.ftlh" as editor />

<#assign ReviewState=@helper["io.papermc.hangar.model.generated.ReviewState"] />
<#assign Permission=@helper["io.papermc.hangar.model.Permission"] />
<#assign Visibility=@helper["io.papermc.hangar.model.Visibility"] />
<#assign scriptsVar>
    <script nonce="${nonce}">
        <#outputformat "JavaScript">
        window.VERSION = ${mapper.valueToTree(v.v)};
        </#outputformat>
    </script>
    <script type="text/javascript" src="${hangar.url("js/version-view.js")}"></script>
</#assign>
<@projects.view p=v.p sp=sp active="#versions" noButtons=true additionalScripts=scriptsVar>
    <!-- Version header -->
    <div class="row">
        <div class="col-md-12 version-header">
                <!-- Title -->
            <div class="clearfix">
                <h1 class="float-left position-relative">
                    ${v.v.versionString}
                    <div class="channel channel-head" style="background-color: ${v.c.color.hex};">${v.c.name}</div>
                </h1>
            </div>

                <!-- User info -->
            <p class="user date float-left">
                <a href="${Routes.USERS_SHOW_PROJECTS.getRouteUrl(v.p.project.ownerName)}">
                    <strong>${v.p.project.ownerName}</strong>
                </a>
                released this version on ${utils.prettifyDateTime(v.v.createdAt)}
            </p>

                <!-- Buttons -->

            <div class="float-right version-actions">
                <div class="version-icons">
                    <div>
                    <#if v.recommended>
                            <i data-toggle="tooltip" data-placement="left"
                            title="<@spring.message "version.recommended" />"
                            class="recommended far fa-lg fa-gem float-right"></i>
                    </#if>

                    <#if v.v.reviewState.checked>
                            <#if headerData.globalPerm(Permission.Reviewer)>
                                <#if v.approvedBy?has_content>
                                    <i class="minor">
                                        <@spring.messageArgs code="version.approved.info" args=[v.approvedBy, utils.prettifyDateTime(v.v.approvedAt)] />
                                    </i>
                                </#if>
                            </#if>
                            <#if v.v.reviewState == ReviewState.PARTIALLY_REVIEWED>
                                <i data-toggle="tooltip" data-placement="left" title="<@spring.message "version.approvedPartial" />"
                                class="far fa-lg fa-check-circle"></i>
                            <#else>
                                <i data-toggle="tooltip" data-placement="left" title="<@spring.message "version.approved" />"
                                class="far fa-lg fa-check-circle"></i>
                            </#if>
                    </#if>
                    </div>
                </div>

                <div class="version-buttons float-right">
                    <div><span class="date">${utils.formatFileSize(v.v.fileSize)}</span></div>

                    <div>

                        <#if !v.recommended && sp.perms(Permission.EditVersion) && v.v.visibility != Visibility.SOFTDELETE>
                            <@form.form method="POST" action=Routes.VERSIONS_SET_RECOMMENDED.getRouteUrl(v.p.project.ownerName, v.p.project.slug, v.v.versionStringUrl) class="d-inline">
                                <@csrf.formField />
                                <button type="submit" class="btn btn-info">
                                    <i class="fas fa-gem"></i> Set recommended
                                </button>
                            </@form.form>
                        </#if>

                        <#if headerData.globalPerm(Permission.Reviewer)>
                            <#if v.v.reviewState.checked>
                                <a href="${Routes.REVIEWS_SHOW_REVIEWS.getRouteUrl(v.p.project.ownerName, v.p.project.slug, v.v.versionStringUrl)}" class="btn btn-info"><@spring.message "review.log" /></a>
                            <#else>
                                <a href="${Routes.REVIEWS_SHOW_REVIEWS.getRouteUrl(v.p.project.ownerName, v.p.project.slug, v.v.versionStringUrl)}" class="btn btn-success">
                                    <i class="fas fa-play"></i> <@spring.message "review.start" />
                                </a>
                            </#if>
                        </#if>

                        <#if v.v.visibility == Visibility.SOFTDELETE>
                            <a class="btn btn-danger" disabled data-toggle="tooltip" data-placement="top"
                            title="<@spring.message "version.delete.alreadyDeleted" />">
                                <i class="fas fa-trash"></i> <@spring.message "general.delete" />
                            </a>
                        <#else>
                            <#if sp.perms(Permission.DeleteVersion)>
                                <#if v.p.publicVersions == 1>
                                    <a class="btn btn-danger" disabled data-toggle="tooltip" data-placement="top"
                                    title="<@spring.message "version.delete.cannotLast" />">
                                        <i class="fas fa-trash"></i> <@spring.message "general.delete" />
                                    </a>
                                <#else>
                                    <button type="button" class="btn btn-danger" data-toggle="modal" data-target="#modal-delete">
                                        <i class="fas fa-trash"></i> <@spring.message "general.delete" />
                                    </button>
                                </#if>
                            </#if>
                        </#if>

                        <div class="btn-group btn-download">
                            <a href="${Routes.VERSIONS_DOWNLOAD.getRouteUrl(v.p.project.ownerName, v.p.project.slug, v.v.versionStringUrl, "", "")}"
                               title="<@spring.message "project.download.recommend" />" data-toggle="tooltip"
                               data-placement="bottom" class="btn btn-primary">
                                <i class="fas fa-download"></i>
                                <#if v.v.externalUrl??>
                                    <@spring.message "general.downloadExternal" />
                                <#else>
                                    <@spring.message "general.download" />
                                </#if>
                            </a>
                            <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <span class="sr-only">Toggle Dropdown</span>
                            </button>
                            <div class="dropdown-menu dropdown-menu-right">
                                <a class="dropdown-item" href="${Routes.VERSIONS_DOWNLOAD.getRouteUrl(v.p.project.ownerName, v.p.project.slug, v.v.versionStringUrl, "", "")}"><@spring.message "general.download" /></a>
                                <a href="#" class="copy-url dropdown-item" data-clipboard-text="${config.baseUrl}${Routes.VERSIONS_DOWNLOAD.getRouteUrl(v.p.project.ownerName, v.p.project.slug, v.v.versionStringUrl, "", "")}">Copy URL</a>
                            </div>
                        </div>

                        <#if cu?? && headerData.globalPerm(Permission.ViewLogs)>
                            <div class="dropdown dropdown-menu-right" style="display: inline-block">
                                <button class="btn btn-alert dropdown-toggle" type="button" id="admin-version-actions" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                                    Admin actions
                                    <span class="caret"></span>
                                </button>
                                <div class="dropdown-menu" aria-labelledby="admin-version-actions">
                                    <a class="dropdown-item" href="${Routes.SHOW_LOG.getRouteUrl("", "", "", v.v.versionString, "", "", "")}">User Action Logs</a>
                                    <#if headerData.globalPerm(Permission.Reviewer)>
                                        <#if v.v.visibility == Visibility.SOFTDELETE>
                                            <a class="dropdown-item" href="#" data-toggle="modal" data-target="#modal-restore">Undo delete</a>
                                        </#if>
                                        <#if headerData.globalPerm(Permission.HardDeleteVersion) && !v.recommended && (v.p.publicVersions gt 1 || v.v.visibility == Visibility.SOFTDELETE)>
                                            <a class="dropdown-item" href="#" data-toggle="modal" data-target="#modal-harddelete" style="color: darkred">Hard delete</a>
                                        </#if>
                                    </#if>
                                </div>
                            </div>
                        </#if>

                    </div>
                </div>
            </div>
        </div>
    </div>


        <!-- Description -->
    <div class="row version-description">
        <div id="description" class="col-md-8">
            <div class="row">
                <#if !v.v.reviewState.checked>
                    <div class="col-md-12">
                        <div class="alert-review alert alert-info" role="alert">
                            <i class="fas fa-info-circle"></i>
                            <@spring.message "project.download.warn" />
                        </div>
                    </div>
                </#if>
                <div class="col-md-12" style="z-index: 5">
                    <div id="version-view">
                        <div class="page-preview page-rendered">${markdownService.render(v.v.getDescription())}</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Dependencies</h3>
                </div>
                <ul class="list-group">
                    <#assign Platform=@helper["io.papermc.hangar.model.Platform"] />
                    <#list v.formattedDependencies as pd, deps>
                        <li class="list-group-item" style="background-color: ${pd.platform.tagColor.background}22">
                            <a href="${pd.platform.url}">
                                <strong>${pd.platform.getName()}</strong>
                            </a>
                            <#if pd.versions?size != 0>
                                <span class="version-string">${pd.formattedVersion}</span>
                            </#if>
                            <#if deps?? && deps?size != 0>
                                <ul class="list-group">
                                    <#list deps as dep, url>
                                    <#-- @ftlvariable name="dep" type="io.papermc.hangar.model.generated.Dependency" -->
                                        <li class="list-group-item">
                                            <#if url?has_content>
                                                <a href="${url}">
                                                    ${dep.name}
                                                </a>
                                            <#else>
                                                ${dep.name}
                                            </#if>
                                            <#if dep.required>
                                                <small class="float-right">(required)</small>
                                            </#if>
                                        </li>
                                    </#list>
                                </ul>
                            </#if>
                        </li>
                    </#list>
                </ul>
            </div>
        </div>
    </div>

    <#if sp.perms(Permission.DeleteVersion) && v.p.publicVersions != 1>
        <div class="modal fade" id="modal-delete" tabindex="-1" role="dialog" aria-labelledby="label-delete">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="label-delete"><@spring.message "version.delete.title" /></h4>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <@form.form method="POST" action=Routes.VERSIONS_SOFT_DELETE.getRouteUrl(v.p.project.ownerName, v.p.project.slug, v.v.versionStringUrl)>
                        <div class="modal-body">
                            <@spring.message "version.delete.info" />
                            <textarea name="comment" class="textarea-delete-comment form-control" rows="3"></textarea>
                        </div>
                        <div class="modal-footer">
                            <div class="form-inline">
                                <@csrf.formField />
                                <button type="button" class="btn btn-default" data-dismiss="modal">
                                <@spring.message "channel.edit.close" />
                                </button>
                                <input type="submit" name="delete" value="<@spring.message "general.delete" />" class="btn btn-danger">
                            </div>
                        </div>
                    </@form.form>
                </div>
            </div>
        </div>
    </#if>

    <#if headerData.globalPerm(Permission.Reviewer)>
        <#if v.v.visibility == Visibility.SOFTDELETE>
            <div class="modal fade" id="modal-restore" tabindex="-1" role="dialog" aria-labelledby="label-delete">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title" id="label-delete"><@spring.message "general.restore" /></h4>
                            <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                        <@form.form method="POST" action=Routes.VERSIONS_RESTORE.getRouteUrl(v.p.project.ownerName, v.p.project.slug, v.v.versionStringUrl)>
                            <div class="modal-body">
                                <textarea name="comment" class="textarea-delete-comment form-control" rows="3"></textarea>
                            </div>
                            <div class="modal-footer">
                                <div class="form-inline">
                                    <@csrf.formField />
                                    <button type="button" class="btn btn-default" data-dismiss="modal">
                                    <@spring.message "channel.edit.close" />
                                    </button>
                                    <input type="submit" name="delete" value="<@spring.message "general.restore" />" class="btn btn-success">
                                </div>
                            </div>
                        </@form.form>
                    </div>
                </div>
            </div>
        </#if>
        <#if headerData.globalPerm(Permission.HardDeleteVersion)>
            <div class="modal fade" id="modal-harddelete" tabindex="-1" role="dialog" aria-labelledby="label-delete">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title" id="label-delete"><@spring.message "general.harddelete" /></h4>
                            <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                        <@form.form method="POST" action=Routes.VERSIONS_DELETE.getRouteUrl(v.p.project.ownerName, v.p.project.slug, v.v.versionStringUrl)>
                            <div class="modal-body">
                                <textarea name="comment" class="textarea-delete-comment form-control" rows="3"></textarea>
                            </div>
                            <div class="modal-footer">
                                <div class="form-inline">
                                    <@csrf.formField />
                                    <button type="button" class="btn btn-default" data-dismiss="modal">
                                    <@spring.message "channel.edit.close" />
                                    </button>
                                    <input type="submit" name="delete" value="<@spring.message "general.harddelete" />" class="btn btn-danger">
                                </div>
                            </div>
                        </@form.form>
                    </div>
                </div>
            </div>
        </#if>
    </#if>
</@projects.view>
