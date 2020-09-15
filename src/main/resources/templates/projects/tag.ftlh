<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro tagTemplate tag platform form tagCount=0>
    <#-- @ftlvariable name="tag" type="io.papermc.hangar.model.viewhelpers.ViewTag" -->
    <#-- @ftlvariable name="platform" type="io.papermc.hangar.model.Platform" -->
    <div class="tags">
        <span style="color: ${tag.color.getForeground()}; background-color:${tag.color.getBackground()}; border-color: ${tag.color.getBackground()}" class="tag">${tag.name}</span>
    </div>
    <br>
    <div>
        <#list platform.getPossibleVersions() as version>
            <label for="version-${version}">${version}</label>
            <input form="${form}" id="version-${version}" type="checkbox" name="versions" value="${version}" <#if tag.data?has_content && tag.data == version>checked</#if>>
            <#if (version?index + 1) % 5 == 0></div><div></#if>
        </#list>
    </div>

</#macro>
