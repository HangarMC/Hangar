<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#--
@import controllers.sugar.Requests.OreRequest
@import models.querymodels.UnhealtyProject
@import ore.OreConfig
@import ore.models.project.{Project, Version}
@import ore.models.Job
@(noTopicProjects: Seq[UnhealtyProject],
  staleProjects: Seq[UnhealtyProject],
  notPublicProjects: Seq[UnhealtyProject],
  missingFileProjects: Seq[(Version, Project)],
  erroredJobs: Seq[Job])(implicit request: OreRequest[_], messages: Messages, config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)
-->

<#assign message><@spring.message "admin.health.title" /></#assign>
<@base.base title="${message}">

    <div class="row">
        <div class="col-md-12">
            <h1><@spring.message "admin.health.title" /></h1>
        </div>
    </div>
    <div class="row">
        <div class="col-md-6">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title"><@spring.message "admin.health.discuss" /></h4>
                </div>
                <div class="panel-body list-group list-group-health">
                    <#list noTopicProjects as project>
                        <div class="list-group-item">
                            <a class="pull-left" href="${routes.getRouteUrl("projects.show", project.getNamespace().getOwner(), project.getNamespace().getSlug())}">
                                <strong>${project.namespace}</strong>
                            </a>
                            <div class="clearfix"></div>
                        </div>
                    </#list>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title"><@spring.message "admin.health.jobs" /></h4>
                </div>
                <div class="panel-body">
                    <#list erroredJobs as job>
                        <div class="list-group-item">
                            Job type: ${job.jobType} Error type: ${job.lastErrorDescriptor} Happened: ${utils.prettifyDateTime(job.lastUpdated)}
                            <div class="clearfix"></div>
                        </div>
                    </#list>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-6">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title"><@spring.message "admin.health.stale" /></h4>
                </div>
                <div class="panel-body list-group list-group-health">
                    <#list staleProjects as project>
                        <div class="list-group-item">
                            <a href="${routes.getRouteUrl("projects.show", project.getNamespace().getOwner(), project.getNamespace().getSlug())}">
                                <strong>${project.namespace}</strong>
                            </a>
                        </div>
                    </#list>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title"><@spring.message "admin.health.hidden" /></h4>
                </div>
                <div class="panel-body list-group list-group-health">
                    <#list notPublicProjects as project>
                        <div class="list-group-item">
                            <a href="${routes.getRouteUrl("projects.show", project.getNamespace().getOwner(), project.getNamespace().getSlug())}">
                                <strong>${project.namespace}</strong> <small><@spring.message "visibility.name." + project.visibility.name /></small>
                            </a>
                        </div>
                    </#list>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-6">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title"><@spring.message "admin.health.platform" /></h4>
                </div>
                <div class="panel-body list-group list-group-health">
                    <div>TODO: Filters?</div>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title"><@spring.message "admin.health.missingFile" /></h4>
                </div>
                <div class="panel-body list-group list-group-health">
                <#--@missingFileProjects.map { case (version, project) =>
                <div class="list-group-item">
                    <a href="${routes.getRouteUrl("versions.show", project.ownerName, project.slug, version.name)}">
                        <strong>${project.namespace}/${version.name}</strong>
                </a>
                </div>
                }--> <#--TODO missinFileProjects -->
                </div>
            </div>
        </div>
    </div>

</@base.base>
