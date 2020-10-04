<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />
<#import "*/projects/channels/helper/modalManage.ftlh" as modalManage />
<#import "*/utils/form.ftlh" as form>
<#import "*/utils/csrf.ftlh" as csrf>

<#assign scriptsVar>
    <script <#--@CSPNonce.attr-->>
        window.OWNER_NAME = '${p.project.ownerName}';
        window.PROJECT_SLUG = '${p.project.slug}';
        window.COLORS = ${mapper.valueToTree(@helper["io.papermc.hangar.model.Color"].getNonTransparentValues())};
        window.MAX_CHANNEL_NAME_LEN = ${config.channels.maxNameLen};
        window.CHANNELS = ${mapper.valueToTree(channels)};
    </script>
    <script type="text/javascript" src="<@hangar.url "js/release-channels.js" />"></script>
</#assign>
<#assign styleVar>
    <link rel="stylesheet" href="<@hangar.url "css/release-channels.css" />">
</#assign>

<#assign message><@spring.messageArgs code="channel.list.title" args=[p.project.ownerName, p.project.slug] /></#assign>
<@base.base title=message additionalScripts=scriptsVar additionalStyling=styleVar>
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title"><@spring.message "channel.list.title" /></h3>
                </div>
                <div class="card-body">
                    <p class="minor create-blurb">
                        <@spring.message "channel.list.description" />
                    </p>
                    <div id="release-channels"></div>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="modal-delete" tabindex="-1" role="dialog" aria-labelledby="label-delete">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title" id="label-delete"><@spring.message "channel.delete" /></h4>
                    <button type="button" class="close" data-dismiss="modal"
                            aria-label="<@spring.message "general.close" />">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <p><@spring.message "channel.delete.info" /></p>
                    <p class="minor">
                        <strong id="version-count-on-delete"></strong>
                        <i><@spring.message "channel.delete.info.versions" /></i>
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <@spring.message "general.cancel" />
                    </button>
                    <form id="delete-form" method="post" action="#" class="form-channel-delete">
                        <@csrf.formField />
                        <button type="submit" class="btn btn-danger"><@spring.message "channel.delete" /></button>
                    </form>
                </div>
            </div>
        </div>
    </div>

</@base.base>