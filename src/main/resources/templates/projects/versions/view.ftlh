<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

@import controllers.sugar.Requests.OreRequest
@import models.viewhelper.{ScopedProjectData, VersionData}
@import ore.OreConfig
@import ore.data.Platform
@import ore.markdown.MarkdownRenderer
@import ore.models.project.{ReviewState, Visibility}
@import ore.permission.Permission
@import util.StringFormatterUtils._
@import util.syntax._
@import views.html.helper.{CSRF, form}
@import views.html.utils.editor
@(v: VersionData, sp: ScopedProjectData)(implicit messages: Messages, request: OreRequest[_], flash: Flash, config: OreConfig, renderer: MarkdownRenderer, assetsFinder: AssetsFinder)

@projectRoutes = @{controllers.project.routes.Projects}
@versionRoutes = @{controllers.project.routes.Versions}
@reviewRoutes = @{controllers.routes.Reviews}
@appRoutes = @{controllers.routes.Application}

@projects.view(v.p, sp, "#versions", noButtons = true) {

    <!-- Version header -->
    <div class="row">
        <div class="col-md-12 version-header">
                <!-- Title -->
            <div class="clearfix">
                <h1 class="pull-left">@v.v.versionString</h1>
                <span class="channel channel-head" style="background-color: @v.c.color.hex;">@v.c.name</span>
            </div>

                <!-- User info -->
            <p class="user date pull-left">
                <a href="${routes.getRouteUrl("users.showProjects", v.p.project.ownerName)}">
                    <strong>${v.p.project.ownerName}</strong>
                </a>
                released this version on ${prettifyDate(v.v.createdAt)}
            </p>

                <!-- Buttons -->

            <div class="pull-right version-actions">
                <div class="version-icons">
                    <div>
                    <#if v.isRecommended>
                            <i data-toggle="tooltip" data-placement="left"
                            title="<@spring.message "version.recommended" />"
                            class="recommended far fa-lg fa-gem pull-right"></i>
                    </#if>

                    <#if v.v.reviewState.isChecked>
                            <#if request.headerData.globalPerm(Permission.Reviewer)>
                                @v.approvedBy.zip(v.v.approvedAt).map { t =>
                                    <i class="minor">
                                        @Html(messages("version.approved.info", t._1, prettifyDate(t._2)))
                                    </i>
                                }
                            </#if>
                            <#if v.v.reviewState == ReviewState.PartiallyReviewed>
                                <i data-toggle="tooltip" data-placement="left" title="<@spring.message "version.approvedPartial" />"
                                class="far fa-lg fa-check-circle"></i>
                            <#else>
                                <i data-toggle="tooltip" data-placement="left" title="<@spring.message "version.approved" />"
                                class="far fa-lg fa-check-circle"></i>
                            </#if>
                    </#if>
                    </div>
                </div>

                <div class="version-buttons pull-right">
                    <div><span class="date">${v.v.humanFileSize}</span></div>

                    <div>

                        <#if !v.isRecommended && sp.perms(Permission.EditVersion) && v.v.visibility != Visibility.SoftDelete>
                            @form(action = versionRoutes.setRecommended(
                                v.p.project.ownerName, v.p.project.slug, v.v.versionString), Symbol("class") -> "form-inline") {
                                @CSRF.formField
                                <button type="submit" class="btn btn-info">
                                    <i class="fas fa-gem"></i> Set recommended
                                </button>
                            }
                        </#if>

                        <#if request.headerData.globalPerm(Permission.Reviewer)>
                            <#if v.v.reviewState.isChecked>
                                <a href="@reviewRoutes.showReviews(v.p.project.ownerName, v.p.project.slug, v.v.versionString)" class="btn btn-info"><@spring.message "review.log" /></a>
                            <#else>
                                <a href="@reviewRoutes.showReviews(v.p.project.ownerName, v.p.project.slug, v.v.versionString)" class="btn btn-success">
                                    <i class="fas fa-play"></i> <@spring.message "review.start" />
                                </a>
                            </#if>
                        </#if>

                        <#if v.v.visibility == Visibility.SoftDelete>
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
                            <a href="@versionRoutes.download(v.p.project.ownerName, v.p.project.slug, v.v.versionString, None)"
                            title="<@spring.message "project.download.recommend" />" data-toggle="tooltip"
                            data-placement="bottom" class="btn btn-primary">
                                <i class="fas fa-download"></i> <@spring.message "general.download" />
                            </a>
                            <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <span class="caret"></span>
                                <span class="sr-only">Toggle Dropdown</span>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-right">
                                <li><a href="@versionRoutes.download(v.p.project.ownerName, v.p.project.slug, v.v.versionString, None)"><@spring.message "general.download" /></a></li>
                                <li><a href="#" class="copy-url" data-clipboard-text="@config.app.baseUrl@versionRoutes.download(v.p.project.ownerName, v.p.project.slug, v.v.versionString, None)">Copy URL</a></li>
                            </ul>
                        </div>

                        <#if request.hasUser && request.headerData.globalPerm(Permission.ViewLogs)>
                            <div class="dropdown dropdown-menu-right" style="display: inline-block">
                                <button class="btn btn-alert dropdown-toggle" type="button" id="admin-version-actions" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                                    Admin actions
                                    <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu" aria-labelledby="admin-version-actions">
                                    <li><a href="@appRoutes.showLog(None, None, None, Some(v.v.versionString), None, None, None)">User Action Logs</a></li>
                                    <#if request.headerData.globalPerm(Permission.Reviewer)>
                                        <#if v.v.visibility == Visibility.SoftDelete>
                                            <li><a href="#" data-toggle="modal" data-target="#modal-restore">Undo delete</a></li>
                                        </#if>
                                        <#if request.headerData.globalPerm(Permission.HardDeleteVersion) && !v.isRecommended && (v.p.publicVersions gt 1 || v.v.visibility == Visibility.SoftDelete)>
                                            <li><a href="#" data-toggle="modal" data-target="#modal-harddelete" style="color: darkred">Hard delete</a></li>
                                        </#if>
                                    </#if>
                                </ul>
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
                <#if !v.v.reviewState.isChecked>
                    <div class="col-md-12">
                        <div class="alert-review alert alert-info" role="alert">
                            <i class="fas fa-info-circle"></i>
                            <@spring.message "project.download.warn" />
                        </div>
                    </div>
                </#if>
                <div class="col-md-12">
                @editor(
                    saveCall = versionRoutes.saveDescription(
                        v.p.project.ownerName, v.p.project.slug, v.v.versionString
                    ),
                    enabled = sp.perms(Permission.EditPage),
                    raw = v.v.description.getOrElse(""),
                    cooked = v.v.obj.render,
                    subject = "Version"
                )
                </div>
            </div>
        </div>


        <#if v.v.dependencies.nonEmpty>
                <!-- Dependencies -->
            <div class="col-md-4">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Dependencies</h3>
                    </div>
                    <ul class="list-group">

                        @for(platform <- Platform.getPlatforms(v.dependencies.map(_._1.pluginId).toList)) {
                            @v.v.dependencies.find(_.pluginId == platform.dependencyId).map { dependency =>
                                <li class="list-group-item">
                                    <a href="@{platform.url}">
                                        <strong>@{platform.name}</strong>
                                    </a>
                                    <p class="version-string">@dependency.version</p>
                                </li>
                            }
                        }

                        @v.filteredDependencies.map { case (depend, project)=>
                        <li class="list-group-item">
                            <#if project.isDefined>
                                <a href="@projectRoutes.show(
                                    project.get.ownerName, project.get.slug)">
                                    <strong>${project.get.name}</strong>
                                </a>
                            <#else>
                                <div class="minor">
                                    ${depend.pluginId}
                                    <i class="fas fa-question-circle"
                                    title="<@spring.message "version.dependency.notOnOre" />"
                                    data-toggle="tooltip" data-placement="right"></i>
                                </div>
                            </#if>
                        <p class="version-string">@depend.version</p>
                        </li>
                        }
                    </ul>
                </div>
            </div>
        <#else>
            <p class="minor text-center"><i><@spring.message "version.dependency.no" /></i></p>
        </#if>
    </div>

    <#if sp.perms(Permission.DeleteVersion) && v.p.publicVersions != 1>
        <div class="modal fade" id="modal-delete" tabindex="-1" role="dialog" aria-labelledby="label-delete">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                            <span aria-hidden="true">&times;</span>
                        </button>
                        <h4 class="modal-title" id="label-delete"><@spring.message "version.delete.title" /></h4>
                    </div>
                    @form(action = versionRoutes.softDelete(v.p.project.ownerName, v.p.project.slug, v.v.versionString)) {
                        <div class="modal-body">
                            <@spring.message "version.delete.info" />
                            <textarea name="comment" class="textarea-delete-comment form-control" rows="3"></textarea>
                        </div>
                        <div class="modal-footer">
                            <div class="form-inline">
                                @CSRF.formField
                                <button type="button" class="btn btn-default" data-dismiss="modal">
                                <@spring.message "channel.edit.close" />
                                </button>
                                <input type="submit" name="delete" value="<@spring.message "general.delete" />" class="btn btn-danger">
                            </div>
                        </div>
                    }
                </div>
            </div>
        </div>
    }</#if>

    <#if request.headerData.globalPerm(Permission.Reviewer)>
        <#if v.v.visibility == Visibility.SoftDelete>
            <div class="modal fade" id="modal-restore" tabindex="-1" role="dialog" aria-labelledby="label-delete">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                                <span aria-hidden="true">&times;</span>
                            </button>
                            <h4 class="modal-title" id="label-delete"><@spring.message "general.restore" /></h4>
                        </div>
                        @form(action = versionRoutes.restore(v.p.project.ownerName, v.p.project.slug, v.v.versionString)) {
                            <div class="modal-body">
                                <textarea name="comment" class="textarea-delete-comment form-control" rows="3"></textarea>
                            </div>
                            <div class="modal-footer">
                                <div class="form-inline">
                                    @CSRF.formField
                                    <button type="button" class="btn btn-default" data-dismiss="modal">
                                    <@spring.message "channel.edit.close" />
                                    </button>
                                    <input type="submit" name="delete" value="<@spring.message "general.restore" />" class="btn btn-success">
                                </div>
                            </div>
                        }
                    </div>
                </div>
            </div>
        </#if>
        <#if request.headerData.globalPerm(Permission.HardDeleteVersion)>
            <div class="modal fade" id="modal-harddelete" tabindex="-1" role="dialog" aria-labelledby="label-delete">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                                <span aria-hidden="true">&times;</span>
                            </button>
                            <h4 class="modal-title" id="label-delete"><@spring.message "general.harddelete" /></h4>
                        </div>
                        @form(action = versionRoutes.delete(v.p.project.ownerName, v.p.project.slug, v.v.versionString)) {
                            <div class="modal-body">
                                <textarea name="comment" class="textarea-delete-comment form-control" rows="3"></textarea>
                            </div>
                            <div class="modal-footer">
                                <div class="form-inline">
                                    @CSRF.formField
                                    <button type="button" class="btn btn-default" data-dismiss="modal">
                                    <@spring.message "channel.edit.close" />
                                    </button>
                                    <input type="submit" name="delete" value="<@spring.message "general.harddelete" />" class="btn btn-danger">
                                </div>
                            </div>
                        }
                    </div>
                </div>
            </div>
        </#if>
    </#if>
}
