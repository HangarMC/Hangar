<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base>

<#--
@(staff: Seq[(String, Role, Option[OffsetDateTime], OffsetDateTime)], ordering: String, page: Int)(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)
-->

@pageSize = @{ config.ore.users.authorPageSize }
@direction = @{ if (ordering.startsWith("-")) "chevron-down" else "chevron-up" }

@isActiveSort(td: String) = @{
	val sort = if (ordering.startsWith("-")) ordering.substring(1) else ordering
	sort.equalsIgnoreCase(td)
}

<#macro scripts>
	<script type="text/javascript" src="<@spring.url "javascripts/userList.js" />"></script>
	<script @CSPNonce.attr>CURRENT_PAGE = @page;</script>
</#macro>

<#assign scriptsVar><@scripts /></#assign>
<@base.base title="Staff - Hangar" additionalScripts=scriptsVar>

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
				<#list staff as user>
				<tr>
					<td>@userAvatar(user.name, User.avatarUrl(name), clazz = "user-avatar-xs")</td>
					<td>
						<a href="@routes.Application.showActivities(name)">${user.name}</a>
					</td>
					<td>
						<span class="user-role channel" style="background-color: ${user.role.color.hex}">
						${user.role.title}
						</span>
					</td>
					<td>@prettifyDate(joinDate.getOrElse(createdAt))</td>
				</tr>
				</#list>

				<#if page gt 1 || staff.size gte pageSize>
					<tr class="authors-footer">
						<td></td>
						<td></td>
						<td></td>
						<td>
							<#if staff.size gte pageSize>
								<a href="@routes.Users.showStaff(Some(ordering), Some(page + 1))" class="btn btn-default">
									<i class="fas fa-arrow-right"></i>
								</a>
							</#if>

							<#if page gt 1>
								<a href="@routes.Users.showStaff(Some(ordering), Some(page - 1))" class="btn btn-default">
									<i class="fas fa-arrow-left"></i>
								</a>
							</#if>
						</td>
					</tr>
				</#if>

			</tbody>
		</table>
	</div>

</@base.base>
