<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro userAvatar userName="" avatarUrl="" imgSrc="" clazz="" attr={} href="">
    <#assign src>
        <#compress>
            <#if imgSrc?has_content>
                ${imgSrc}
            <#else>
                <#if userName?has_content>
                    ${avatarUrl}
                </#if>
            </#if>
        </#compress>
    </#assign>

    <#assign url>
        <#compress>
            <#if href?has_content>
                ${href}
            <#elseif userName?has_content>
                ${Routes.USERS_SHOW_PROJECTS.getRouteUrl(userName)}
            <#else>
                #
            </#if>
        </#compress>
    </#assign>

    <#assign name>${userName}</#assign>

    <a href="${url}" <#list attr as k, v>${k + "=" + v + " "}</#list>>
        <img class="user-avatar ${clazz}" title="${name}" src="${src}" alt="${name}"/>
    </a>
</#macro>
