<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#--
@*
Page used for uploading and creating new projects.
*@
@import play.twirl.api.Html

@import controllers.sugar.Requests.OreRequest
@import ore.OreConfig
@import ore.data.project.Category
@import ore.db.Model
@import ore.models.organization.Organization
@import ore.models.user.User
@import views.html.helper.{CSRF, form}
@(createProjectOrgas: Seq[Model[Organization]], user: Model[User])(implicit messages: Messages, flash: Flash, request: OreRequest[_], config: OreConfig, assetsFinder: AssetsFinder)
-->
<#assign scriptsVar>
    <script type="text/javascript" src="<@hangar.url "javascripts/projectCreateValidate.js" />"></script>
    <script type="text/javascript" src="<@hangar.url "javascripts/pluginUpload.js" />"></script>
</#assign>

<#assign message><@spring.message "project.create" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar>

    <div class="row">
        <div class="col-md-10">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">
                    <@spring.message "project.create.title" />
                    </h3>
                </div>

                <div class="panel-body project-body">
                    <div class="minor create-blurb">
                        <p><@spring.message "project.create.infoText.head" /></p>
                        <p><@spring.message "project.create.infoText.guidelines" /></p>
                    </div>

                    <div>
                        @form(action = routes.getRouteUrl("projects.createProject")) {
                            @CSRF.formField
                            <div class="form-group">
                                <label for="projectName"><@spring.message "project.create.input.name" /></label>
                                <input type="text" id="projectName" name="name" class="form-control">
                            </div>

                            <div class="form-group">
                                <label for="projectPluginId"><@spring.message "project.create.input.pluginId" /></label>
                                <input type="text" id="projectPluginId" name="pluginId" class="form-control">
                            </div>

                            <div class="form-group">
                                <label for="projectCategory"><@spring.message "project.create.input.category" /></label>
                                <select id="projectCategory" name="category" class="form-control">
                                    <#-- todo: Category.visible -->
                                <#list Category.visible as cat>
                                    <option>${cat.title}</option>
                                </#list>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="projectDescription"><@spring.message "project.create.input.description" /></label>
                                <input type="text" id="projectDescription" name="description" class="form-control">
                            </div>

                            <div class="form-group">
                                <label for="projectCategory"><@spring.message "project.owner" /></label>
                                <select id="projectCategory" name="owner" class="form-control">
                                    <option value="@user.id.value">@user.name</option>
                                    <#list createProjectOrgas as orga>
                                        <option value="${orga.id.value}">${orga.name}</option>
                                    </#list>
                                </select>
                            </div>

                            <button type="submit" class="btn btn-primary">Create project</button>
                        }
                    </div>
                </div>
            </div>
        </div>
    </div>
</@base.base>
