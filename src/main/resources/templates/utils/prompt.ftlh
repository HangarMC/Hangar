<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro prompt prompt id="" position="right">
<#-- @ftlvariable name="prompt" type="io.papermc.hangar.model.common.Prompt" -->
    <div class="prompt popover ${id} ${position}" data-prompt-id="${prompt.ordinal()}">
        <div class="arrow"></div>
        <h3 class="popover-title"><@spring.message prompt.titleKey /></h3>
        <div class="popover-content">
            <p><@spring.message prompt.messageKey /></p>
            <button class="btn-got-it btn btn-success btn-sm">
                <@spring.message "prompt.confirm" />
            </button>
        </div>
    </div>
</#macro>
