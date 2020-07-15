<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

@import controllers.sugar.Requests.OreRequest
@import ore.OreConfig
@import ore.db.Model
@import ore.markdown.MarkdownRenderer
@import ore.models.admin.VersionVisibilityChange
@import ore.models.project.{Project, Version}
@import ore.models.user.User
@import util.StringFormatterUtils._
@import util.syntax._

@(project: Project, version: Version, visibilityChanges: Seq[(Model[VersionVisibilityChange], Option[User])])(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, renderer: MarkdownRenderer, assetsFinder: AssetsFinder)

<#assign message><@spring.messageArgs "version.log.logger.title" project.namespace /></#assign>
<@base.base title=message>
	<div class="row">
		<div class="col-md-12">
			<h1><@spring.message "version.log.visibility.title" /> <a href="${routes.getRouteUrl("versions.show", project.ownerName, project.slug, version.versionString)}">${project.ownerName}/${project.slug}/versions/${version.versionString}</a></h1>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
			<div class="panel panel-default">
				<div class="panel-heading">
					<h4 class="panel-title pull-left"><@spring.message "version.log.visibility.title" /></h4>
					<div class="clearfix"></div>
				</div>
				<table class="table table-condensed setting-no-border table-review-log">
					<thead>
						<tr>
							<th>State</th>
							<th>Time</th>
							<th>Comment</th>
							<th>Set by</th>
						</tr>
					</thead>
					<tbody>
						<#if visibilityChanges.isEmpty>
							<tr><td>No entries founds</td></tr>
                        </#if>
						@visibilityChanges.reverse.map { case (entry, createdBy) =>
						<tr>
							<td>${entry.visibility}</td>
							<td>${entry.createdAt?string.long}</td>
							<td>${entry.obj.render}</td>
							<#if createdBy??>
								<td>${createdBy.name}</td>
							<#else>
								<td>Unknown</td>
                            </#if>
						</tr>
						}
					</tbody>
				</table>
			</div>
		</div>
	</div>
</@base.base>
