<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/users/view.ftlh" as userView />

<#assign scriptsVar>
    <script nonce="${nonce}">
        <#outputformat "JavaScript">
        window.API_KEY_PERMISSIONS = ${mapper.valueToTree(perms)};
        window.EXISTING_KEYS = ${mapper.valueToTree(keys)};
        </#outputformat>
    </script>
    <script type="text/javascript" src="${hangar.url("js/api-key-management.js")}"></script>
</#assign>

<#assign NamedPermission=@helper["io.papermc.hangar.model.NamedPermission"] />
<@userView.view u=u o=o so=so additionalScripts=scriptsVar>
    <div id="api-key-management"></div>
</@userView.view>
