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
    <script type="text/javascript" src="<@hangar.url "js/channelManage.js" />"></script>
<#--    <script type="text/javascript" src="<@hangar.url "js/pluginUpload.js" />"></script>-->
<#--    <script type="text/javascript" src="<@hangar.url "js/projectDetail.js" />"></script>-->
    <#if pending?? && !pending.dependencies??>
        <script type="text/javascript" src="<@hangar.url "js/platform-choice.js" />"></script>
    </#if>
    <script>
             window.DEFAULT_COLOR = '${config.channels.colorDefault.hex}';
             window.OWNER_NAME = '${ownerName}';
             window.PROJECT_SLUG = '${projectSlug}';
             window.PENDING_VERSION = ${mapper.valueToTree(pending)};
             window.CHANNELS = ${mapper.valueToTree(channels)};
             window.FORUM_SYNC = ${forumSync?c};
    </script>
    <script type="text/javascript" src="<@hangar.url "js/versionCreateChannelNew.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "js/create-version.js" />"></script>
</#assign>
<#assign styleVar>
    <link rel="stylesheet" href="<@hangar.url "css/create-version.css" />">
</#assign>

<#assign message><@spring.message "version.create.pageTitle" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar additionalStyling=styleVar>

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
                        <span><@spring.messageArgs code="version.create.info" args=[projectName] /></span>
                    </div>
                    <div id="create-version"></div>
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
