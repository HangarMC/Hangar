<#macro linkout url>
<#--noinspection FtlReferencesInspection-->
    ${Routes.LINK_OUT.getRouteUrl(url)}
</#macro>

<#macro url path>
    <#compress>
        <#if config.useWebpack>
            http://localhost:8081/${path}
        <#else>
            ${urls.getForLookupPath('/assets/${path}')}
        </#if>
    </#compress>
</#macro>
