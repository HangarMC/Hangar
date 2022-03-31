<#macro announcement>
    <#list config.announcements as announcement>
        <div class="row">
            <div class="col-12">
                <div class="announcement" style="background-color: ${announcement.color}">${announcement.text}</div>
            </div>
        </div>
    </#list>
</#macro>
