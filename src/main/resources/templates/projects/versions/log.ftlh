<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign message><@spring.messageArgs "version.log.logger.title" project.namespace /></#assign>
<@base.base title=message>
    <div class="row">
        <div class="col-md-12">
            <h1><@spring.message "version.log.visibility.title" /> <a
                        href="${routes.getRouteUrl("versions.show", project.project.name, project.project.slug, version.versionString)}">${project.project.name}
                    /${project.project.slug}/versions/${version.versionString}</a></h1>
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
