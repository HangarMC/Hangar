@import controllers.sugar.Requests.OreRequest
@import models.viewhelper.{ProjectData, ScopedProjectData}
@import ore.OreConfig
@import ore.markdown.MarkdownRenderer
@import ore.models.user.User
@import util.syntax._
@(title: String, call: Int => Call, p: ProjectData, sp: ScopedProjectData, users: Seq[User], page: Int, pageSize: Int)(implicit messages: Messages, request: OreRequest[_], flash: Flash, config: OreConfig, renderer: MarkdownRenderer, assetsFinder: AssetsFinder)

@columns = @{
    3
}

@projects.view(p, sp, "") {
    <div class="row user-grid">
        <div class="col-xs-12">
            <h3 class="text-bold mb-0">@title</h3>
            <div class="extra-divider"></div>
        </div>

        @for(div <- 0 until columns) {
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
                        <a class="media-heading mb-0" href="@controllers.routes.Users.showProjects(user.name)">@user.name</a>
                    </div>
                </div>
            }
            </div>
        }

        @if(p.starCount >= pageSize || page > 1) {
            <div class="col-xs-12">
                <div class="pull-right next-back">
                    @if(page > 1) {
                        <a href="@call(page - 1)" class="btn btn-default"><i class="fa fa-arrow-left"></i></a>
                    }
                    @if(p.starCount > pageSize * page) {
                        <a href="@call(page + 1)" class="btn btn-default"><i class="fa fa-arrow-right"></i></a>
                    }
                </div>
            </div>
        }
    </div>
}
