<#--
@()(implicit messages: Messages, requestHeader: RequestHeader, request: OreRequest[_] = null, config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)
-->

<#import "../layout/base.ftlh" as base>
<@base.base title="${messages("error.notFound.title")}" authEnabled=false noContainer=true>
    <div class="container-error container">
        <img src="${@assetsFinder.path("images/ore-dark.png")}" alt="Ore logo"/>
        <h1 class="minor">${@messages("error.notFound.title")}</h1>
        <h3 class="minor">${@messages("error.notFound.message", requestHeader.uri)}</h3>
    </div>
</@base.base>
