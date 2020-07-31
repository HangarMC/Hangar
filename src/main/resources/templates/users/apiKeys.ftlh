<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/users/view.ftlh" as userView />

<#--@(u: UserData, o: Option[(OrganizationData, ScopedOrganizationData)], keys: Seq[ApiKey], perms: Seq[NamedPermission])(implicit messages: Messages, flash: Flash, request: OreRequest[_], config: OreConfig, assetsFinder: AssetsFinder)-->
<#-- @ftlvariable name="u" type="me.minidigger.hangar.model.viewhelpers.UserData" -->
<#-- @ftlvariable name="keys" type="java.util.List<ApiKey>" -->
<#-- @ftlvariable name="perms" type="java.util.Collection<NamedPermission>" -->
<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/apiKeysManagement.js" />"></script>
    <script <#--@CSPNonce.attr-->>
        NO_PERMS_SET = '<@spring.message "user.apiKeys.error.noPermsSet" />';
        NO_NAME_SET = '<@spring.message "user.apiKeys.error.noNameSet" />';
        TOO_LONG_NAME = '<@spring.message "user.apiKeys.error.tooLongName" />';
        NAMED_USED = '<@spring.message "user.apiKeys.error.nameAlreadyUsed" />';
        DELETE_KEY = '<@spring.message "user.apiKeys.keyDeleteButton" />';
    </script>
</#assign>

<#assign NamedPermission=@helper["me.minidigger.hangar.model.NamedPermission"] />
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
                <#-- @ftlvariable name="key" type="me.minidigger.hangar.model.viewhelpers.ApiKey" -->
                <#list keys as key>
                    <tr class="api-key-row">
                        <th class="api-key-name">${key.key.name}</th>
                        <th></th>
                        <th>${key.key.tokenIdentifier}</th>
                        <th>${key.namedRawPermissions?map(np -> np.name())?join(", ")}</th>
                        <th><button class="btn btn-danger api-key-row-delete-button"><@spring.message "user.apiKeys.keyDeleteButton" /></button></th>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</@userView.view>
