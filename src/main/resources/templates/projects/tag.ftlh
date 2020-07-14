<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#--@import models.querymodels.ViewTag-->

<#--@(tag: ViewTag, tagCount: Int = 0)-->
<#macro tagTemplate tag tagCount=0>
    <#if tag.data?has_content || tagCount gt 2 || tag.data.exists(_.length gt 14)>
        <div class="tags">
            <span style="color: ${tag.color.foreground}; background-color:${tag.color.background}; border-color: ${tag.color.background}" class="tag">${tag.name}</span>
        </div>
    <#else>
        <div class="tags has-addons">
            <span style="color: ${tag.color.foreground}; background-color:${tag.color.background}; border-color: ${tag.color.background}" class="tag">${tag.name}</span>
            <span class="tag">${tag.data.get.take(14)}</span>
        </div>
    </#if>
</#macro>
