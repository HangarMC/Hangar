<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

@import controllers.sugar.Requests.OreRequest
@import ore.OreConfig
@(title: String, recipient: String, body: String)(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)

<#assign message><@spring.message title /></#assign>
<@base.base title=message scriptsEnabled=false showHeader=false showFooter=false>
    <div class="container">
        <p>${recipient},</p>
        <p><@spring.message body /></p>
        <p><@spring.message "email.signature" /></p>
    </div>
</@base.base>
