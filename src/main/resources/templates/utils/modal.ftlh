<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#macro modal titleId id="" label="">
    <div class="modal fade" id="${id}" tabindex="-1" role="dialog" aria-labelledby="${label}">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"
                            aria-label="<@spring.message "general.close" />">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title"><@spring.message titleId /></h4>
                </div>
                <#nested>
            </div>
        </div>
    </div>
</#macro>
