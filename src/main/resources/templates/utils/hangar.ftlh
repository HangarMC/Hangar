<#macro linkout url>
    ${config.security.makeSafe(url)}
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
