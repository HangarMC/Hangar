@import ore.data.Prompt
@(prompt: Prompt, id: String = "", position: String = "right")(implicit messages: Messages)

<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<div class="prompt popover @id @position" data-prompt-id="@prompt.value">
    <div class="arrow"></div>
    <h3 class="popover-title"><@spring.message prompt.titleId /></h3>
    <div class="popover-content">
        <p><@spring.message prompt.messageId /></p>
        <button class="btn-got-it btn btn-success btn-sm">
            <@spring.message "prompt.confirm" />
        </button>
    </div>
</div>
