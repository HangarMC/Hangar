<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign scriptsVar>
    <script nonce="${nonce}">
        <#outputformat "JavaScript">
        window.MAX_REVIEW_TIME = ${mapper.valueToTree(config.queue.maxReviewTime.toMillis())};
        window.CURRENT_USER = ${mapper.valueToTree(cu)};
        window.UNDER_REVIEW = ${mapper.valueToTree(underReview)};
        window.NOT_STARTED = ${mapper.valueToTree(versions)};
        </#outputformat>
    </script>
    <script type="text/javascript" src="${hangar.url("js/version-queue.js")}"></script>
</#assign>

<#assign message><@spring.message "user.queue" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar>
    <div id="version-queue"></div>
</@base.base>
