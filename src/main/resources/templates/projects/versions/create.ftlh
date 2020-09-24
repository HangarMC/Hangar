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
    <script type="text/javascript" src="<@hangar.url "build/js/channelManage.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "build/js/pluginUpload.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "build/js/projectDetail.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "build/js/versionCreateChannelNew.js" />"></script>
    <#if pending?? && !pending.dependencies??>
        <script type="text/javascript" src="<@hangar.url "build/js/platform-choice.js" />"></script>
    </#if>

    <script>
             DEFAULT_COLOR = '${config.channels.colorDefault.hex}';
    </script>
</#assign>

<#assign message><@spring.message "version.create.pageTitle" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar>

    <div class="row">
        <div class="${mainWidth}">
            <div class="card card-create">
                <div class="card-header">
                    <h3 class="card-title">
                    <@spring.message "version.create.title" />
                    </h3>
                </div>

                <div class="create-body card-body">
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
                                        <td>
                                            <#if version.versionString??>
                                                ${version.versionString}
                                            <#else>
                                                <div class="form-group">
                                                    <label for="version-string-input" class="sr-only">Version String</label>
                                                    <input id="version-string-input" class="form-control" type="text" form="form-publish" name="versionString" required placeholder="Version">
                                                </div>
                                            </#if>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><strong><@spring.message "version.description" /></strong></td>
                                        <td>
                                            <#if version.versionString??>
                                                <#if version.description?has_content>
                                                    ${version.description}
                                                <#else>
                                                    <#if projectDescription?has_content>
                                                        ${projectDescription}
                                                    <#else>
                                                        <@spring.message "version.create.noDescription" />
                                                    </#if>
                                                </#if>
                                            <#else>
                                                <div class="form-group">
                                                    <label for="version-description-input" class="sr-only">Version Description</label>
                                                    <input type="text" form="form-publish" name="versionDescription" class="form-control" id="version-description-input">
                                                </div>
                                            </#if>
                                        </td>
                                    </tr>
                                    <#if version.fileName?? && !version.externalUrl??>
                                        <tr>
                                            <td><strong><@spring.message "version.filename" /></strong></td>
                                            <td>${version.fileName}</td>
                                        </tr>
                                        <tr>
                                            <td><strong><@spring.message "version.fileSize" /></strong></td>
                                            <td>${utils.formatFileSize(version.fileSize)}</td>
                                        </tr>
                                    <#else>
                                        <tr>
                                            <td><strong><@spring.message "version.externalUrl" /></strong></td>
                                            <td>
                                                <div class="form-group">
                                                    <label for="external-url-input" class="sr-only"></label>
                                                    <input id="external-url-input" class="form-control" type="text" value="${version.externalUrl}" name="externalUrl" form="form-publish" required>
                                                </div>
                                            </td>
                                        </tr>
                                    </#if>
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
                                                <i id="channel-new" class="fas fa-plus" data-toggle="modal" data-target="#channel-settings"></i>
                                            </a>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><strong>Platform</strong></td>
                                        <td>
                                            <#if version.dependencies??>
                                                <div class="float-right" id="upload-platform-tags">
                                                    <#list version.dependenciesAsGhostTags as pair>
                                                        <@projectTag.tagTemplate @helper["io.papermc.hangar.model.viewhelpers.ViewTag"].fromVersionTag(pair.right) pair.left "form-publish" />
                                                    </#list>
                                                </div>
                                            <#else>
                                                <div id="platform-choice"></div>
                                            </#if>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label for="is-unstable-version" class="form-check-label">
                                                <strong><@spring.message "version.create.unstable" /></strong>
                                            </label>
                                        </td>
                                        <td class="rv">
                                            <div class="form-check">
                                                <input id="is-unstable-version" class="form-check-input" form="form-publish" name="unstable" type="checkbox" value="true">
                                            </div>
                                            <div class="clearfix"></div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label for="is-recommended-version" class="form-check-label">
                                                <strong>Recommended</strong>
                                            </label>
                                        </td>
                                        <td class="rv">
                                            <div class="form-check">
                                                <input id="is-recommended-version" class="form-check-input" form="form-publish" name="recommended" type="checkbox" checked value="true">
                                            </div>
                                            <div class="clearfix"></div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label for="create-forum-post-version" class="form-check-label"></label>
                                            <strong>Create forum post</strong>
                                        </td>
                                        <td class="rv">
                                            <div class="form-check">
                                                <#-- @ftlvariable name="forumSync" type="java.lang.Boolean" -->
                                                <input id="create-forum-post-version" class="form-check-input" form="form-publish" name="forum-post" type="checkbox" <#if forumSync> checked </#if> value="true">
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
                        enctype="multipart/form-data" id="form-upload" class="form-inline">
                        <@csrf.formField />
                            <label class="btn btn-info float-left" for="pluginFile">
                                <input id="pluginFile" name="pluginFile" type="file" style="display: none;" accept=".jar,.zip">
                                <@spring.message "version.create.selectFile" />
                            </label>
                            <@alertFile.alertFile />
                    </@form.form>

                    <#if !pending??>
                        <@form.form action=Routes.VERSIONS_CREATE_EXTERNAL_URL.getRouteUrl(ownerName, projectSlug) method="POST" id="form-url-upload" class="form-inline">
                            <@csrf.formField />
                            <div class="input-group float-right" style="width: 50%">
                                <input type="text" class="form-control" id="externalUrl" name="externalUrl" placeholder="<@spring.message "version.create.externalUrl" />" style="width: 70%">
                                <div class="input-group-append">
                                    <button class="btn btn-info" type="submit">Create Version</button>
                                </div>

                            </div>
                        </@form.form>
                    <#else>
                        <#assign version = pending>
                        <#assign formAction>
                            <#if version.versionString??>${Routes.VERSIONS_PUBLISH.getRouteUrl(ownerName, projectSlug, version.versionString)}<#else>${Routes.VERSIONS_PUBLISH_URL.getRouteUrl(ownerName, projectSlug)}</#if>
                        </#assign>
                        <@form.form method="POST" action=formAction id="form-publish" class="float-right">
                            <@csrf.formField />
                            <input type="hidden" class="channel-color-input" name="channel-color-input" value="${config.channels.colorDefault.hex}">
                            <div><input type="submit" name="create" value="<@spring.message "version.create.publish" />" class="btn btn-primary"></div>
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
