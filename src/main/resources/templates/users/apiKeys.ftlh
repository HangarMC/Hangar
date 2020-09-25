<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/users/view.ftlh" as userView />

<#--@(u: UserData, o: Option[(OrganizationData, ScopedOrganizationData)], keys: Seq[ApiKey], perms: Seq[NamedPermission])(implicit messages: Messages, flash: Flash, request: OreRequest[_], config: OreConfig, assetsFinder: AssetsFinder)-->
<#assign scriptsVar>
    <script <#--@CSPNonce.attr-->>
      window.NO_PERMS_SET = '<@spring.message "user.apiKeys.error.noPermsSet" />';
      window.NO_NAME_SET = '<@spring.message "user.apiKeys.error.noNameSet" />';
      window.TOO_LONG_NAME = '<@spring.message "user.apiKeys.error.tooLongName" />';
      window.NAMED_USED = '<@spring.message "user.apiKeys.error.nameAlreadyUsed" />';
      window.DELETE_KEY = '<@spring.message "user.apiKeys.keyDeleteButton" />';
    </script>
    <script type="text/javascript" src="<@hangar.url "build/js/apiKeysManagement.js" />"></script>
</#assign>

<#assign NamedPermission=@helper["io.papermc.hangar.model.NamedPermission"] />
<@userView.view u=u o=o additionalScripts=scriptsVar>
    <div id="keyAlert" class="alert alert-danger" role="alert" style="display: none;"></div>

    <div class="row">
        <div class="col-md-6">
            <h2><@spring.message "user.apiKeys.createNew" /></h2>
            <div id="api-create-key-form">
                <div class="row">
                    <#list perms?chunk( (perms?size/2)?int ) as chunk>
                        <div class="col-md-6">
                            <#list chunk as perm>
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" id="perm.${perm.name()}"> ${perm.name()}
                                    </label>
                                </div>
                            </#list>
                        </div>
                    </#list>

                </div>

                <div class="form-group">
                    <label for="keyName"><@spring.message "user.apiKeys.keyName" />:</label>
                    <input type="text" class="form-control" id="keyName">
                </div>
                <button id="button-create-new-key" class="btn btn-default"><@spring.message "user.apiKeys.createKeyBtn" /></button>
            </div>
        </div>

        <div class="col-md-6">
            <h2><@spring.message "user.apiKeys.existingKeys" /></h2>
            <table class="table">
                <thead>
                <tr>
                    <th><@spring.message "user.apiKeys.keyName" /></th>
                    <th><@spring.message "user.apiKeys.keyToken" /></th>
                    <th><@spring.message "user.apiKeys.keyIdentifier" /></th>
                    <th><@spring.message "user.apiKeys.keyPermissions" /></th>
                    <th><@spring.message "user.apiKeys.keyDeleteColumn" /></th>
                </tr>
                </thead>
                <tbody id="api-key-rows">
                <#list keys as key>
                    <tr class="api-key-row">
                        <th class="api-key-name">${key.name}</th>
                        <th></th>
                        <th>${key.tokenIdentifier}</th>
                        <th>${key.namedRawPermissions?map(np -> np.name())?join(", ")}</th>
                        <th><button class="btn btn-danger api-key-row-delete-button"><@spring.message "user.apiKeys.keyDeleteButton" /></button></th>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</@userView.view>
