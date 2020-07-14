<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#--@import ore.permission.role.{Role, RoleCategory}-->
<#--@(roleCategory: RoleCategory, form: String = "form-continue", id: String = "", classes: String = "",-->
<#--        hidden: Boolean = false)-->

<#--@roles = @{-->
<#--    Role.values.filter(role => role.category == roleCategory && role.isAssignable)-->
<#--            .toList.sortBy(_.permissions: Long).reverse-->
<#--}-->

<#macro roleSelect roleCategory="" form="form-continue" id="" classes="" hidden=false>
    <select id="${id}" form="${form}" class="${classes}" <#if hidden> style="display: none;" </#if>>
        <#list roles as roleType>
            <option value="${roleType.value}">${roleType.title}</option>
        </#list>
    </select>
</#macro>
