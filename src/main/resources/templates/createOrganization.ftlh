<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign scripts>
    <script type="text/javascript" src="<@hangar.url "javascripts/orgCreate.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/userSearch.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/membersConfig.js" />"></script>
</#assign>

<#assign message><@spring.message "org.create.title" /></#assign>
<@base.base title="${message}" additionalScripts=scripts>
    <div class="row">
        <div class="col-md-8 col-md-offset-2">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">
                    <@spring.message "org.create.title" />
                    </h3>
                </div>

                <div class="project-body panel-body">
                    <p class="minor"> <@spring.message "org.info" /></p>

                    <#import "*/utils/form.ftlh" as form>
                    <@form.form action=routes.getRouteUrl("org.create") method="POST" id="form-continue">
                        <#import "*/utils/csrf.ftlh" as csrf>
                        <@csrf.formField />
                        <div class="setting">
                            <div class="setting-description">
                                <h4><@spring.message "org.name" /></h4>
                            </div>
                            <input name="name" type="text" class="input-name form-control"
                            placeholder="PaperMC" maxlength="20" />
                            <i class="fas fa-spinner fa-spin status-org-name" style="display: none;"></i>
                            <div class="clearfix"></div>
                        </div>
                    </@form.form>

                    <#import "*/users/invite/form.ftlh" as userForm>
<#--                    <@userForm.form owner=request.headerData.currentUser roleCategory=RoleCategory.Organization />-->
                    <@userForm.form owner="" roleCategory="" />

                    <button title="Continue" type="submit" name="create" form="form-continue"
                    value="<@spring.message "general.continue" />"
                    class="push-down pull-right continue-btn btn btn-primary" disabled>
                    <@spring.message "general.create" />
                    </button>
                </div>
            </div>
        </div>
    </div>
</@base.base>
