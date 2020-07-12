<#--
The main entry point of Ore. This page displays a list of Projects that can be
sorted according to different criteria.
-->

<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base>

<#function randomSponsor>
    val logos = config.sponge.sponsors

    val index = new Random().nextInt(logos.size)
    logos(index)
    <#-- todo implement random sponsor stuff -->
    <#return { "link": "https://minidigger.me", "image": "https://avatars2.githubusercontent.com/u/2185527?s=400&v=4"}>
</#function>

<#macro scripts>
    <script src="<@spring.url "build/home.js" />"></script>
</#macro>

<#macro stylesheets>
    <link rel="stylesheet" type="text/css" href="<@spring.url "build/home.css" />" />
</#macro>

<#macro meta>
    <meta property="og:title" content="<@spring.message "general.appName" />">
    <meta property="og:type" content="website" />
    <meta property="og:url" content="@config.app.baseUrl">
    <meta property="og:image" content="<@spring.url "images/ore-dark.png" />" />
    <meta property="og:description" content="<@spring.message "general.description" />" />
</#macro>

<#assign message><@spring.message "general.title" /></#assign>
<#assign scriptsVar><@scripts /></#assign>
<#assign stylesheetsVar><@stylesheets /></#assign>
<#assign metaVar><@meta /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar additionalMeta=metaVar additionalStyling=stylesheetsVar>
    <!-- Header -->
    <div class="index-header">
        <div class="row centered-content-row">
            <div class="col-md-9 ore-banner">
                <div class="row aligned-row">
                    <div class="col-xs-2 ore-logo">
                        <img src="<@spring.url "images/ore-colored.svg" />" alt="Ore logo" />
                    </div>
                    <div class="col-xs-10 text">
                        <div class="headline">Hangar</div>
                        <div>A Minecraft package repository</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3 sponsor">
                <div class="panel sponsor-panel">
                    <span>Sponsored by</span>
                    <div class="panel-body">
                        <#assign sponsor=randomSponsor() />
                        <a href="${sponsor.link}">
                            <img class="logo" src="<@spring.url sponsor.image />" alt="Sponsor" />
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="home"></div>
</@base.base>
