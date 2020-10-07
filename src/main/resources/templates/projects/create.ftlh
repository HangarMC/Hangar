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
                    </div>
                </div>
            </div>
        </div>
    </div>
</@base.base>
