<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/form.ftlh" as form>
<#import "*/utils/csrf.ftlh" as csrf>
<#import "*/projects/view.ftlh" as projects />
<#import "*/projects/helper/btnHide.ftlh" as btnHide />
<#import "*/projects/helper/inputSettings.ftlh" as inputSettings />
<#import "*/utils/userAvatar.ftlh" as userAvatar />
<#import "*/users/memberList.ftlh" as memberList />

<#assign Permission=@helper["io.papermc.hangar.model.Permission"] />
<#assign Role=@helper["io.papermc.hangar.model.Role"] />
<#assign scriptsVar>
    <script nonce="${nonce}">
        <#outputformat "JavaScript">
        window.PROJECT = ${mapper.valueToTree(p)};
        window.DEPLOYMENT_KEY = ${mapper.valueToTree(deploymentKey)};

        window.FILTERED_MEMBERS = ${utils.serializeMap(p.filteredMembers(headerData))};
        window.PERMISSIONS = {
            SEE_HIDDEN: ${headerData.globalPerm(Permission.SeeHidden)?c},
            MANAGE_MEMBERS: ${sp.perms(Permission.ManageSubjectMembers)?c},
            EDIT_API_KEYS: ${sp.perms(Permission.EditApiKeys)?c},
            DELETE_PROJECT: ${sp.perms(Permission.DeleteProject)?c},
            HARD_DELETE_PROJECT: ${headerData.globalPerm(Permission.HardDeleteProject)?c},
        }
        window.POSSIBLE_ROLES = ${mapper.valueToTree(Role.values()?filter(role -> role.category == p.roleCategory && role.isAssignable())?sort_by("permissions")?reverse)};
        window.PROJECT_OWNER_PERM = ${Permission.IsSubjectOwner.value};
        </#outputformat>
    </script>
    <script type="text/javascript" src="${hangar.url("js/project-settings.js")}"></script>
</#assign>
<#assign styleVar>
    <link rel="stylesheet" href="${hangar.url("css/project-settings.css")}">
</#assign>

<@projects.view p=p sp=sp active="#settings" additionalScripts=scriptsVar additionalStyling=styleVar>
    <div id="project-settings">
    </div>
</@projects.view>
