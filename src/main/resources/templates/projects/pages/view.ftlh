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
<#function canEditPages>
    <#return sp.perms(Permission.EditPage)>
</#function>

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/pageCollapse.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/userSearch.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/memberList.js" />"></script>
    <script> <#--@CSPNonce.attr-->
            pluginId = '${p.getProject().pluginId}';
            namespace = '${p.getFullSlug()}';
    </script>
    <#if editorOpen>
        <script <#--@CSPNonce.attr-->>$(function() { $('.btn-edit').click(); });</script>
    </#if>
</#assign>

<@projectView.view p=p sp=sp active="#docs" additionalScripts=scriptsVar>
    <div class="row">
        <div class="col-md-9">
            <div class="row">
                <div class="col-md-12">
                    <@editor.editor
                        saveCall=Routes.PAGES_SAVE.getRouteUrl(p.project.ownerName, p.project.slug, projectPage.slug)
                        deleteCall=Routes.PAGES_DELETE.getRouteUrl(p.project.ownerName, p.project.slug, projectPage.slug)
                        deletable=projectPage.isDeletable
                        enabled=canEditPages()
                        raw=projectPage.contents
                        cooked=markdownService.render(projectPage.contents)
                        subject="Page"
                        extraFormValue=projectPage.name />
                </div>
            </div>
        </div>
        <div class="col-md-3">

            <#if p.project.recommendedVersionId??>
            <div class="btn-group btn-download">
                <a href="${Routes.VERSIONS_DOWNLOAD_RECOMMENDED.getRouteUrl(p.project.ownerName, p.project.slug, "")}"
                   title="<@spring.message "project.download.recommend" />" data-toggle="tooltip"
                   data-placement="bottom" class="btn btn-primary">
                    <i class="fas fa-download"></i> <@spring.message "general.download" />
                </a>
                <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    <span class="caret"></span>
                    <span class="sr-only">Toggle Dropdown</span>
                </button>
                <div class="dropdown-menu dropdown-menu-right">
                    <a href="${Routes.VERSIONS_DOWNLOAD_RECOMMENDED.getRouteUrl(p.project.ownerName, p.project.slug, "")}" class="dropdown-item"><@spring.message "general.download" /></a>
                    <a href="#" class="copy-url dropdown-item" data-clipboard-text="${config.baseUrl}${Routes.VERSIONS_DOWNLOAD_RECOMMENDED.getRouteUrl(p.project.ownerName, p.project.slug, "")}">Copy URL</a>
                </div>
            </div>
            </#if>

            <div class="stats minor">
                <p><@spring.messageArgs code="project.category.info" args=[p.project.category.title] /></p>
                <p><@spring.messageArgs code="project.publishDate" args=[utils.prettifyDate(p.project.createdAt)] /></p>
                <p><span id="view-count"></span> views</p>
                <p><span id="star-count"></span> <a href="${Routes.PROJECTS_SHOW_STARGAZERS.getRouteUrl(p.project.ownerName, p.project.slug, "")}">star<#if p.starCount != 1>s</#if></a></p>
                <p><span id="watcher-count"></span> <a href="${Routes.PROJECTS_SHOW_WATCHERS.getRouteUrl(p.project.ownerName, p.project.slug, "")}">watcher<#if p.watcherCount != 1>s</#if></a></p>
                <p><span id="download-count"></span> total downloads</p>
                <#if p.project.licenseName?has_content && p.project.licenseUrl?has_content>
                    <p>
                      <@spring.message "project.license.link" />
                      <a target="_blank" rel="noopener" href="${p.project.licenseUrl}">${p.project.licenseName}</a>
                    </p>
                </#if>
            </div>

            <div class="card">
                <div class="card-header">
                    <h3 class="card-title"><@spring.message "project.promotedVersions" /></h3>
                </div>
                <ul class="list-group promoted-list">

                </ul>
            </div>

            <div class="card \">
                <div class="card-header">
                    <h3 class="float-left card-title"><@spring.message "page.plural" /></h3>
                    <#if canEditPages() && pageCount < config.projects.maxPages>
                        <button data-toggle="modal" data-target="#new-page" title="New"
                        class="new-page btn yellow btn-sm float-right">
                            <i class="fas fa-plus"></i>
                        </button>
                        <@pageCreate.pageCreate p.project rootPages />
                    </#if>
                </div>
                <ul class="list-group">
<#--                    <li class="list-group-item">-->
<#--                        <a href="${Routes.PAGES_SHOW.getRouteUrl(p.project.ownerName, p.project.slug, config.pages.home.name)}">-->
<#--                        ${"Home"}-->
<#--                        </a>-->
<#--                    </li>-->
                    <#-- @ftlvariable name="pg" type="io.papermc.hangar.model.viewhelpers.ProjectPage" -->
                    <#-- @ftlvariable name="children" type="java.util.List<ProjectPage>" -->
                    <#list rootPages as pg, children>
                        <li class="list-group-item">
                            <#if children?size gt 0>
                                <#if projectPage.parentId?? && projectPage.parentId = pg.id>
                                    <a class="page-collapse" data-page-id="${pg.id}">
                                        <i class="far fa-minus-square"></i>
                                    </a>
                                <#else>
                                    <a class="page-expand" data-page-id="${pg.id}">
                                        <i class="far fa-plus-square"></i>
                                    </a>
                                </#if>
                            </#if>
                            <a href="${Routes.PAGES_SHOW.getRouteUrl(p.project.ownerName, p.project.slug, pg.slug)}" class="href">
                                ${pg.name}
                            </a>
                        </li>
                        <#if projectPage.parentId?? && projectPage.parentId = pg.id>
                            <div class="page-children" data-page-id="${pg.id}">
                                <#-- @ftlvariable name="childPage" type="io.papermc.hangar.model.viewhelpers.ProjectPage" -->
                                <#list children as childPage>
                                    <li class="list-group-item page-item-child">
                                        <a href="${Routes.PAGES_SHOW.getRouteUrl(p.project.ownerName, p.project.slug, childPage.slug)}">
                                            ${childPage.name}
                                        </a>
                                    </li>
                                </#list>
                            </div>
                        </#if>
                    </#list>
                </ul>
            </div>

                <!-- Member list -->
            <@memberList.memberList project=p perms=sp.permissions settingsCall=Routes.PROJECTS_SHOW_SETTINGS.getRouteUrl(p.project.ownerName, p.project.slug) />
        </div>
    </div>
</@projectView.view>
