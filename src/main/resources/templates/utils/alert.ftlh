<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro alert alertType>
<#--    <#if flash.get(alertType).isDefined || flash.get(s"$alertType-num").isDefined>-->
<#--        <div class="alert alert-fade <@bootstrapNames type=alertType/> alert-dismissable"-->
<#--             role="alert" style="display: none;">-->
<#--            <button type="button" class="close" data-dismiss="alert" aria-label="<@spring.message "general.close" />">-->
<#--                <span aria-hidden="true">&times;</span>-->
<#--            </button>-->

<#--            <@normalAlert />-->
<#--            <@numAlert />-->
<#--        </div>-->
<#--    </#if>-->
</#macro>

<#macro normalAlert>
    @flash.get(alertType).map { message =>
    @(messages(message))
    }
</#macro>

<#-- todo port alerts -->

<#macro numAlert>
    @flash.get(s"$alertType-num").map { num =>
    <ul>
        @for(i <- 0 until num.toInt; msg <- flash.get(s"$alertType-$i").toSeq) {
        <li>
            <@spring.message msg />
        </li>
        }
    </ul>
    }
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
