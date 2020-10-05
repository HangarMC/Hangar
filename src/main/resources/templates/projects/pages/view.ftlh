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
    <script>
        window.PAGE = ${mapper.valueToTree(projectPage)};
        window.ROOT_PAGES = ${utils.serializeMap(rootPages)};
        window.PAGE_COUNT = ${pageCount};
        window.EDITOR_OPEN = ${(editorOpen!false)?c};
        window.JOINABLE = ${mapper.valueToTree(p)};
    </script>
    <script type="text/javascript" src="<@hangar.url "js/memberList.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "js/project-view.js" />"></script>
</#assign>

<#assign styleVar>
    <link rel="stylesheet" href="<@hangar.url "css/project-view.css" />">
</#assign>

<@projectView.view p=p sp=sp active="#docs" additionalScripts=scriptsVar additionalStyling=styleVar>
    <div id="project-view">
        <div class="row">
            <div class="col-12">
                <div class="page-preview page-rendered">${markdownService.render(projectPage.contents)}</div>
            </div>
        </div>
    </div>
    <div id="member-list" style="display: none">
        <@memberList.memberList project=p perms=sp.permissions settingsCall=Routes.PROJECTS_SHOW_SETTINGS.getRouteUrl(p.project.ownerName, p.project.slug) />
    </div>
</@projectView.view>
