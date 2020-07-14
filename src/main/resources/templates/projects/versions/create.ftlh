<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

@import controllers.sugar.Requests.OreRequest
@import models.querymodels.ViewTag
@import ore.OreConfig
@import ore.models.project.Channel
@import ore.models.project.factory.PendingVersion
@import ore.util.FileUtils
@import views.html.helper.{CSPNonce, CSRF, form}
@import views.html.utils.editor
@(projectName: String, pluginId: String, projectSlug: String, ownerName: String, projectDescription: Option[String], forumSync: Boolean, pending: Option[PendingVersion],
  channels: Seq[Channel])(implicit messages: Messages, flash: Flash, request: OreRequest[_], config: OreConfig, assetsFinder: AssetsFinder)

<#assign mainWidth = "col-md-10">

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/channelManage.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/pluginUpload.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/projectDetail.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/versionCreateChannelNew.js" />"></script>
    <script @CSPNonce.attr>
            DEFAULT_COLOR = '${config.defaultChannelColor.hex}';
    </script>
</#assign>

<#assign message><@spring.message "version.create.pageTitle" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar>

    <div class="row">
        <div class="${mainWidth}">
            <div class="panel panel-default panel-create">
                <div class="panel-heading">
                    <h3 class="panel-title">
                    <@spring.message "version.create.title" />
                    </h3>
                </div>

                <div class="create-body panel-body">
                    <div class="minor create-blurb">
                        <span>@Html(messages("version.create.info", projectName, pluginId))</span>
                    </div>

                    <#if pending??>
                        @* Show plugin meta *@
                        @defining(pending.get) { version: PendingVersion =>
                            <div class="plugin-meta">
                                <table class="plugin-meta-table">
                                    <tr>
                                        <td><strong><@spring.message "version" /></strong></td>
                                        <td>${version.versionString}</td>
                                    </tr>
                                    <tr>
                                        <td><strong><@spring.message "version.description" /></strong></td>
                                        <td>
                                        @version.description.map { description =>
                                            ${description}
                                        }.getOrElse {
                                            ${projectDescription!messages("version.create.noDescription")}
                                        }
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><strong><@spring.message "version.filename" /></strong></td>
                                        <td>${version.fileName}</td>
                                    </tr>
                                    <tr>
                                        <td><strong><@spring.message "version.fileSize" /></strong></td>
                                        <td>${FileUtils.formatFileSize(version.fileSize)}</td>
                                    </tr>
                                    <tr>
                                        <td><strong>Channel</strong></td>
                                        <td class="form-inline">
                                            @* Show channel selector if old project, editor if new project *@
                                            <select id="select-channel" form="form-publish" name="channel-input" class="form-control">
                                            @channels.map { channel =>
                                                <option value="@channel.name" data-color="@channel.color.hex" <#if channel.name.equals(version.channelName)>selected</#if>
                                                ${channel.name}
                                                </option>
                                            }
                                            </select>
                                            <a href="#">
                                                <i id="channel-new" class="fas fa-plus" data-toggle="modal"
                                                data-target="#channel-settings"></i>
                                            </a>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><strong>Platform</strong></td>
                                        <td>
                                            <div class="pull-right" id="upload-platform-tags">
                                            <#list version.dependenciesAsGhostTags as t>
                                                ${projects.tag(ViewTag.fromVersionTag(t))}
                                            </#list>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><strong><@spring.message "version.create.unstable" /></strong></td>
                                        <td class="rv">
                                            <div class="checkbox-inline">
                                                <input form="form-publish" name="unstable" type="checkbox" value="true"/>
                                            </div>
                                            <div class="clearfix"></div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td><strong>Recommended</strong></td>
                                        <td class="rv">
                                            <div class="checkbox-inline">
                                                <input form="form-publish" name="recommended" type="checkbox" checked
                                                value="true"/>
                                            </div>
                                            <div class="clearfix"></div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td><strong>Create forum post</strong></td>
                                        <td class="rv">
                                            <div class="checkbox-inline">
                                                <input form="form-publish" name="forum-post" type="checkbox"
                                                <#if forumSync> checked </#if> value="true"/>
                                            </div>
                                            <div class="clearfix"></div>
                                        </td>
                                    </tr>
                                </table>
                            </div>

                            <div class="release-bulletin">
                                <div>
                                    <h3><@spring.message "version.releaseBulletin" /></h3>
                                    <p><@spring.message "version.releaseBulletin.info" /></p>

                                    @editor(
                                        savable = false,
                                        enabled = true,
                                        raw = version.description!"",
                                        cancellable = false,
                                        targetForm = "form-publish"
                                    )
                                </div>
                            </div>
                            <script @CSPNonce.attr>$(function () { $('.btn-edit').click() });</script>
                        }
                    </#if>

                    @form(action = routes.getRouteUrl("versions.upload", ownerName, projectSlug),
                        Symbol("enctype") -> "multipart/form-data", Symbol("id") -> "form-upload") {
                        @CSRF.formField
                            <label class="btn btn-default pull-left" for="pluginFile">
                                <input id="pluginFile" name="pluginFile" type="file" style="display: none;">
                                <@spring.message "version.create.selectFile" />
                            </label>

                            ${projects.helper.alertFile()}
                    }

                    <#if pending??>
                        @* Ready to go! *@
                        @defining(pending.get) { version: PendingVersion =>
                            @form(action = routes.getRouteUrl("versions.publish",
                                ownerName, projectSlug, version.versionString),
                                Symbol("id") -> "form-publish", Symbol("class") -> "pull-right") {
                                @CSRF.formField
                                <input type="hidden" class="channel-color-input" name="channel-color-input"
                                value="@config.defaultChannelColor.hex" />

                                <div>
                                    <input type="submit" name="create" value="<@spring.message "version.create.publish" />"
                                    class="btn btn-primary" />
                                </div>
                            }
                        }
                    </#if>

                </div>
            </div>
            <span class="pull-left tos"><i>@Html(messages("version.create.tos", "#"))</i></span>
        </div>
        <#if pending??>
            ${projects.helper.createSteps(2)}
        <#else>
            ${projects.helper.createSteps(1)}
        </#if>

    </div>

    <@modalManage.modalManage />

</@base.base>
