<#import "/spring.ftl" as spring />
<#import "../layout/base.ftlh" as base>
<#import "*/utils/hangar.ftlh" as hangar />

<@base.base title="Error" authEnabled=false noContainer=true>
    <div class="container-error container">
    <img src="${hangar.url("images/ore-dark.png")}" alt="Hangar logo" />
    <h1 class="minor">Hangar is experiencing difficulties.</h1>
    <p class="minor">
        If you suspect this is a bug, please take a minute to report it to
        <a target="_blank" rel="noopener" href="https://github.com/PaperMC/Hangar/issues">the issue tracker.</a>
    </p>
    </div>
</@base.base>
