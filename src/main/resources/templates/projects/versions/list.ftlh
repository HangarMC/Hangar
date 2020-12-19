<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/projects/view.ftlh" as projects />

<#--
Versions page within Project overview.
-->
<#assign Permission=@helper["io.papermc.hangar.model.Permission"]>
<#assign Platform=@helper["io.papermc.hangar.model.Platform"]>
<#assign scriptsVar>
    <script nonce="${nonce}">
            <#outputformat "JavaScript">
            window.PROJECT_OWNER = '${p.project.ownerName}';
            window.PROJECT_SLUG = '${p.project.slug}';
            window.CHANNELS = ${mapper.valueToTree(channels)};
            window.PLATFORMS = ${mapper.valueToTree(Platform.getValues())}
            window.CAN_EDIT_CHANNELS = ${sp.perms(Permission.EditTags)?c};
            window.FILTERED_MEMBERS = ${utils.serializeMap(p.filteredMembers(headerData))};
            window.CAN_MANAGE_MEMBERS = ${sp.permissions.has(Permission.ManageSubjectMembers)?c};
            </#outputformat>
    </script>
    <script type="text/javascript" src="${hangar.url("js/version-list-view.js")}"></script>
</#assign>
<#assign stylesheetsVar>
    <link rel="stylesheet" type="text/css" href="${hangar.url("css/version-list-view.css")}" />
</#assign>
<@projects.view p=p sp=sp active="#versions" additionalScripts=scriptsVar additionalStyling=stylesheetsVar>
    <div id="version-list-view">
        <div class="row">
            <div class="col-md-9">
                <#--placeholder-->
            </div>
            <div class="col-md-3">
                <div class="card channels">
                    <div class="card-header">
                        <h3 class="card-title float-left">Channels</h3>
                        <input type="checkbox" class="float-right channels-all" checked />
                    </div>

                    <ul class="list-group list-channel">
                        <#list channels as channel>
                            <li class="list-group-item">
                                <span class="channel" style="background-color: ${channel.color.hex}">${channel.name}</span>
                                <input type="checkbox" class="float-right" checked/>
                            </li>
                        </#list>

                        <#if sp.perms(Permission.EditTags)>
                            <li class="list-group-item text-right">
                                <a class="btn btn-sm bg-warning" href="${Routes.CHANNELS_SHOW_LIST.getRouteUrl(p.project.ownerName, p.project.slug)}">
                                    Edit
                                </a>
                            </li>
                        </#if>
                    </ul>
                </div>
                <#import "*/users/memberList.ftlh" as users>
                <@users.memberList project=p perms=sp.permissions settingsCall=Routes.PROJECTS_SHOW_SETTINGS.getRouteUrl(p.project.ownerName, p.project.slug)/>
            </div>
        </div>
    </div>
</@projects.view>
