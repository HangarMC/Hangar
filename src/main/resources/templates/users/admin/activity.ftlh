<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign message><@spring.message "activity.title" /></#assign>
<@base.base title=("${message} ${username}")>

    <div class="row">
        <div class="col-md-12  header-flags">
            <div class="clearfix">
                <h1 class="pull-left"><@spring.message "activity.title" /> <i>${username}</i></h1>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title pull-left"><@spring.message "activity.title" /></h4>
                    <div class="clearfix"></div>
                </div>
                <table class="table table-condensed setting-no-border table-review-log">
                    <tbody>
                        <#if !activities?has_content>
                            <tr><td>No activities founds</td></tr>
                        </#if>
                        <#assign OffsetDateTime=@helper["java.time.OffsetDateTime"]>
                        <#list activities as activity>
                            <#if activity.class.simpleName == "ReviewActivity">
                                <tr>
                                    <td>Review approved</td>
                                    <td>${utils.prettifyDateTime(activity.endedAt!OffsetDateTime.MIN))}</td>
                                    <td>for:
                                        <a href="${routes.getRouteUrl("reviews.showReviews", activity.getProject().getOwner(), activity.getProject().getSlug(), activity.id)}">
                                            ${activity.getProject().getOwner()} / ${activity.getProject().getSlug()}
                                        </a>
                                    </td>
                                </tr>
                            <#elseif activity.class.simpleName = "FlagActivity">
                                <tr>
                                    <td>Flag resolved</td>
                                    <td>${utils.prettifyDateTime(activity.resolvedAt!OffsetDateTime.MIN))}</td> <#--TODO: activity end datetime needs to be moved to abstract Activity-->
                                    <td>for:
                                        <a href="${routes.getRouteUrl("projects.show", activity.getProject().getSlug(), activity.getProject().getSlug())}">
                                            ${activity.getProject().getOwner()} / ${activity.getProject().getSlug()}
                                        </a>
                                    </td>
                                </tr>
                            </#if>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</@base.base>
