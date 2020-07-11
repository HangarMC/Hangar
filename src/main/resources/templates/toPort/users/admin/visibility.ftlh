@import controllers.sugar.Requests.OreRequest
@import models.querymodels.ProjectNeedsApproval
@import ore.OreConfig
@import ore.markdown.MarkdownRenderer
@(needsApproval: Seq[ProjectNeedsApproval],
 waitingProjects: Seq[ProjectNeedsApproval])(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, renderer: MarkdownRenderer, assetsFinder: AssetsFinder)

@projectRoutes = @{controllers.project.routes.Projects}

@renderApprovalList(projectSeq: Seq[ProjectNeedsApproval]) = {
    <ul class="list-group list-flags-admin">
        @if(projectSeq.isEmpty) {
            <li class="list-group-item">
                <p>No projects found.</p>
            </li>
        }
        @projectSeq.map { project =>
            <li class="list-group-item">
                <div class="row">
                    <div class="col-xs-12 col-md-8">
                        <span class="description">
                            <strong>@project.changeRequester</strong>
                            <span class="minor"> requested changes on </span>
                            <a href="@projectRoutes.show(project.namespace.ownerName, project.namespace.slug)">
                            @project.namespace
                            </a>
                        </span>
                        <p>
                            <strong>Request:</strong>
                            @renderer.render(project.comment)
                        </p>
                    </div>
                    <div class="col-xs-12 col-md-4">
                        <span class="pull-right btn-group-sm">
                        @projects.helper.btnHide(project.namespace, project.visibility)
                        </span>
                    </div>
                </div>
            </li>
        }
    </ul>
}

@scripts = {
    <script type="text/javascript" src="@assetsFinder.path("javascripts/hideProject.js")"></script>
}

@layout.base("Project Visibility", scripts) {

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
                @renderApprovalList(needsApproval)
            </div>
        </div>
        <div class="row">
            <div class="col-md-12 header-flags">
                <h2>Wating Changes</h2>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title pull-left">Waiting Changes</h4>
                        <div class="clearfix"></div>
                    </div>
                    @renderApprovalList(waitingProjects)
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
}
