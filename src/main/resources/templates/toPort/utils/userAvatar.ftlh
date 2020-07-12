@(userName: Option[String], avatarUrl: String = "", imgSrc: String = null, clazz: String = "", attr: Map[String, String] = Map(), href: String = null)

<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#-- TODO: all the userName.nonEmpty/isEmpty stuff - how do you do this? -->

<#assign src>
    <#if imgSrc??>
        ${imgSrc}
    <#else>
        <#if userName.nonEmpty>
            ${avatarUrl}
        </#if>
    </#if>
</#assign>

<#--

<#assign url>
    <#if href??>
        ${href}
    <#elseif userName.isEmpty>
        #
    <#else>
        ${routes.getRouteUrl("users.showProjects")} -- ????????
    </#if>
</#assign>

@url = @{
    if (href != null)
        href
    else if (userName.isEmpty)
        "#"
    else
        routes.Users.showProjects(userName.get).url
}

-->

<#assign name>
    <#if !userName.isEmpty>
        ${userName}
    </#if>
</#assign>
@name = @{
    if (userName.isEmpty)
        ""
    else
        userName
}

<#-- TODO: attrs here -->
<a href="${url}" @for((k, v) <- attr) { @(s"$k=$v") }>
    <img class="user-avatar ${clazz}" title="${name}" src="${src}" alt="${name}" />
</a>
