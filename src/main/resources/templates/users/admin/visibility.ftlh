<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />
<#import "*/projects/helper/btnHide.ftlh" as btnHide />

<#macro renderApprovalList projectSeq>
    <ul class="list-group list-flags-admin">
        <#if !projectSeq?has_content>
            <li class="list-group-item">
                <p>No projects found.</p>
            </li>
        </#if>
        <#list projectSeq as project>
            <li class="list-group-item">
                <div class="row">
                    <div class="col-xs-12 col-md-8">
                        <span class="description">
                            <strong>${project.changeRequester}</strong>
                            <span class="minor"> requested changes on </span>
                            <a href="${routes.getRouteUrl("projects.show", project.getNamespace().getOwner(), project.getNamespace().getSlug())}">
                            ${project.namespace}
                            </a>
                        </span>
                        <p>
                            <strong>Request:</strong>
                            ${markdownService.render(project.comment)}
                        </p>
                    </div>
                    <div class="col-xs-12 col-md-4">
                        <span class="pull-right btn-group-sm">
                        <@btnHide.btnHide project.namespace, project.visibility/>
                        </span>
                    </div>
                </div>
            </li>
        </#list>
    </ul>
</#macro>

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/hideProject.js" />"></script>
</#assign>

<@base.base title="Project Visibility" additionalScripts=scriptsVar>

    <div class="row">
        <div class="col-md-12 header-flags">
            <h2>Needs Approval</h2>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title pull-left">Needs Approval</h4>
                    <div class="clearfix"></div>
                </div>
                <@renderApprovalList needsApproval />
            </div>
        </div>
        <div class="row">
            <div class="col-md-12 header-flags">
                <h2>Awaiting Changes</h2>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title pull-left">Awaiting Changes</h4>
                        <div class="clearfix"></div>
                    </div>
                    <@renderApprovalList waitingProjects />
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="modal-visibility-comment" tabindex="-1" role="dialog" aria-labelledby="modal-visibility-comment">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title" style="color:black;">Comment</h4>
                </div>
                <div class="modal-body">
                    <textarea class="textarea-visibility-comment form-control" rows="3"></textarea>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-default">Close</button>
                    <button class="btn btn-visibility-comment-submit btn-primary"><i class="fas fa-pencil-alt"></i> Submit</button>
                </div>
            </div>
        </div>
    </div>
</@base.base>
