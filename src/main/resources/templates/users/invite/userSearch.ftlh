<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#--@()(implicit messages: Messages)-->
<#macro userSearch>
    <div class="input-group input-group-sm user-search">
        <input type="text" class="form-control" placeholder="<@spring.message "org.users.add" />&hellip;" />
        <span class="input-group-btn">
            <button disabled class="btn btn-default btn-search"><i class="fas fa-search"></i></button>
        </span>
    </div>
</#macro>

