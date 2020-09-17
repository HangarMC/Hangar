<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro userSearch>
    <div class="input-group input-group user-search">
        <input type="text" class="form-control" placeholder="<@spring.message "org.users.add" />&hellip;">
        <div class="input-group-append input-group-btn">
            <button disabled class="btn btn-default btn-search"><i class="fas fa-search"></i></button>
        </div>
    </div>
</#macro>

