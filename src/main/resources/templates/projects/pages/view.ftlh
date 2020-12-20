<#-- @ftlvariable name="pageCount" type="java.lang.Integer" -->
<#-- @ftlvariable name="editorOpen" type="java.lang.Boolean" -->
<#-- @ftlvariable name="rootPages" type="java.util.Map<ProjectPage, java.util.List<ProjectPage>>" -->
<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/projects/view.ftlh" as projectView />
<#import "*/users/memberList.ftlh" as memberList />
<#import "*/utils/editor.ftlh" as editor />
<#import "*/pages/modalPageCreate.ftlh" as pageCreate />

<#--
Documentation page within Project overview.
-->

<#assign Permission=@helper["io.papermc.hangar.model.Permission"]>

<#assign scriptsVar>
    <script nonce="${nonce}">
        <#outputformat "JavaScript">
        window.PAGE = ${mapper.valueToTree(projectPage)};
        window.ROOT_PAGES = ${utils.serializeMap(rootPages)};
        window.PAGE_COUNT = ${pageCount};
        window.EDITOR_OPEN = ${(editorOpen!false)?c};

        window.FILTERED_MEMBERS = ${utils.serializeMap(p.filteredMembers(headerData))};
        window.CAN_MANAGE_MEMBERS = ${sp.permissions.has(Permission.ManageSubjectMembers)?c};
        </#outputformat>
    </script>
    <script nonce="${nonce}" type="text/javascript" src="${hangar.url("js/project-view.js")}"></script>
    <script nonce="${nonce}" type="text/javascript" src="${hangar.url("ext/js/admonition.js")}"></script>
</#assign>

<#assign styleVar>
    <link rel="stylesheet" href="${hangar.url("css/project-view.css")}">
    <link rel="stylesheet" href="${hangar.url("ext/css/admonition.css")}">
</#assign>

<@projectView.view p=p sp=sp active="#docs" additionalScripts=scriptsVar additionalStyling=styleVar>
    <div id="project-view">
        <div class="row">
            <div class="col-lg-8 col-12">
                <div class="page-preview page-rendered"><#outputformat "plainText">${markdownService.render(projectPage.contents)}</#outputformat></div>
            </div>
            <div class="col-lg-4 col-12">
                <@memberList.memberList project=p perms=sp.permissions settingsCall=Routes.PROJECTS_SHOW_SETTINGS.getRouteUrl(p.project.ownerName, p.project.slug) />
            </div>
        </div>
    </div>
</@projectView.view>
