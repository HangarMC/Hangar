<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#--
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
-->

<#function isActiveSort td>
    var sort = ordering
    if (sort.startsWith("-")) sort = sort.substring(1)
    <#return sort.equalsIgnoreCase(td)>
</#function>

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/userList.js" />"></script>
    <script @CSPNonce.attr>CURRENT_PAGE = ${page};</script>
</#assign>

<@base.base title="Authors - Hangar" additionalScripts=scriptsVar>
    <div class="panel panel-default">
        <table class="table table-users">
            <thead>
                <tr>
                    <td></td>
                    <td <#if isActiveSort(UserOrdering.UserName)>class="user-sort"</#if> data-list="authors" >
                        Username
                        <#if isActiveSort(UserOrdering.UserName)><i class="o fas fa-@direction"></i></#if>
                    </td>
                    <td <#if isActiveSort(UserOrdering.Role)>class="user-sort"</#if> data-list="authors" >
                        Roles
                        <#if isActiveSort(UserOrdering.Role)><i class="o fas fa-@direction"></i></#if>
                    </td>
                    <td <#if isActiveSort(UserOrdering.JoinDate)>class="user-sort"</#if> data-list="authors" >
                        Joined
                        <#if isActiveSort(UserOrdering.JoinDate)><i class="o fas fa-@direction"></i></#if>
                    </td>
                    <td <#if isActiveSort(UserOrdering.Projects)>class="user-sort"</#if> data-list="authors" >
                        Projects
                        <#if isActiveSort(UserOrdering.Projects)><i class="o fas fa-${direction}"></i></#if>
                    </td>
                </tr>
            </thead>
            <tbody>
                @authors.map { case (name, joinDate, createdAt, optRole, optDonorRole, projectCount) =>
                <tr>
                    <td>@userAvatar(Some(name), User.avatarUrl(name), clazz = "user-avatar-xs")</td>
                    <td>
                        <a href="${routes.getRouteUrl("users.showProjects", name)}">${name}</a>
                    </td>
                    <td>
                        @optDonorRole.map { role =>
                            <span class="channel channel-sm" style="background-color: ${role.color.hex}">
                            @role.title
                            </span>
                        }
                        @optRole.map { role =>
                            <span class="user-role channel" style="background-color: ${role.color.hex}">
                            @role.title
                            </span>
                        }

                    </td>
                    <td>@prettifyDate(joinDate!createdAt)</td>
                    <td>${projectCount}</td>
                </tr>
                }

                <#if page gt 1 || authors.size gte pageSize>
                    <tr class="authors-footer">
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td>
                            <#if authors.size gte pageSize>
                                <a href="${routes.getRouteUrl("users.showAuthors", ordering, page + 1)}" class="btn btn-default">
                                    <i class="fas fa-arrow-right"></i>
                                </a>
                            </#if>

                            <#if page gt 1>
                                <a href="${routes.getRouteUrl("users.showAuthors", ordering, page - 1)}" class="btn btn-default">
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
