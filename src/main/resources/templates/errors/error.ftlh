<#--
@()(implicit messages: Messages, request: OreRequest[_] = null, config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)
-->

<#import "../layout/base.ftlh" as base>
<@base.base title="Error" authEnabled=false noContainer=true>
    <div class="container-error container">
    <img src="${@assetsFinder.path("images/ore-dark.png")}" alt="Ore logo" />
    <h1 class="minor">Hangar is experiencing difficulties.</h1>
    <p class="minor">
        If you suspect this is a bug, please take a minute to report it to
        <a target="_blank" rel="noopener" href="https://github.com/MiniDigger/Hangar/issues">the issue tracker.</a>
    </p>
    </div>
</@base.base>
