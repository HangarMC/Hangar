<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/form.ftlh" as form />
<#import "*/utils/csrf.ftlh" as csrf />
<#import "*/layout/base.ftlh" as base />

<#assign scriptsVar>
    <link rel="stylesheet" href="<@hangar.url "css/platform-version-table.css" />">
    <script>
        window.PLATFORMS = ${platformVersions}
    </script>
    <script type="text/javascript" src="<@hangar.url "js/platform-version-table.js" />"></script>
</#assign>

<#assign message><@spring.message "admin.platformVersions.title" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar>
    <div id="platform-version-table"></div>
</@base.base>
