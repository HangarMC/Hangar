<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro roleSelect roleCategory form="form-continue" id="" classes="" hidden=false>
    <#assign Role=@helper["me.minidigger.hangar.model.Role"]>
    <#assign roles=Role.values()?filter(role -> role.category == roleCategory && role.isAssignable())?sort_by("permissions")?reverse>
    <select id="${id}" form="${form}" class="${classes}" <#if hidden> style="display: none;" </#if>>
        <#list roles as roleType>
            <option value="${roleType.value}">${roleType.title}</option>
        </#list>
    </select>
</#macro>
