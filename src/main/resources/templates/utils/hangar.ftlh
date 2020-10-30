<#macro linkout url>
<#--noinspection FtlReferencesInspection-->
    <#if config.security.checkSafe(url)>
        ${url}
    <#else>
        ${Routes.LINK_OUT.getRouteUrl(url)}
    </#if>
</#macro>

<#macro url path>
    <#compress>
        <#if config.useWebpack>
            http://localhost:8081/${path}
        <#else>
            ${urls.getForLookupPath('/assets/${path}')!"/assets/${path}?error"}
        </#if>
    </#compress>
</#macro>
