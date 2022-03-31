<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#function stepState s step>
    <#if s == step>
        <#return "step-active">
    <#elseif s < step>
        <#return "step-complete">
    <#else>
        <#return "">
    </#if>
</#function>

<#function stepIcon s defaultIcon step>
    <#if s < step>
        <#return "fa-check-square">
    <#else>
        <#return defaultIcon>
    </#if>
</#function>

<#macro createSteps step>
<div class="project-create-steps col-md-2">
    <div class="project-create-step ${stepState(1, step)}">
        <div class="step-content">
            <i class="fas ${stepIcon(1, "fa-upload", step)}"></i> <strong>Upload version</strong>
        </div>
    </div>
    <div class="project-create-step ${stepState(2, step)}">
        <div class="step-content">
            <i class="fas ${stepIcon(2, "fa-paper-plane", step)}"></i> <strong>Publish version</strong>
        </div>
    </div>
</div>
</#macro>
