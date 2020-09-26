<#macro linkout url>
<#--noinspection FtlReferencesInspection-->
    ${Routes.LINK_OUT.getRouteUrl(url)}
</#macro>

<#macro url path>
    <#compress>
        <#if path?starts_with("lib")>
            /${path}
        <#elseif config.useWebpack>
            http://localhost:8081/${path}
        <#else>
            /assets/${path}
        </#if>
    </#compress>
</#macro>
