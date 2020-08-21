<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro tagTemplate tag tagCount=0>
<#-- @ftlvariable name="tag" type="io.papermc.hangar.model.viewhelpers.ViewTag" -->
    <#if tag.data?has_content || tagCount gt 2 || (tag.data?has_content && tag.data?length gt 14)>
        <div class="tags">
            <span style="color: ${tag.color.getForeground()}; background-color:${tag.color.getBackground()}; border-color: ${tag.color.getBackground()}" class="tag">${tag.name}</span>
        </div>
    <#else>
        <div class="tags has-addons">
            <span style="color: ${tag.color.getForeground()}; background-color:${tag.color.getBackground()}; border-color: ${tag.color.getBackground()}" class="tag">${tag.name}</span>
            <span class="tag">${tag.data[0..14]}</span>
        </div>
    </#if>
</#macro>
