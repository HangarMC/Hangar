<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base>

<#function direction>
	<#if ordering?startsWith("-")>
		<#return "chevron-down">
	<#else>
		<#return "chevron-up">
	</#if>
</#function>

<#function isActiveSort td>
	<#assign sort=ordering>
	<#if sort?startsWith("-")>
		<#assign sort=sort?substring(1)>
	</#if>
	<#return sort?upperCase == td?upperCase>
</#function>

<#assign scriptsVar>
	<script <#--@CSPNonce.attr-->>window.CURRENT_PAGE = ${page};</script>
	<script type="text/javascript" src="<@hangar.url "js/userList.js" />"></script>
</#assign>

<@base.base title="Staff - Hangar" additionalScripts=scriptsVar>
	<#assign UserOrdering = @helper["io.papermc.hangar.model.UserOrdering"]>
<#-- @ftlvariable name="UserOrdering" type="io.papermc.hangar.model.UserOrdering" -->
    <div class="card">
		<table class="table table-users">
			<thead>
				<tr>
					<td></td>
					<td <#if isActiveSort(UserOrdering.UserName)>class="user-sort"</#if> data-list="staff" >
						Username
						<#if isActiveSort(UserOrdering.UserName)><i class="fas o fa-${direction()}"></i></#if>
					</td>
					<td <#if isActiveSort(UserOrdering.Role)>class="user-sort"</#if> data-list="staff" >
						Roles
						<#if isActiveSort(UserOrdering.Role)><i class="fas o fa-${direction()}"></i></#if>
					</td>
					<td <#if isActiveSort(UserOrdering.JoinDate)>class="user-sort"</#if> data-list="staff" >
						Joined
						<#if isActiveSort(UserOrdering.JoinDate)><i class="fas o fa-${direction()}"></i></#if>
					</td>
				</tr>
			</thead>
			<tbody>
				<#list staff as staffUser>
				<tr>
                    <#import "*/utils/userAvatar.ftlh" as userAvatar>
					<td><@userAvatar.userAvatar userName=staffUser.name avatarUrl=utils.avatarUrl(staffUser.name) clazz="user-avatar-xs"></@userAvatar.userAvatar></td>
					<td>
						<a href="${Routes.SHOW_ACTIVITIES.getRouteUrl(staffUser.name)}">${staffUser.name}</a>
					</td>
					<td>
						<span class="user-role channel" style="background-color: ${staffUser.role.color.hex}">
						${staffUser.role.title}
						</span>
					</td>
					<td>${utils.prettifyDate(staffUser.joinDate!staffUser.createdAt)}</td>
				</tr>
				</#list>
				<#-- @ftlvariable name="page" type="java.lang.Integer" -->
				<#-- @ftlvariable name="pageSize" type="java.lang.Integer" -->
				<#if page gt 1 || staff?size gte pageSize>
					<tr class="authors-footer">
						<td></td>
						<td></td>
						<td></td>
						<td>
							<#if staff?size gte pageSize>
								<a href="${Routes.USERS_SHOW_STAFF.getRouteUrl(ordering, page + 1)}" class="btn btn-default">
									<i class="fas fa-arrow-right"></i>
								</a>
							</#if>

							<#if page gt 1>
								<a href="${Routes.USERS_SHOW_STAFF.getRouteUrl(ordering, page - 1)}" class="btn btn-default">
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
