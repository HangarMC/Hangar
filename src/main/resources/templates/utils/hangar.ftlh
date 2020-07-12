<#-- todo implement config lookup -->
<#macro config path>
    ${path}
</#macro>

<#-- todo implement perm check -->
<#macro hasPerm perm>
    <#if true>
        <#nested>
    </#if>
</#macro>

<#macro linkout url>
    <#--noinspection FtlReferencesInspection-->
    ${routes.getRouteUrl("linkOut", url)}
</#macro>
