<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/form.ftlh" as form />
<#import "*/utils/csrf.ftlh" as csrf />
<#import "*/layout/base.ftlh" as base />

<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/platformVersions.js" />"></script>
</#assign>

<#assign message><@spring.message "admin.platformVersions.title" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar>
    <#assign Platform=@helper["io.papermc.hangar.model.Platform"] />
    <#-- @ftlvariable name="Platform" type="io.papermc.hangar.model.Platform" -->
    <#list platformVersions as platform, versions>
        <@form.form action="${Routes.UPDATE_PLATFORM_VERSIONS.getRouteUrl(platform.ordinal()?string)}" method="POST" id="${platform.getName()?lower_case}-form" class="form-inline">
            <@csrf.formField />
            <h3>${platform.getName()}</h3>
            <table class="table table-super-condensed" id="${platform.getName()?lower_case}-table">
                <#list versions as v>
                    <tr id="version-${v}">
                        <td>
                            ${v}
                        </td>
                        <td>
                            <button class='btn btn-danger remove-version' data-version="${v}">
                                <span class='glyphicon glyphicon-remove'></span>
                            </button>
                        </td>
                    </tr>
                </#list>
            </table>
            <div class="form-group">
                <label for="${platform.getName()?lower_case}-add" class="sr-only">Add Version</label>
                <div class="input-group">
                    <div class="input-group-addon">Version</div>
                    <input type="text" class="form-control version-input" id="${platform.getName()?lower_case}-input" data-button="${platform.getName()?lower_case}-button">
                </div>
            </div>
            <button class="btn btn-primary add-version" type="button" id="${platform.getName()?lower_case}-button" data-table="${platform.getName()?lower_case}-table" data-input="${platform.getName()?lower_case}-input" disabled>
                <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
                Add
            </button>
            <button class="btn btn-success" type="submit">
                <span class="glyphicon glyphicon-floppy-disk"></span>
                Save Changes
            </button>
            <div style="width: 80%; border-top: #555555 solid 1px; margin-top: 10px;"></div>
        </@form.form>
    </#list>
</@base.base>