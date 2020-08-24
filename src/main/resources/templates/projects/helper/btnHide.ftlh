<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#assign Visibility=@helper["io.papermc.hangar.model.Visibility"] />
<#macro btnHide namespace projectVisibility>
    <div class="btn-group">
        <button class="btn btn-sm btn-alert btn-hide-dropdown dropdown-toggle" type="button" id="visibility-actions" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" data-project="${namespace}" style="color: black">
            <i class="fas fa-eye"></i> Visibility actions
            <span class="caret"></span>
        </button>
        <ul class="dropdown-menu" aria-labelledby="visibility-actions">
            <#list Visibility.values() as visibility>
                <li>
                    <a href="#" class="btn-visibility-change" data-project="${namespace}" data-level="${visibility.getName()}" data-modal="${visibility.showModal?string('true', 'false')}" >
                        <@spring.message "visibility.name." + visibility.getName() /> <#if projectVisibility == visibility>  <i class="fa fa-check" style="color: black" aria-hidden="true"></i> </#if>
                    </a>
                </li>
            </#list>
        </ul>
    </div>
</#macro>
