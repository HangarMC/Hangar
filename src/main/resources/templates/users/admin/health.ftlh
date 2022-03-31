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
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title"><@spring.message "admin.health.discuss" /></h4>
                </div>
                <div class="card-body list-group list-group-health">
                    <#list noTopicProjects as project>
                        <div class="list-group-item">
                            <a class="float-left" href="${Routes.PROJECTS_SHOW.getRouteUrl(project.getNamespace().getOwner(), project.getNamespace().getSlug())}">
                                <strong>${project.namespace}</strong>
                            </a>
                            <div class="clearfix"></div>
                        </div>
                    </#list>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title"><@spring.message "admin.health.jobs" /></h4>
                </div>
                <div class="card-body">
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
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title"><@spring.message "admin.health.stale" /></h4>
                </div>
                <div class="card-body list-group list-group-health">
                    <#list staleProjects as project>
                        <div class="list-group-item">
                            <a href="${Routes.PROJECTS_SHOW.getRouteUrl(project.getNamespace().getOwner(), project.getNamespace().getSlug())}">
                                <strong>${project.namespace}</strong>
                            </a>
                        </div>
                    </#list>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title"><@spring.message "admin.health.hidden" /></h4>
                </div>
                <div class="card-body list-group list-group-health">
                    <#list notPublicProjects as project>
                        <div class="list-group-item">
                            <a href="${Routes.PROJECTS_SHOW.getRouteUrl(project.getNamespace().getOwner(), project.getNamespace().getSlug())}">
                                <strong>${project.namespace}</strong> <small><@spring.message "visibility.name." + project.getVisibility().getName() /></small>
                            </a>
                        </div>
                    </#list>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title"><@spring.message "admin.health.platform" /></h4>
                </div>
                <div class="card-body list-group list-group-health">
                    <div>TODO: Filters?</div>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h4 class="card-title"><@spring.message "admin.health.missingFile" /></h4>
                </div>
                <div class="card-body list-group list-group-health">
                    <#list missingFileProjects as missingFileProject>
                        <div class="list-group-item">
                            <a href="${Routes.VERSIONS_SHOW.getRouteUrl(missingFileProject.owner, missingFileProject.name, missingFileProject.getVersion().getVersionStringUrl())}">
                                <strong>${missingFileProject.displayText}</strong>
                            </a>
                        </div>
                    </#list>
                </div>
            </div>
        </div>
    </div>

</@base.base>
