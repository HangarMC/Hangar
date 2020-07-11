@import java.time.OffsetDateTime

@import controllers.sugar.Requests.OreRequest
@import db.impl.access.UserBase.UserOrdering
@import ore.OreConfig
@import ore.models.user.User
@import ore.permission.role.Role
@import util.StringFormatterUtils._
@import util.syntax._
@import views.html.helper.CSPNonce
@import views.html.utils.userAvatar
@(authors: Seq[(String, Option[OffsetDateTime], OffsetDateTime, Option[Role], Option[Role], Long)], ordering: String, page: Int)(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)

@pageSize = @{ config.ore.users.authorPageSize }
@direction = @{ if (ordering.startsWith("-")) "chevron-down" else "chevron-up" }

@isActiveSort(td: String) = @{
    var sort = ordering
    if (sort.startsWith("-")) sort = sort.substring(1)
    sort.equalsIgnoreCase(td)
}

@scripts = {
    <script type="text/javascript" src="@assetsFinder.path("javascripts/userList.js")"></script>
    <script @CSPNonce.attr>CURRENT_PAGE = @page;</script>
}

@layout.base("Authors - Hangar", scripts) {

    <div class="panel panel-default">
        <table class="table table-users">
            <thead>
                <tr>
                    <td></td>
                    <td @if(isActiveSort(UserOrdering.UserName)) { class="user-sort" } data-list="authors" >
                        Username
                        @if(isActiveSort(UserOrdering.UserName)) { <i class="o fas fa-@direction"></i> }
                    </td>
                    <td @if(isActiveSort(UserOrdering.Role)) { class="user-sort" } data-list="authors" >
                        Roles
                        @if(isActiveSort(UserOrdering.Role)) { <i class="o fas fa-@direction"></i> }
                    </td>
                    <td @if(isActiveSort(UserOrdering.JoinDate)) { class="user-sort" } data-list="authors" >
                        Joined
                        @if(isActiveSort(UserOrdering.JoinDate)) { <i class="o fas fa-@direction"></i> }
                    </td>
                    <td @if(isActiveSort(UserOrdering.Projects)) { class="user-sort" } data-list="authors" >
                        Projects
                        @if(isActiveSort(UserOrdering.Projects)) { <i class="o fas fa-@direction"></i> }
                    </td>
                </tr>
            </thead>
            <tbody>
                @authors.map { case (name, joinDate, createdAt, optRole, optDonorRole, projectCount) =>
                <tr>
                    <td>@userAvatar(Some(name), User.avatarUrl(name), clazz = "user-avatar-xs")</td>
                    <td>
                        <a href="@routes.Users.showProjects(name)">@name</a>
                    </td>
                    <td>
                        @optDonorRole.map { role =>
                            <span class="channel channel-sm" style="background-color: @role.color.hex">
                            @role.title
                            </span>
                        }
                        @optRole.map { role =>
                            <span class="user-role channel" style="background-color: @role.color.hex">
                            @role.title
                            </span>
                        }

                    </td>
                    <td>@prettifyDate(joinDate.getOrElse(createdAt))</td>
                    <td>@projectCount</td>
                </tr>
                }

                @if(page > 1 || authors.size >= pageSize) {
                    <tr class="authors-footer">
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td>
                            @if(authors.size >= pageSize) {
                                <a href="@routes.Users.showAuthors(Some(ordering), Some(page + 1))" class="btn btn-default">
                                    <i class="fas fa-arrow-right"></i>
                                </a>
                            }

                            @if(page > 1) {
                                <a href="@routes.Users.showAuthors(Some(ordering), Some(page - 1))" class="btn btn-default">
                                    <i class="fas fa-arrow-left"></i>
                                </a>
                            }
                        </td>
                    </tr>
                }

            </tbody>
        </table>
    </div>

}
