<#import "/spring.ftl" as spring />
<#import "utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign message><@spring.message "general.linkout.title" /></#assign>
<@base.base title="${message}">
    <div class="row">
        <div class="col-md-6 col-md-offset-3">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">
                    <@spring.message "general.linkout.title" />
                    </h3>
                </div>
                <div class="panel-body">
                    <p><@spring.messageArgs "general.linkout.warning", [remoteUrl] /></p>
                    <a class="pull-left link-go-back">
                        <i class="fas fa-arrow-left"></i> <@spring.message "project.back" />
                    </a>
                    <a href="${remoteUrl}">
                        <button class="pull-right btn btn-primary">
                        <@spring.message "general.continue" />
                        </button>
                    </a>
                </div>
            </div>
        </div>
    </div>
</@base.base>
