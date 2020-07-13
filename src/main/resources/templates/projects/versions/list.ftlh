<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

@*
Versions page within Project overview.
*@
@import controllers.sugar.Requests.OreRequest
@import models.viewhelper.{ProjectData, ScopedProjectData}
@import ore.OreConfig
@import ore.markdown.MarkdownRenderer
@import ore.models.project.Channel
@import ore.permission.Permission
@import views.html.helper.CSPNonce
@(p: ProjectData,
        sp: ScopedProjectData,
        channels: Seq[Channel])(implicit messages: Messages, request: OreRequest[_], flash: Flash, config: OreConfig, renderer: MarkdownRenderer, assetsFinder: AssetsFinder)

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/userSearch.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/memberList.js" />"></script>
    <script @CSPNonce.attr>
            window.PLUGIN_ID = '${p.project.pluginId}';
            window.PROJECT_OWNER = '${p.project.ownerName}';
            window.PROJECT_SLUG = '${p.project.slug}';
    </script>
    <script type="text/javascript" src="<@hangar.url "build/version-list.js" />"></script>
</#assign>

<#assign stylesheetsVar>
    <link rel="stylesheet" type="text/css" href="<@hangar.url "build/version-list.css" />" />
</#assign>

@projects.view(p, sp, "#versions", additionalScripts = scripts, additionalStyling = stylesheets) {

    <div class="row">
        <div class="col-md-9">
            <div id="version-list"></div>
        </div>

        <div class="col-md-3">
            <div class="panel panel-default channels">
                <div class="panel-heading">
                    <h3 class="panel-title pull-left">Channels</h3>
                    <input type="checkbox" class="pull-right channels-all" { checked } />
                </div>

                    <!-- Channel list -->
                <ul class="list-group list-channel">
                    @channels.sorted.map { channel =>
                        <li class="list-group-item">
                            <span class="channel" style="background-color: ${channel.color.hex}">${channel.name}</span>
                            <input type="checkbox" class="pull-right"/>
                        </li>
                    }

                    <#if sp.perms(Permission.EditChannel)>
                        <li class="list-group-item text-right">
                            <a class="btn yellow btn-xs" href="${routes.getRouteUrl("channels.showList", p.project.ownerName, p.project.slug)}">
                                Edit
                            </a>
                        </li>
                    </#if>
                </ul>
            </div>
            @users.memberList(
                j = p,
                perms = sp.permissions,
                settingsCall = ${routes.getUrl("projects.showSettings", p.project.ownerName, p.project.slug)}
            )
        </div>
    </div>

}
