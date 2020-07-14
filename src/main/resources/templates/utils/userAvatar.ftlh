@(userName: Option[String], avatarUrl: String = "", imgSrc: String = null, clazz: String = "", attr: Map[String, String] = Map(), href: String = null)

<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#-- TODO: all the userName.nonEmpty/isEmpty stuff - how do you do this? -->

<#macro userAvatar userName="" avatarUrl="" imgSrc="" clazz="" attr={} href="">
    <#assign src>
        <#if !imgSrc.isEmpty()>
            ${imgSrc}
        <#else>
            <#if !userName.isEmpty()>
                ${avatarUrl}
            </#if>
        </#if>
    </#assign>

    <#assign url>
        <#if !href.isEmpty()>
            ${href}
        <#elseif !userName.isEmpty()>
            ${routes.getRouteUrl("users.showProjects", userName)}
        <#else>
            #
        </#if>
    </#assign>

    <#assign name>${userName}</#assign>

    <a href="${url}" <#list attr as k, v>${k + "=" + v + " "}</#list>>
        <img class="user-avatar ${clazz}" title="${name}" src="${src}" alt="${name}" />
    </a>
</#macro>
