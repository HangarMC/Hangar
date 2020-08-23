<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#assign scriptsVar>
    <script <#--@CSPNonce.attr--> type="text/javascript" src="<@hangar.url "javascripts/showNotes.js" />"></script>
    <script<#--@CSPNonce.attr-->> resourcePath = '${project.ownerName}/${project.slug}'</script>
</#assign>

<#assign message><@spring.message "notes" /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar>
    <div class="row">
        <div class="col-md-12  header-flags">
            <div class="clearfix">
                <h1 class="pull-left"><@spring.message "notes" /> <a href="${routes.getRouteUrl("projects.show", project.ownerName, project.slug)}">${project.ownerName}/${project.slug}</a></h1>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="input-group" style="width: 100%;
                margin-top: 1em;">
                <textarea type="text" class="form-control textarea-addmessage" placeholder="Message"></textarea>
                <div class="input-group-addon btn btn-note-addmessage-submit btn-primary"><i class="fas fa-clipboard"></i> <@spring.message "notes.addmessage" /></div>
            </div>
        </div>
    </div>
    <div class="row version-description">
        <div class="col-md-12">
            <h2><@spring.message "notes" /></h2>
        </div>
        <#if !notes?has_content>
            <div class="col-md-12">
                <div class="alert-notes alert alert-info" role="alert">
                    <i class="fas fa-info-circle"></i>
                    This project does not have notes
                </div>
            </div>
        </#if>

        <div class="col-md-12">
            <table class="table table-condensed setting-no-border table-notes-log">
                <tbody>
                <#list notes as note>
                <tr>
                    <td class="note-fixed-with">${utils.prettifyDateTime(note.toDateTime())}</td>
                    <td class="note-fixed-with"><strong>${note.getUser()!"Unknown"}</strong></td>
                    <td>${markdownService.render(note.getMessage())}</td>
                </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</@base.base>
