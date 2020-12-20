<#function linkout url>
   <#return config.security.makeSafe(url)>
</#function>

<#function url path>
    <#if config.useWebpack && !path?starts_with("ext/")>
        <#return 'http://localhost:8081/${path}'>
    <#else>
        <#assign fixedPath = ("/assets/" + path)?replace("/ext/", "-ext/")>
        <#return urls.getForLookupPath(fixedPath)!"${fixedPath}?error">
    </#if>
</#function>
