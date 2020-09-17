<#import "/spring.ftl" as spring />
<#import "utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign message><@spring.message "general.linkout.title" /></#assign>
<@base.base title="${message}">
    <div class="row">
        <div class="col-md-6 col-md-offset-3">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">
                    <@spring.message "general.linkout.title" />
                    </h3>
                </div>
                <div class="card-body">
                    <p><@spring.messageArgs code="general.linkout.warning" args=[remoteUrl] /></p>
                    <a class="float-left link-go-back">
                        <i class="fas fa-arrow-left"></i> <@spring.message "project.back" />
                    </a>
                    <a href="${remoteUrl}">
                        <button class="float-right btn btn-primary">
                        <@spring.message "general.continue" />
                        </button>
                    </a>
                </div>
            </div>
        </div>
    </div>
</@base.base>
