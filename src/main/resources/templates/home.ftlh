<#--
The main entry point of Hangar. This page displays a list of Projects that can be
sorted according to different criteria.
-->

<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base>

<#macro scripts>
    <script src="<@hangar.url "build/home.js" />"></script>
</#macro>

<#macro stylesheets>
    <link rel="stylesheet" type="text/css" href="<@hangar.url "build/home.css" />" />
</#macro>

<#macro meta>
    <meta property="og:title" content="<@spring.message "general.appName" />">
    <meta property="og:type" content="website" />
    <meta property="og:url" content="${config.baseUrl}">
    <meta property="og:image" content="<@hangar.url "images/ore-dark.png" />" />
    <meta property="og:description" content="<@spring.message "general.description" />" />
</#macro>

<#function randomSponsor>
    <#local index = rand.nextInt(config.getSponsors()?size) />
    <#return config.sponsors[index] />
</#function>

<#assign message><@spring.message "general.title" /></#assign>
<#assign scriptsVar><@scripts /></#assign>
<#assign stylesheetsVar><@stylesheets /></#assign>
<#assign metaVar><@meta /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar additionalMeta=metaVar additionalStyling=stylesheetsVar>
    <#-- Header -->
    <div class="index-header">
        <div class="row centered-content-row">
            <div class="col-md-9 ore-banner">
                <div class="row aligned-row">
                    <div class="col-xs-2 ore-logo">
                        <img src="<@hangar.url "images/ore-colored.svg" />" alt="Ore logo" />
                    </div>
                    <div class="col-xs-10 text">
                        <div class="headline">Hangar</div>
                        <div>A Minecraft package repository</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3 sponsor">
                <div class=".card sponsor-panel">
                    <span>Sponsored by</span>
                    <div class="card-body">
                        <#-- @ftlvariable name="sponsor" type="io.papermc.hangar.config.hangar.HangarConfig.Sponsor" -->
                        <#assign sponsor=randomSponsor() />
                        <a href="${sponsor.link}">
                            <img class="logo" src="${sponsor.image}" alt="${sponsor.name}" />
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="home"></div>
</@base.base>
