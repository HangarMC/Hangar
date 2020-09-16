<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/form.ftlh" as form />
<#import "*/utils/csrf.ftlh" as csrf />
<#import "*/layout/base.ftlh" as base />

<#assign scriptsVar>
    <link rel="stylesheet" href="<@hangar.url "build/platform-version-table.css" />">
    <script type="text/javascript" src="<@hangar.url "javascripts/platformVersions.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "CSSescape/css.escape.js" />"></script>
    <script>
        PLATFORMS = ${platformVersions}
    </script>
    <script type="text/javascript" src="<@hangar.url "build/platform-version-table.js" />"></script>
</#assign>

<#assign message><@spring.message "admin.platformVersions.title" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar>
    <div id="platform-version-table"></div>
    <#--<#assign Platform=@helper["io.papermc.hangar.model.Platform"] />
    &lt;#&ndash; @ftlvariable name="Platform" type="io.papermc.hangar.model.Platform" &ndash;&gt;
    <#list platformVersions as platform, versions>
        <@form.form action="${Routes.UPDATE_PLATFORM_VERSIONS.getRouteUrl(platform.ordinal()?string)}" method="POST" id="${platform.getName()?lower_case}-form" class="form-inline">
            <@csrf.formField />
            <h3>${platform.getName()}</h3>
            <table class="table table-super-condensed table-striped table-bordered table-platform-versions" id="${platform.getName()?lower_case}-table">
                <#list versions as v>
                    <tr id="version-${v}">
                        <td>
                            ${v}
                        </td>
                        <td class="table-shrink">
                            <button class='btn btn-danger remove-version' data-version="${v}">
                                <span class='glyphicon glyphicon-remove'></span>
                            </button>
                        </td>
                    </tr>
                <#else>
                    <i>No versions</i>
                </#list>
            </table>
            <div class="form-group">
                <label for="${platform.getName()?lower_case}-input" class="sr-only">Add Version</label>
                <div class="input-group">
                    <div class="input-group-addon">Version</div>
                    <input type="text" class="form-control version-input" id="${platform.getName()?lower_case}-input" data-button="${platform.getName()?lower_case}-button">
                </div>
            </div>
            <button class="btn btn-primary add-version" type="button" id="${platform.getName()?lower_case}-button" data-table="${platform.getName()?lower_case}-table" data-input="${platform.getName()?lower_case}-input" disabled>
                <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
            </button>
            <button class="btn btn-success version-submit" type="submit" disabled>
                <span class="glyphicon glyphicon-floppy-disk"></span>
                Save
            </button>
            <div style="width: 80%; border-top: #555555 solid 1px; margin-top: 10px;"></div>
        </@form.form>
    </#list>-->
</@base.base>