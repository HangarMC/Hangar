<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/projects/view.ftlh" as view/>
<#import "*/utils/editor.ftlh" as editor/>

<#--
Discussion page within Project overview.
-->

<#-- todo: Bridge to Paper Discourse page? Directly save discussion in Hangar? -->
<#assign scriptsVar>
    <script <#--@CSPNonce.attr-->>
        DiscourseEmbed = {
            discourseUrl: '${config.app.discourseUrl}/',
            topicId: ${p.project.topicId}
        };
    </script>
    <script type="text/javascript" src="<@hangar.url "javascripts/projectDiscuss.js" />"></script>
    <script <#--@CSPNonce.attr-->>$(function() { $('.btn-edit').click(); });</script>
</#assign>

<@view.view p=p sp=sp active="#discussion" additionalScripts=scriptsVar>
    <div id='discourse-comments'></div>
    <div class="row">
    <#if cu??>
        <div class="col-md-8">
            <div class="reply-box">
                <#if sp.canPostAsOwnerOrga>
                <div class="pull-right push-down">
                    <i class="minor"><@spring.message "project.discuss.postAs" /></i>
                    <select name="poster" form="form-editor-save">
                        <option selected>${cu.name}</option>
                        <option>${p.projectOwner.name}</option>
                    </select>
                </div>
                        <div class="clearfix"></div>
                </#if>

                <div class="push-down">
                    <@editor.editor saveCall=Routes.PROJECTS_POST_DISCUSSION_REPLY.getRouteUrl(p.project.ownerName, p.project.slug) cancellable=false enabled=true/>
                </div>
            </div>
            <div class="reply-controls">
                <a class="forums" target="_blank" rel="noopener"
                href="${config.app.discourseUrl}/t/${p.project.topicId}">
                <@spring.message "general.viewOnForums" />
                </a>
            </div>
        </div>
    <#else>
        <div class="pull-right">
            <a href="${Routes.USERS_LOGIN.getRouteUrl("", "", request.path)}"><@spring.message "general.login" /></a>
            <span class="minor"> <@spring.message "general.toReply" /></span>
        </div>
    </#if>
    </div>
</@view.view>
