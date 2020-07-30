<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/utils/csrf.ftlh" as csrf />

<#--@import ore.OreConfig-->
<#--@import ore.db.Model-->
<#--@import ore.models.project.{Page, Project}-->
<#--@import util.syntax._-->
<#--@import views.html.helper.CSPNonce-->

<#--@(model: Project, rootPages: Seq[Model[Page]])(implicit messages: Messages, request: RequestHeader, config: OreConfig)-->

<#macro pageCreate project, rootPages>
    <script <#--@CSPNonce.attr-->>
        PROJECT_OWNER = '${project.ownerName}';
        PROJECT_SLUG = '${project.slug}';
    </script>

    <div class="modal fade" id="new-page" tabindex="-1" role="dialog" aria-labelledby="new-page-label">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <
                    <button type="button" class="close" data-dismiss="modal" aria-label="<@spring.message "general.close" />">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title" id="new-page-label"><@spring.message "page.new.title" /></h4>
                    <h4 class="modal-title" id="new-page-label-error" style="display: none;
                        color: red">
                        <@spring.message "page.new.error" />
                    </h4>
                </div>
                <div class="modal-body input-group">
                    <div class="setting">
                        <div class="setting-description">
                            <h4><@spring.message "project.page.name" /></h4>
                            <p><@spring.message "project.page.name.info" /></p>
                        </div>
                        <div class="setting-content">
                            <input class="form-control" type="text" id="page-name" name="page-name">
                        </div>
                        <div class="clearfix"></div>
                    </div>
                    <div class="setting setting-no-border">
                        <div class="setting-description">
                            <h4><@spring.message "project.page.parent" /></h4>
                            <p><@spring.message "project.page.parent.info" /></p>
                        </div>
                        <div class="setting-content">
                            <select class="form-control select-parent">
                                <option selected value="-1">&lt;none&gt;</option>
                                <#list rootPages?keys?filter(x -> x.name != config.pages.home.name) as singlePage>
                                    <option value="${singlePage.id}" data-slug="${singlePage.slug}">${singlePage.name}</option>
                                </#list>
                            </select>
                        </div>
                        <div class="clearfix"></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <@csrf.formField />
                    <button type="button" class="btn btn-default" data-dismiss="modal"><@spring.message "general.close" /></button>
                    <button id="continue-page" type="button" class="btn btn-primary"><@spring.message "general.continue" /></button>
                </div>
            </div>
        </div>
    </div>
</#macro>

