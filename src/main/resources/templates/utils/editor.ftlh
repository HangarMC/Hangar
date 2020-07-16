<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/form.ftlh" as form>
<#import "*/utils/csrf.ftlh" as csrf>

<#macro editor saveCall="" deleteCall="" savable=true deletable=false enabled=true raw="" cooked="" subject="" cancellable=true targetForm="" extraFormValue="">
    <#if enabled>
        <!-- Edit -->
        <button type="button" class="btn btn-sm btn-edit btn-page btn-default" title="<@spring.message "general.edit" />">
            <i class="fas fa-edit"></i> <@spring.message "general.edit" />
        </button>

        <!-- Preview -->
        <div class="btn-edit-container btn-preview-container" title="<@spring.message "general.preview" />">
            <button type="button" class="btn btn-sm btn-preview btn-page btn-default">
                <i class="fas fa-eye"></i>
            </button>
        </div>

        <!-- Save -->
        <#if savable>
            <div class="btn-edit-container btn-save-container" title="<@spring.message "general.save" />">
                <button form="form-editor-save" type="submit" class="btn btn-sm btn-save btn-page btn-default">
                    <i class="fas fa-save"></i>
                </button>
            </div>
        </#if>

        <!-- Cancel -->
        <#if cancellable>
            <div class="btn-edit-container btn-cancel-container" title="<@spring.message "general.cancel" />">
                <button type="button" class="btn btn-sm btn-cancel btn-page btn-default">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </#if>

        <!-- Delete -->
        <#if deletable>
            <div class="btn-edit-container btn-delete-container" title="<@spring.message "general.delete" />">
                <button type="button" class="btn btn-sm btn-page-delete btn-page btn-default"
                data-toggle="modal" data-target="#modal-page-delete">
                    <i class="fas fa-trash"></i>
                </button>
            </div>

            <div class="modal fade" id="modal-page-delete" tabindex="-1" role="dialog"
                 aria-labelledby="label-page-delete">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                            <h4 class="modal-title" id="label-page-delete">Delete ${subject?lower_case}</h4>
                        </div>
                        <div class="modal-body">
                            Are you sure you want to delete this ${subject?lower_case}? This cannot be undone.
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                            <@form.form method="POST" action=deleteCall class="form-inline">
                                <@csrf.formField />
                                <button type="submit" class="btn btn-danger">Delete</button>
                            </@form.form>
                        </div>
                    </div>
                </div>
            </div>
        </#if>

        <!-- Edit window -->
        <div class="page-edit" style="display: none ;">
            <textarea name="content" class="form-control" <#if targetForm??>
                form="${targetForm}"
            <#else>
                form="form-editor-save"
            </#if>>${raw}</textarea>
        </div>

        <!-- Preview window -->
        <div class="page-preview page-rendered" style="display: none ;"></div>

        <#if savable>
            <@form.form method="POST" action=saveCall id="form-editor-save">
                <@csrf.formField />
                <#if extraFormValue??>
                    <input type="hidden" value="@extraFormValue" name="name">
                </#if>
            </@form.form>
        </#if>
    </#if>

    <!-- Saved window -->
    <div class="page-content page-rendered">${cooked}</div>
</#macro>
