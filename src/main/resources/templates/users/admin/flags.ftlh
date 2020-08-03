<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />
<#import "*/projects/helper/btnHide.ftlh" as hide />

<#--
@import controllers.sugar.Requests.OreRequest
@import models.querymodels.ShownFlag
@import ore.OreConfig
@import ore.models.user.User
@import util.StringFormatterUtils._
@import util.syntax._
@import views.html.utils.userAvatar

@* project perms for all visibilities *@
@(flags: Seq[ShownFlag])(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)
-->
<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/adminFlags.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/hideProject.js" />"></script>
</#assign>

<@base.base title="Flags" additionalScripts=scriptsVar>
    <div class="row">
        <div class="col-md-12 header-flags">
            <h2>Flags</h2>
            <h3 class="minor no-flags" <#if flags?size gt 0>style="display: none;"</#if>>
                <i class="far fa-thumbs-up"></i> <@spring.message "user.flags.none" />
            </h3>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <ul class="list-group list-flags-admin">
                <#list flags as flag>
                    <li data-flag-id="${flag.flag.id}" class="list-group-item">
                        <div class="row">
                            <div class="col-xs-12 col-md-1" style="width: 40px;">
                                <a href="${routes.getRouteUrl("users.showProjects", flag)}">
                                    <#import "*/utils/userAvatar.ftlh" as userAvatar>
                                    <@userAvatar.userAvatar userName=flag.reportedBy avatarUrl=utils.format(config.security.api.avatarUrl, flag.reportedBy) clazz="user-avatar-xs"></@userAvatar.userAvatar>
                                </a>
                            </div>
                            <div class="col-xs-12 col-md-11">
                            <span class="description">
                                <strong>${flag.reportedBy}</strong>
                                <span class="minor"> reported </span>
                                <a href="${routes.getRouteUrl("projects.show", flag.projectOwnerName, flag.projectSlug)}">
                                    ${flag.projectNamespace}
                                </a>
                                <span class="minor"> for </span>
                                <strong>${flag.flag.reason.title}</strong>
                                <span class="minor"> at </span>
                                <strong>${(flag.flag.createdAt).format("yyyy-MM-dd HH:mm:ss")}</strong>
                                <br><i class="minor">${flag.flag.comment}</i>
                            </span>
                        </div>
                            <div class="col-xs-12">
                                <span class="pull-right btn-group-sm">
                                    <a target="_blank" rel="noopener" href="https://papermc.io/forums/users/${flag.reportedBy}" class="btn btn-default">
                                        <i class="fas fa-reply"></i> <@spring.message "user.flags.messageUser" />
                                    </a>
                                    <a target="_blank" rel="noopener" href="https://papermc.io/forums/users/${flag.projectOwnerName}" class="btn btn-default">
                                        <i class="fas fa-reply"></i> <@spring.message "user.flags.messageOwner" />
                                    </a>
                                    <@hide.btnHide flag.projectNamespace flag.projectVisibility />
                                    <button type="submit" class="btn btn-primary btn-resolve">
                                        <i class="fas fa-check"></i> <strong><@spring.message "user.flags.markResolved" /></strong>
                                    </button>
                                </span>
                            </div>
                        </div>
                    </li>
                </#list>
            </ul>
        </div>
    </div>

    <div class="modal fade" id="modal-visibility-needschanges" tabindex="-1" role="dialog" aria-labelledby="modal-visibility-needschanges">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="<@spring.message "general.close" />">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">Needs Changes</h4>
                </div>
                <div class="modal-body">
                    <textarea class="textarea-needschanges form-control" rows="3"></textarea>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-default" data-dismiss="modal"><@spring.message "general.close" /></button>
                    <button class="btn btn-visibility-needschanges-submit btn-primary"><i class="fa fa-pencil-alt"></i> Submit</button>
                </div>
            </div>
        </div>
    </div>
</@base.base>
