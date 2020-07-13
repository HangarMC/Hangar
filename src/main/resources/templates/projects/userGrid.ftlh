<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

@import controllers.sugar.Requests.OreRequest
@import models.viewhelper.{ProjectData, ScopedProjectData}
@import ore.OreConfig
@import ore.markdown.MarkdownRenderer
@import ore.models.user.User
@import util.syntax._
@(title: String, call: Int => Call, p: ProjectData, sp: ScopedProjectData, users: Seq[User], page: Int, pageSize: Int)(implicit messages: Messages, request: OreRequest[_], flash: Flash, config: OreConfig, renderer: MarkdownRenderer, assetsFinder: AssetsFinder)

<#assign columns = 3>

@projects.view(p, sp, "") {
    <div class="row user-grid">
        <div class="col-xs-12">
            <h3 class="text-bold mb-0">@title</h3>
            <div class="extra-divider"></div>
        </div>

        <#list 1..columns as i>
            <div class="col-md-4">
            @users.iterator.zipWithIndex.filter(_._2 % columns == div).map(_._1).map { user =>
                <div class="media">
                    <div class="media-left">
                        @views.html.utils.userAvatar(
                            userName = Some(user.name),
                            avatarUrl = user.avatarUrl,
                            clazz = "user-avatar-sm media-object"
                        )
                    </div>
                    <div class="media-body center-vertically">
                        <a class="media-heading mb-0" href="${routes.getRouteUrl("users.showProjects", user.name)}">${user.name}</a>
                    </div>
                </div>
            }
            </div>
        </#list>

        <#if p.starCount gte pageSize || page gt 1>
            <div class="col-xs-12">
                <div class="pull-right next-back">
                    <#if page gt 1>
                        <a href="@call(page - 1)" class="btn btn-default"><i class="fa fa-arrow-left"></i></a>
                    </#if>
                    <#if p.starCount gt pageSize * page>
                        <a href="@call(page + 1)" class="btn btn-default"><i class="fa fa-arrow-right"></i></a>
                    </#if>
                </div>
            </div>
        </#if>
    </div>
}
