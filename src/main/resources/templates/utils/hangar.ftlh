<#function linkout url>
   <#return config.security.makeSafe(url)>
</#function>

<#function url path>
    <#if config.useWebpack>
        <#return 'http://localhost:8081/${path}'>
    <#else>
        <#return urls.getForLookupPath("/assets/${path}")!"/assets/${path}?error">
    </#if>
</#function>
