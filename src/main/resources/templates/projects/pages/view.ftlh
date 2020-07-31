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


<#assign Permission=@helper["me.minidigger.hangar.model.Permission"]>
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
                    <#-- @ftlvariable name="page" type="me.minidigger.hangar.model.viewhelpers.ProjectPage" -->
                    <@editor.editor
                        saveCall=routes.getRouteUrl("pages.save", p.project.ownerName, p.project.slug, page.slug)
                        deleteCall=routes.getRouteUrl("pages.delete", p.project.ownerName, p.project.slug, page.slug)
                        deletable=page.deletable
                        enabled=canEditPages()
                        raw=page.contents
                        cooked=markdownService.render(page.contents)
                        subject="Page"
                        extraFormValue=page.name />
                </div>
            </div>
        </div>
        <div class="col-md-3">

            <div class="stats minor">
                <p><@spring.messageArgs code="project.category.info" args=[p.project.category.title] /></p>
                <p><@spring.messageArgs code="project.publishDate" args=[p.project.createdAt.format("yyyy-MM-dd")] /></p>
                <p><span id="view-count"></span> views</p>
                <p><span id="star-count"></span> <a href="${routes.getRouteUrl("projects.showStargazers", p.project.ownerName, p.project.slug, "")}">star<#if p.starCount != 1>s</#if></a></p>
                <p><span id="watcher-count"></span> <a href="${routes.getRouteUrl("projects.showWatchers", p.project.ownerName, p.project.slug, "")}">watcher<#if p.watcherCount != 1>s</#if></a></p>
                <p><span id="download-count"></span> total downloads</p>
                <#if p.project.licenseName?has_content && p.project.licenseUrl?has_content>
                    <p>
                      <@spring.message "project.license.link" />
                      <a target="_blank" rel="noopener" href="${p.project.licenseUrl}">${p.project.licenseName}</a>
                    </p>
                </#if>
            </div>

            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title"><@spring.message "project.promotedVersions" /></h3>
                </div>
                <ul class="list-group promoted-list">

                </ul>
            </div>

            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="pull-left panel-title"><@spring.message "page.plural" /></h3>
                    <#if canEditPages() && pageCount < config.projects.maxPages>
                        <button data-toggle="modal" data-target="#new-page" title="New"
                        class="new-page btn yellow btn-xs pull-right">
                            <i class="fas fa-plus"></i>
                        </button>
                        <@pageCreate.pageCreate p.project rootPages /> <#-- TODO this is showing when user logged out -->
                    </#if>
                </div>
                <ul class="list-group">
<#--                    <li class="list-group-item">-->
<#--                        <a href="${routes.getRouteUrl("pages.show", p.project.ownerName, p.project.slug, config.pages.home.name)}">-->
<#--                        ${"Home"}-->
<#--                        </a>-->
<#--                    </li>-->
                    <#-- @ftlvariable name="pg" type="me.minidigger.hangar.model.viewhelpers.ProjectPage" -->
                    <#-- @ftlvariable name="children" type="java.util.List<ProjectPage>" -->
                    <#list rootPages as pg, children>
                        <li class="list-group-item">
                            <#if children?size gt 0>
                                <#if page.parentId?? && page.parentId = pg.id>
                                    <a class="page-collapse" data-page-id="${pg.id}">
                                        <i class="far fa-minus-square"></i>
                                    </a>
                                <#else>
                                    <a class="page-expand" data-page-id="${pg.id}">
                                        <i class="far fa-plus-square"></i>
                                    </a>
                                </#if>
                            </#if>
                            <a href="${routes.getRouteUrl("pages.show", p.project.ownerName, p.project.slug, pg.slug)}" class="href">
                                ${pg.name}
                            </a>
                        </li>
                        <#if page.parentId?? && page.parentId = pg.id>
                            <div class="page-children" data-page-id="${pg.id}">
                                <#-- @ftlvariable name="childPage" type="me.minidigger.hangar.model.viewhelpers.ProjectPage" -->
                                <#list children as childPage>
                                    <li class="list-group-item page-item-child">
                                        <a href="${routes.getRouteUrl("pages.show", p.project.ownerName, p.project.slug, childPage.slug)}">
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
            <@memberList.memberList j=p perms=sp.permissions settingsCall=routes.getRouteUrl("projects.showSettings", p.project.ownerName, p.project.slug) />
        </div>
    </div>
</@projectView.view>
