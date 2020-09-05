<#--
@(title: String, additionalScripts: Html = Html(""), scriptsEnabled: Boolean = true, authEnabled: Boolean = true, showHeader: Boolean = true,
showFooter: Boolean = true, noContainer: Boolean = false, additionalMeta: Html = Html(""), additionalStyling: Html = Html(""))(content: Html)

(implicit request: OreRequest[_], messages: Messages, config: OreConfig, flash: Flash, assetsFinder: AssetsFinder)
-->
<@compress single_line=true>
<#import "/spring.ftl" as spring />
<#import "../utils/hangar.ftlh" as hangar />

<#macro base title additionalScripts="" scriptsEnabled=true authEnabled=true showHeader=true showFooter=true noContainer=false additionalMeta="" additionalStyling="">
    <!DOCTYPE html>
    <html lang="<@spring.message "general.language" />">
    <head>
        <link href="<@hangar.config "security.api.url" />" rel="preconnect" crossorigin>
        <link href="<@hangar.config "app.discourseCdnUrl" />" rel="preconnect" crossorigin>
        <link href="<@hangar.config "security.api.url" />" rel="dns-prefetch">
        <link href="<@hangar.config "app.discourseCdnUrl" />" rel="dns-prefetch">

        <link rel="prefetch" href="<@hangar.url "images/favicon.ico" />">
        <link rel="prefetch" href="https://fonts.googleapis.com/css?family=Montserrat:400,700">
        <link rel="prefetch" href="<@hangar.url "highlight/styles/default.css" />">
        <link rel="prefetch" href="<@hangar.url "bootstrap/css/bootstrap.min.css" />">
        <link rel="prefetch" href="<@hangar.url "build/commons.css" />">
        <link rel="prefetch" href="<@hangar.url "build/main.css" />">
        <link rel="prefetch" href="<@hangar.url "build/font-awesome.css" />">
        <link rel="prefetch" href="<@hangar.url "manifest/manifest.json" />">
        <link rel="prefetch" href="<@hangar.url "lib/jquery/dist/jquery.min.js" />">

        <title>${title} | Hangar</title>

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
        <link rel="stylesheet" type="text/css" href="<@hangar.url "highlight/styles/default.css" />"/>
        <link rel="stylesheet" type="text/css" href="<@hangar.url "bootstrap/css/bootstrap.min.css" />"/>
        <link rel="stylesheet" type="text/css" href="<@hangar.url "build/font-awesome.css" />"/>
        <link rel="stylesheet" type="text/css" href="<@hangar.url "build/commons.css" />"/>
        <link rel="stylesheet" type="text/css" href="<@hangar.url "build/main.css" />"/>
        ${additionalStyling}

        <#-- See https://goo.gl/OOhYW5 and https://goo.gl/qRE0vM -->
        <link rel="manifest" href="<@hangar.url "manifest/manifest.json" />">
        <meta name="theme-color" content="#333">

        <script type="text/javascript" src="<@hangar.url "lib/jquery/dist/jquery.min.js" />"></script>
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
                <#assign var><@hangar.config "ore.staging" /></#assign>
                <#if var == "true">
                    <div class="row">
                        <div class="col-xs-12">
                            <div class="staging-warning"><@spring.message "general.stagingWarning" /></div>
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
        <#if _csrf?? && _csrf.token??>
            <script>
                window.csrf = '${_csrf.token}';
                window.ajaxSettings = {"headers": { '${_csrf.headerName}': window.csrf}};
                window.isLoggedIn = ${headerData.hasUser()?c};
                $.ajaxSetup(window.ajaxSettings);
            </script>
        </#if>

        <script type="text/javascript" src="${Routes.JAVA_SCRIPT_ROUTES.getRouteUrl()}"></script>
        <script type="text/javascript" src="<@hangar.url "build/vendors.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "build/font-awesome.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "build/commons.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "lib/moment/min/moment.min.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "bootstrap/js/bootstrap.min.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "lib/filesize/lib/filesize.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "lib/clipboard/dist/clipboard.min.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "highlight/highlight.pack.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "javascripts/main.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "javascripts/apiRequests.js" />"></script>

        ${additionalScripts}
    </#if>

    </body>
    </html>
</#macro>
</@compress>
