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
@(staff: Seq[(String, Role, Option[OffsetDateTime], OffsetDateTime)], ordering: String, page: Int)(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)

@pageSize = @{ config.ore.users.authorPageSize }
@direction = @{ if (ordering.startsWith("-")) "chevron-down" else "chevron-up" }

@isActiveSort(td: String) = @{
	val sort = if (ordering.startsWith("-")) ordering.substring(1) else ordering
	sort.equalsIgnoreCase(td)
}

@scripts = {
	<script type="text/javascript" src="@assetsFinder.path("javascripts/userList.js")"></script>
	<script @CSPNonce.attr>CURRENT_PAGE = @page;</script>
}

@layout.base("Staff - Hangar", scripts) {

	<div class="panel panel-default">
		<table class="table table-users">
			<thead>
				<tr>
					<td></td>
					<td @if(isActiveSort(UserOrdering.UserName)) { class="user-sort" } data-list="staff" >
						Username
						@if(isActiveSort(UserOrdering.UserName)) { <i class="fas o fa-@direction"></i> }
					</td>
					<td @if(isActiveSort(UserOrdering.Role)) { class="user-sort" } data-list="staff" >
						Roles
						@if(isActiveSort(UserOrdering.Role)) { <i class="fas o fa-@direction"></i> }
					</td>
					<td @if(isActiveSort(UserOrdering.JoinDate)) { class="user-sort" } data-list="staff" >
						Joined
						@if(isActiveSort(UserOrdering.JoinDate)) { <i class="fas o fa-@direction"></i> }
					</td>
				</tr>
			</thead>
			<tbody>
				@staff.map { case (name, role, joinDate, createdAt) =>
				<tr>
					<td>@userAvatar(Some(name), User.avatarUrl(name), clazz = "user-avatar-xs")</td>
					<td>
						<a href="@routes.Application.showActivities(name)">@name</a>
					</td>
					<td>
						<span class="user-role channel" style="background-color: @role.color.hex">
						@role.title
						</span>
					</td>
					<td>@prettifyDate(joinDate.getOrElse(createdAt))</td>
				</tr>
				}

				@if(page > 1 || staff.size >= pageSize) {
					<tr class="authors-footer">
						<td></td>
						<td></td>
						<td></td>
						<td>
							@if(staff.size >= pageSize) {
								<a href="@routes.Users.showStaff(Some(ordering), Some(page + 1))" class="btn btn-default">
									<i class="fas fa-arrow-right"></i>
								</a>
							}

							@if(page > 1) {
								<a href="@routes.Users.showStaff(Some(ordering), Some(page - 1))" class="btn btn-default">
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
