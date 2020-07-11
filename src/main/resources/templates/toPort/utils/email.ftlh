<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

@import controllers.sugar.Requests.OreRequest
@import ore.OreConfig
@(title: String, recipient: String, body: String)(implicit messages: Messages, request: OreRequest[_], config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)

@layout.base(messages(title), scriptsEnabled = false, showHeader = false, showFooter = false) {

    <div class="container">
        <p>@recipient,</p>
        <p><@spring.message body /></p>
        <p>@Html(messages("email.signature"))</p>
    </div>

}
