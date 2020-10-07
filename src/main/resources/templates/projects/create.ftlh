<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#--
Page used for uploading and creating new projects.
-->
<#assign scriptsVar>
    <script nonce="${nonce}">
        window.CURRENT_USER = ${mapper.valueToTree(cu)};
        window.ORGANIZAITONS = ${mapper.valueToTree(createProjectOrgas)};
    </script>
    <script type="text/javascript" src="<@hangar.url "js/create-project.js" />"></script>
    <#--<script type="text/javascript" src="<@hangar.url "js/projectCreateValidate.js" />"></script>-->
</#assign>
<#assign styleVar>
    <link rel="stylesheet" href="<@hangar.url "css/create-project.css" />">
</#assign>

<#assign message><@spring.message "project.create" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar additionalStyling=styleVar>

    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">
                    <@spring.message "project.create.title" />
                    </h3>
                </div>

                <div class="card-body project-body">
                    <div class="minor create-blurb">
                        <p><@spring.message "project.create.infoText.head" /></p>
                        <p><@spring.message "project.create.infoText.guidelines" /></p>
                    </div>

                    <div>
                        <div id="create-project"></div>
                        <#--<#import "*/utils/form.ftlh" as form>
                        <@form.form action=Routes.PROJECTS_CREATE_PROJECT.getRouteUrl() method="POST">
                            <#import "*/utils/csrf.ftlh" as csrf>
                            <@csrf.formField />
                            <div class="form-group">
                                <label for="projectName"><@spring.message "project.create.input.name" /></label>
                                <input type="text" id="projectName" name="name" class="form-control">
                            </div>

                            <div class="form-group">
                                <label for="projectCategory"><@spring.message "project.create.input.category" /></label>
                                <select id="projectCategory" name="category" class="form-control">
                                <#assign Category=@helper["io.papermc.hangar.model.Category"]>
                                <#list Category.values() as cat>
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
                                    <option value="${cu.id}">${cu.name}</option>
                                    <#list createProjectOrgas as orga>
                                        <option value="${orga.id}">${orga.name}</option>
                                    </#list>
                                </select>
                            </div>

                            <button type="submit" class="btn btn-primary">Create project</button>
                        </@form.form>-->
                    </div>
                </div>
            </div>
        </div>
    </div>
</@base.base>
