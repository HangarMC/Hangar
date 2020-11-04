<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign message><@spring.message "version.log.logger.title"/></#assign>
<@base.base title=message>
    <div class="row">
        <div class="col-md-12">
            <h1><@spring.message "version.log.visibility.title" />
                <a href="${Routes.VERSIONS_SHOW.getRouteUrl(project.ownerName, project.project.slug, version.versionStringUrl)}">
                    ${project.ownerName}/${project.project.slug}/versions/${version.versionString}
                </a>
            </h1>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="card">
                <div class="car-header">
                    <h4 class="card-title float-left"><@spring.message "version.log.visibility.title" /></h4>
                    <div class="clearfix"></div>
                </div>
                <table class="table table-sm setting-no-border table-review-log">
                    <thead>
                    <tr>
                        <th>State</th>
                        <th>Time</th>
                        <th>Comment</th>
                        <th>Set by</th>
                    </tr>
                    </thead>
                    <tbody>
                    <#if !visibilityChanges?has_content>
                        <tr>
                            <td>No entries founds</td>
                        </tr>
                    </#if>
                    <#list visibilityChanges as entry, createdBy>
                        <tr>
                            <td>${entry.visibility}</td>
                            <td>${utils.prettifyDateTime(entry.createdAt)}</td>
                            <td>${markdownService.render(entry.comment)}</td>
                            <#if createdBy??>
                                <td>${createdBy}</td>
                            <#else>
                                <td>Unknown</td>
                            </#if>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</@base.base>
