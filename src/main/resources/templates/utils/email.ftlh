<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#-- @ftlvariable name="title" type="java.lang.String" -->
<#-- @ftlvariable name="recipient" type="java.lang.String" -->
<#-- @ftlvariable name="body" type="java.lang.String" -->
<#assign message><@spring.message title /></#assign>
<@base.base title=message scriptsEnabled=false showHeader=false showFooter=false>
    <div class="container">
        <p>${recipient},</p>
        <p><@spring.message body /></p>
        <p><@spring.message "email.signature" /></p>
    </div>
</@base.base>
