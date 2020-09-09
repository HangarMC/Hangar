<#import "/spring.ftl" as spring />
<#import "../layout/base.ftlh" as base>
<#import "*/utils/hangar.ftlh" as hangar />

<#assign message><@spring.message "error.notFound.title" /></#assign>
<@base.base title="${message}" authEnabled=true noContainer=true>
    <div class="container-error container">
        <img src="<@hangar.url "images/ore-dark.png" />" alt="Hangar logo"/>
        <h1 class="minor"><@spring.message "error.notFound.title" /></h1>
        <h3 class="minor"><@spring.messageArgs code="error.notFound.message" args=[springMacroRequestContext.requestUri] /></h3>
    </div>
</@base.base>
