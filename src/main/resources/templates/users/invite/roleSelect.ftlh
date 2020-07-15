<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#--@roles = @{-->
<#--    Role.values.filter(role => role.category == roleCategory && role.isAssignable)-->
<#--            .toList.sortBy(_.permissions: Long).reverse-->
<#--}-->

<#macro roleSelect roleCategory form="form-continue" id="" classes="" hidden=false>
    <select id="${id}" form="${form}" class="${classes}" <#if hidden> style="display: none;" </#if>>
        <#list roles as roleType>
            <option value="${roleType.value}">${roleType.title}</option>
        </#list>
    </select>
</#macro>
