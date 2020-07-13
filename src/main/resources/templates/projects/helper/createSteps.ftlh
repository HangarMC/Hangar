<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

@(step: Int)

@stepState(s: Int) = {
    <#if s == step>
        step-active
    <#elseif s < step>
        step-complete
    </#if>
}

@stepIcon(s: Int, defaultIcon: String) = {
    <#if s < step>
        fa-check-square
    <#else>
        @Html(defaultIcon)
    </#if>
}

<div class="project-create-steps col-md-2">
    <div class="project-create-step @stepState(1)">
        <div class="step-content">
            <i class="fas @stepIcon(1, "fa-upload")"></i> <strong>Upload version</strong>
        </div>
    </div>
    <div class="project-create-step @stepState(2)">
        <div class="step-content">
            <i class="fas @stepIcon(2, "fa-paper-plane")"></i> <strong>Publish version</strong>
        </div>
    </div>
</div>
