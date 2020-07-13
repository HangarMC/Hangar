<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

@import controllers.sugar.Requests.OreRequest
@import ore.OreConfig
@import ore.data.DownloadType
@import ore.models.project.{Project, ReviewState, Version}
@import views.html.helper.CSRF
@(project: Project,
        target: Version,
        isTargetChannelNonReviewed: Boolean,
        downloadType: DownloadType)(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)

@layout.base(messages("version.download.confirm.title") + " " + messages("version.download.confirm.header", project.ownerName, project.name, target.name)) {

    <div class="row">
        <div class="col-md-6 col-md-offset-3">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <div class="col-xs-12 col-sm-4 no-padding">
                        <h4 class="panel-title"><i class="fas fa-exclamation-triangle"></i>&ensp;<@spring.message "general.warning" /></h4>
                    </div>
                    <div class="col-xs-12 col-sm-8 no-padding">
                        <span class="pull-right"><@spring.messageArgs "version.download.confirm.header" project.ownerName project.name target.name /></span>
                    </div>
                </div>
                <div class="panel-body">
                    <div class="alert alert-danger" style="margin-bottom: 10px">
                    <#if isTargetChannelNonReviewed || target.reviewState == ReviewState.Backlog>
                        <@spring.message "version.download.confirm.nonReviewedChannel" />
                    <#elseif target.reviewState != ReviewState.PartiallyReviewed>
                        <@spring.message "version.download.confirm.reviewedChannel" />
                    <#else>
                        <@spring.message "version.download.confirmPartial.reviewedChannel" />
                    </#if>
                    </div>
                    <p>
                        <@spring.message "general.disclaimer" />:
                        <span class="text-italic"><@spring.message "version.download.confirm.disclaimer" /></span>
                    </p>

                    <div class="row">
                        <div class="col-xs-12 col-sm-6 mb-3-sm">
                            <button class="btn btn-success link-go-back">
                            <@spring.message "project.back" />
                            </button>
                        </div>

                        <div class="col-xs-12 col-sm-6">
                            <form action="${routes.getRouteUrl("versions.confirmDownload", project.ownerName, project.slug, target.name, Some(downloadType.value), "", "dummy")}" method="post" id="form-download">
                                @CSRF.formField

                                <button type="submit" form="form-download" class="btn btn-danger pull-right-sm">
                                <@spring.message "version.download.confirm.download" />
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

}
