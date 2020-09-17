<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />
<#import "*/utils/form.ftlh" as form>
<#import "*/utils/csrf.ftlh" as csrf>
<#import "*/projects/channels/helper/modalManage.ftlh" as modalManage />
<#import "*/projects/helper/alertFile.ftlh" as alertFile />
<#import "*/projects/tag.ftlh" as projectTag />
<#import "*/utils/editor.ftlh" as editor />

<#assign mainWidth = "col-md-10">

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/channelManage.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/pluginUpload.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/projectDetail.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/versionCreateChannelNew.js" />"></script>
    <script>
             DEFAULT_COLOR = '${config.channels.colorDefault.hex}';
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
                        <span><@spring.messageArgs code="version.create.info" args=[projectName, pluginId] /></span>
                    </div>

                    <#if pending??>
                        <#-- Show plugin meta -->
                        <#assign version = pending>
                            <div class="plugin-meta">
                                <table class="plugin-meta-table">
                                    <tr>
                                        <td><strong><@spring.message "version" /></strong></td>
                                        <td>${version.versionString}</td>
                                    </tr>
                                    <tr>
                                        <td><strong><@spring.message "version.description" /></strong></td>
                                        <td>
                                            <#if version.description?has_content>
                                                ${version.description}
                                            <#else>
                                                <#if projectDescription?has_content>
                                                    ${projectDescription}
                                                <#else>
                                                    <@spring.message "version.create.noDescription" />
                                                </#if>
                                            </#if>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><strong><@spring.message "version.filename" /></strong></td>
                                        <td>${version.fileName}</td>
                                    </tr>
                                    <tr>
                                        <td><strong><@spring.message "version.fileSize" /></strong></td>
                                        <td>${utils.formatFileSize(version.fileSize)}</td>
                                    </tr>
                                    <tr>
                                        <td><strong>Channel</strong></td>
                                        <td class="form-inline">
                                            <#-- Show channel selector if old project, editor if new project -->
                                            <select id="select-channel" form="form-publish" name="channel-input" class="form-control">
                                            <#list channels as channel>
                                                <option value="${channel.name}" data-color="${channel.color.hex}" <#if channel.name == version.channelName>selected</#if>>
                                                    ${channel.name}
                                                </option>
                                            </#list>
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
                                            <div class="float-right" id="upload-platform-tags">
                                            <#list version.dependenciesAsGhostTags as pair>
                                                <@projectTag.tagTemplate @helper["io.papermc.hangar.model.viewhelpers.ViewTag"].fromVersionTag(pair.right) pair.left "form-publish" />
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
                                                <#-- @ftlvariable name="forumSync" type="java.lang.Boolean" -->
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

                                    <@editor.editor
                                        cooked=markdownService.render(version.description!"")
                                        savable=false
                                        enabled=true
                                        raw=version.description!""
                                        cancellable=false
                                        targetForm="form-publish"
                                    />
                                </div>
                            </div>
                            <script <#--@CSPNonce.attr-->>$(function () { $('.btn-edit').click() });</script>
                    </#if>

                    <@form.form action=Routes.VERSIONS_UPLOAD.getRouteUrl(ownerName, projectSlug) method="POST"
                        enctype="multipart/form-data" id="form-upload">
                        <@csrf.formField />
                            <label class="btn btn-default float-left" for="pluginFile">
                                <input id="pluginFile" name="pluginFile" type="file" style="display: none;" accept=".jar,.zip">
                                <@spring.message "version.create.selectFile" />
                            </label>

                            <@alertFile.alertFile />
                    </@form.form>

                    <#if pending??>
                        <#-- Ready to go! -->
                        <#assign version = pending>
                        <@form.form method="POST" action=Routes.VERSIONS_PUBLISH.getRouteUrl(ownerName, projectSlug, version.versionString)
                            id="form-publish" class="float-right">
                            <@csrf.formField />
                            <input type="hidden" class="channel-color-input" name="channel-color-input"
                            value="${config.channels.colorDefault.hex}" />

                            <div>
                                <input type="submit" name="create" value="<@spring.message "version.create.publish" />"
                                class="btn btn-primary" />
                            </div>
                        </@form.form>
                    </#if>

                </div>
            </div>
            <span class="float-left tos"><i><@spring.messageArgs code="version.create.tos" args=["#"] /></i></span>
        </div>

        <#import "*/projects/helper/createSteps.ftlh" as createSteps>
        <#if pending??>
            <@createSteps.createSteps 2 />
        <#else>
            <@createSteps.createSteps 1 />
        </#if>

    </div>

    <@modalManage.modalManage />

</@base.base>
