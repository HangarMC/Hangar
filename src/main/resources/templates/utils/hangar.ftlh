<#macro linkout url>
<#--noinspection FtlReferencesInspection-->
    ${Routes.LINK_OUT.getRouteUrl(url)}
</#macro>

<#macro url path>
    <#compress>
<#--        <#if path == 'build/main.css'>-->
<#--            http://localhost:8081/main.css-->
<#--        <#else>-->
            <#if path?starts_with("bootstrap") || path?starts_with("build") || path?starts_with("diff-match-patch") || path?starts_with("highlight") || path?starts_with("images") || path?starts_with("javascripts") || path?starts_with("manifest") || path?starts_with('jquery-ui')>
            /assets
            </#if>
            /${path}
<#--        </#if>-->
    </#compress>
</#macro>
