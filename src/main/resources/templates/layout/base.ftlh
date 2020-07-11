<#--
@(title: String, additionalScripts: Html = Html(""), scriptsEnabled: Boolean = true, authEnabled: Boolean = true, showHeader: Boolean = true,
showFooter: Boolean = true, noContainer: Boolean = false, additionalMeta: Html = Html(""), additionalStyling: Html = Html(""))(content: Html)

(implicit request: OreRequest[_], messages: Messages, config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)
-->

<#macro base title additionalScripts="" scriptsEnabled=true authEnabled=true showHeader=true showFooter=true noContainer=false additionalMeta="" additionalStyling="">
    <!DOCTYPE html>
    <html lang="${@messages.lang.language}">
    <head>
        <link href="${@config.security.api.url}" rel="preconnect" crossorigin>
        <link href="${@config.app.discourseCdnUrl}" rel="preconnect" crossorigin>
        <link href="${@config.security.api.url}" rel="dns-prefetch">
        <link href="${@config.app.discourseCdnUrl}" rel="dns-prefetch">

        <link rel="prefetch" href="${@assetsFinder.path("images/favicon.ico")}">
        <link rel="prefetch" href="https://fonts.googleapis.com/css?family=Montserrat:400,700">
        <link rel="prefetch" href="${@assetsFinder.path("highlight/styles/default.css")}">
        <link rel="prefetch" href="${@assetsFinder.path("bootstrap/css/bootstrap.min.css")}">
        <link rel="prefetch" href="${@assetsFinder.path("build/commons.css")}">
        <link rel="prefetch" href="${@routes.Assets.versioned("build/main.css")}">
        <link rel="prefetch" href="${@assetsFinder.path("build/font-awesome.css")}">
        <link rel="prefetch" href="${@assetsFinder.path("manifest/manifest.json")}">
        <link rel="prefetch" href="${@routes.Assets.versioned("lib/jquery/dist/jquery.min.js")}">

        <title>${title}</title>

        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        ${additionalMeta}

        <link rel="apple-touch-icon" sizes="180x180"
              href="https://papermc.io/favicons/apple-touch-icon.png?v=BG70oBaPzN">
        <link rel="icon" type="image/png" sizes="32x32"
              href="https://papermc.io/favicons/favicon-32x32.png?v=BG70oBaPzN">
        <link rel="icon" type="image/png" sizes="16x16"
              href="https://papermc.io/favicons/favicon-16x16.png?v=BG70oBaPzN">
        <link href="https://fonts.googleapis.com/css?family=Montserrat:400,700" rel="stylesheet">
        <link rel="stylesheet" type="text/css" href="${@assetsFinder.path("highlight/styles/default.css")}"/>
        <link rel="stylesheet" type="text/css" href="${@assetsFinder.path("bootstrap/css/bootstrap.min.css")}"/>
        <link rel="stylesheet" type="text/css" href="${@assetsFinder.path("build/font-awesome.css")}"/>
        <link rel="stylesheet" type="text/css" href="${@assetsFinder.path("build/commons.css")}"/>
        <link rel="stylesheet" type="text/css" href="${@routes.Assets.versioned("build/main.css")}"/>
        ${additionalStyling}

        <#-- See https://goo.gl/OOhYW5 and https://goo.gl/qRE0vM -->
        <link rel="manifest" href="${@assetsFinder.path("manifest/manifest.json")}">
        <meta name="theme-color" content="#333">

        <script type="text/javascript" src="${@routes.Assets.versioned("lib/jquery/dist/jquery.min.js")}"></script>
    </head>
    <body class="site">
    <#if showHeader>
        <#import "header.ftlh" as header>
        <@header.header authEnabled=authEnabled />
    </#if>

    <div class="site-content">
        <#if noContainer>
            <#nested>
        <#else>
            <div class="container<#if showHeader> site-header-margin</#if>">
                <#if config.ore.staging>
                    <div class="row">
                        <div class="col-xs-12">
                            <div class="staging-warning"> ${@messages("general.stagingWarning")}</div>
                        </div>
                    </div>
                </#if>
                <div class="row">
                    <div class="col-xs-12">
                        <#import "../utils/alert.ftlh" as alert>
                        <@alert.alert "success" />
                        <@alert.alert "error" />
                        <@alert.alert "info" />
                        <@alert.alert "warning" />
                    </div>
                </div>

                <#nested>
            </div>
        </#if>
    </div>

    <#if showFooter>
        <#import "footer.ftlh" as footer>
        <@footer.footer />
    </#if>

    <#if scriptsEnabled>
        @if(request != null && play.filters.csrf.CSRF.getToken.isDefined) {
        <script @CSPNonce.attr>
            window.csrf = '@play.filters.csrf.CSRF.getToken.get.value';
            window.isLoggedIn = @request.hasUser;
            $.ajaxSetup({headers: {'Csrf-Token': csrf}});
        </script>
        }

        <script type="text/javascript" src="${@routes.Application.javascriptRoutes}"></script>
        <script type="text/javascript" src="${@assetsFinder.path("build/vendors.js")}"></script>
        <script type="text/javascript" src="${@assetsFinder.path("build/font-awesome.js")}"></script>
        <script type="text/javascript" src="${@assetsFinder.path("build/commons.js")}"></script>
        <script type="text/javascript" src="${@assetsFinder.path("lib/moment/min/moment.min.js")}"></script>
        <script type="text/javascript" src="${@assetsFinder.path("bootstrap/js/bootstrap.min.js")}"></script>
        <script type="text/javascript" src="${@assetsFinder.path("lib/filesize/lib/filesize.js")}"></script>
        <script type="text/javascript" src="${@assetsFinder.path("lib/clipboard/dist/clipboard.min.js")}"></script>
        <script type="text/javascript" src="${@assetsFinder.path("highlight/highlight.pack.js")}"></script>
        <script type="text/javascript" src="${@assetsFinder.path("javascripts/main.js")}"></script>
        <script type="text/javascript" src="${@assetsFinder.path("javascripts/apiRequests.js")}"></script>

        ${additionalScripts}
    </#if>

    </body>
    </html>
</#macro>
