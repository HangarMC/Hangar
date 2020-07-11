<#--
@()(implicit messages: Messages, requestHeader: RequestHeader, request: OreRequest[_] = null, config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)
-->

<#import "../layout/base.ftlh" as base>
<@base.base title="${messages("error.ore.timeout")}" authEnabled=false noContainer=true>
    <div class="container-error container">
        <img src="${@assetsFinder.path("images/ore-dark.png")}" alt="Ore logo" />
        <h1 class="minor">${@messages("error.ore.timeout.title")}</h1>
        <i class="minor">${@messages("error.ore.timeout.body")}</i>
    </div>
</@base.base>
