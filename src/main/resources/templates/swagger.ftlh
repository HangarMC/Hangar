<#--
Mostly copied from swagger-ui's index file
-->

<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign scriptsVar>
    <script type="text/javascript" src="${hangar.url("js/swagger.js")}"></script>
</#assign>
<#assign styleVar>
    <link rel="stylesheet" type="text/css" href="${hangar.url("css/swagger.css")}">
</#assign>

<#assign message><@spring.message "general.openapi" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar additionalStyling=styleVar>
    <div id="swagger-ui-vue"></div>
</@base.base>
