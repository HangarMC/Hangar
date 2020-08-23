<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro alert alertType>
    <#if alerts[alertType]?? || alerts[alertType + "-num"]??>
        <div class="alert alert-fade <@bootstrapName type=alertType/> alert-dismissable"
             role="alert" style="display: none;">
            <button type="button" class="close" data-dismiss="alert" aria-label="<@spring.message "general.close" />">
                <span aria-hidden="true">&times;</span>
            </button>

            <@normalAlert alertType />
            <@numAlert alertType />
        </div>
    </#if>
</#macro>

<#macro normalAlert alertType>
    <#if alerts[alertType]["message"]??>
        <#if alerts[alertType]["args"]??>
            <@spring.messageArgs alerts[alertType]["message"] alerts[alertType]["args"] />
        <#else>
            <@spring.message alerts[alertType]["message"] />
        </#if>
    </#if>
</#macro>

<#macro numAlert alertType>
    <#if alerts[alertType + "-num"]??>
        <ul>
            <#list 0..alerts[alertType + "-num"] as i>
                <li><@spring.message alerts[alertType + "-" + i] /></li>
            </#list>
        </ul>
    </#if>
</#macro>

<#macro bootstrapName type>
    <#switch type>
        <#case "error">
            alert-danger
            <#break>
        <#case "success">
            alert-success
            <#break>
        <#case "info">
            alert-info
            <#break>
        <#case "warning">
            alert-warning
            <#break>
    </#switch>
</#macro>
