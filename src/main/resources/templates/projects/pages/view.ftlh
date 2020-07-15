<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/projects/view.ftlh" as projectView />
<#import "*/users/memberList.ftlh" as memberList />

<#--
Documentation page within Project overview.
-->

@import java.text.NumberFormat

@import controllers.project.routes
@import controllers.sugar.Requests.OreRequest
@import models.viewhelper.{ProjectData, ScopedProjectData}
@import ore.OreConfig
@import ore.db.Model
@import ore.markdown.MarkdownRenderer
@import ore.models.project.Page
@import ore.permission.Permission
@import util.StringFormatterUtils._
@import util.syntax._
@import views.html.helper.CSPNonce
@import views.html.utils.editor
@(p: ProjectData,
  sp: ScopedProjectData,
  rootPages: Seq[(Model[Page], Seq[Page])],
  page: Page,
  parentPage: Option[Page],
  pageCount: Int,
  editorOpen: Boolean = false)(implicit messages: Messages, request: OreRequest[_], flash: Flash, config: OreConfig, renderer: MarkdownRenderer, assetsFinder: AssetsFinder)

@canEditPages = @{
    sp.perms(Permission.EditPage)
}

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/pageCollapse.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/userSearch.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/memberList.js" />"></script>
    <script @CSPNonce.attr>
            pluginId = '${p.project.pluginId}';
            namespace = '${p.project.namespace}';
    </script>
    <#if editorOpen>
        <script @CSPNonce.attr>$(function() { $('.btn-edit').click(); });</script>
    </#if>
</#assign>

<@projectView.view p=p sp=sp active="#docs" additionalScripts=scriptsVar>
    <div class="row">
        <div class="col-md-9">
            <div class="row">
                <div class="col-md-12">
                    @editor(
                        saveCall = routes.Pages.save(p.project.ownerName, p.project.slug, page.fullSlug(parentPage)),
                        deleteCall = routes.Pages.delete(p.project.ownerName, p.project.slug, page.fullSlug(parentPage)),
                        deletable = !page.isHome,
                        enabled = canEditPages,
                        raw = page.contents,
                        cooked = page.html(Some(p.project)),
                        subject = "Page",
                        extraFormValue = page.name)
                </div>
            </div>
        </div>
        <div class="col-md-3">

            <div class="stats minor">
                <p><@spring.messageArgs "project.category.info", p.project.category.title /></p>
                <p><@spring.messageArgs "project.publishDate", prettifyDate(p.project.createdAt) /></p>
                <p><span id="view-count"></span> views</p>
                <p><span id="star-count"></span> <a href="${routes.getRouteUrl("projects.showStargazers", p.project.ownerName, p.project.slug, "")}">stars</a></p>
                <p><span id="watcher-count"></span> <a href="${routes.getRouteUrl("projects.showWatchers", p.project.ownerName, p.project.slug, "")}">watchers</a></p>
                <p><span id="download-count"></span> total downloads</p>
                <#list p.project.settings.licenseName as licenseName>
                    <p>
                        <@spring.message "project.license.link" />
                        <a target="_blank" rel="noopener" href="${p.project.settings.licenseUrl}">${licenseName}</a>
                    </p>
                </#list>
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
                    <#if canEditPages && pageCount < config.ore.projects.maxPages>
                        <button data-toggle="modal" data-target="#new-page" title="New"
                        class="new-page btn yellow btn-xs pull-right">
                            <i class="fas fa-plus"></i>
                        </button>
                        @projects.pages.modalPageCreate(p.project, rootPages.map(_._1))
                    </#if>
                </div>
                <ul class="list-group">
                    <li class="list-group-item">
                        <a href="${routes.getRouteUrl("pages.show", p.project.ownerName, p.project.slug, Page.homeName)}">
                        ${Page.homeName}
                        </a>
                    </li>
                    @rootPages.filter(_._1.name != Page.homeName).map { case (pg, children) =>
                    <li class="list-group-item">
                        <#if children.nonEmpty>
                            <#if !page.parentId.contains(pg.id.value)>
                                <a class="page-expand" data-page-id="${pg.id}">
                                    <i class="far fa-plus-square"></i>
                                </a>
                            <#else>
                                <a class="page-collapse" data-page-id="${pg.id}">
                                    <i class="far fa-minus-square"></i>
                                </a>
                            </#if>
                        </#if>
                    <a href="${routes.getRouteUrl("pages.show", p.project.ownerName, p.project.slug, pg.fullSlug(""))}">
                    ${pg.name}
                    </a>
                    </li>
                        <#if page.parentId.contains(pg.id)>
                            <div class="page-children" data-page-id="${pg.id}">
                            @children.map { child =>
                                <li class="list-group-item page-item-child">
                                    <a href="${routes.getRouteUrl("pages.show", p.project.ownerName, p.project.slug, child.fullSlug(pg))}">
                                    ${child.name}
                                    </a>
                                </li>
                            }
                            </div>
                        </#if>
                    }
                </ul>
            </div>

                <!-- Member list -->
            <@memberList.memberList j=p perms=sp.permissions settingsCall=routes.getRouteUrl("projects.showSettings", p.project.ownerName, p.project.slug) />
        </div>
    </div>
</@projectView.view>
