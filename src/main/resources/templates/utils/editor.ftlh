<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />

<#--
@import views.html.helper.{CSRF, form}
@(saveCall: Call = null,
  deleteCall: Call = null,
  savable: Boolean = true,
  deletable: Boolean = false,
  enabled: Boolean,
  raw: String = "",
  cooked: Html = Html(""),
  subject: String = null,
  cancellable: Boolean = true,
  targetForm: String = null,
  extraFormValue: String = null)(implicit messages: Messages, request: Request[_])
-->

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
                        <h4 class="modal-title" id="label-page-delete">Delete ${subject.toLowerCase}</h4>
                    </div>
                    <div class="modal-body">
                        Are you sure you want to delete this ${subject.toLowerCase}? This cannot be undone.
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                        @form(action = deleteCall, Symbol("class") -> "form-inline") {
                            @CSRF.formField
                            <button type="submit" class="btn btn-danger">Delete</button>
                        }
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
        @form(action = saveCall, Symbol("id") -> "form-editor-save") {
            @CSRF.formField
            <#if extraFormValue??>
                <input type="hidden" value="@extraFormValue" name="name">
            </#if>
        }
    </#if>
</#if>

<!-- Saved window -->
<div class="page-content page-rendered">${cooked}</div>
