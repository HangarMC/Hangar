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
        <link href="${config.security.api.url}">
        <link href="${config.security.api.url}" rel="preconnect" crossorigin>
<#--        <link href="<@hangar.config "app.discourseCdnUrl" />" rel="preconnect" crossorigin>--><#--TODO this-->
        <link href="${config.security.api.url}" rel="dns-prefetch">
<#--        <link href="<@hangar.config "app.discourseCdnUrl" />" rel="dns-prefetch">--><#--TODO this-->

        <link rel="prefetch" href="<@hangar.url "images/favicon/favicon.ico" />">
        <link rel="prefetch" href="https://fonts.googleapis.com/css?family=Montserrat:400,700">
        <link rel="prefetch" href="<@hangar.url "css/chunk-common.css" />">
        <link rel="prefetch" href="<@hangar.url "css/chunk-vendors.css" />">
        <link rel="prefetch" href="<@hangar.url "css/main.css" />">

        <link rel="prefetch" href="<@hangar.url "manifest/manifest.json" />">
        <link rel="prefetch" href="<@hangar.url "js/bootstrap.js" />">

        <title>${title} | Hangar</title>

        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        ${additionalMeta}

        <link rel="apple-touch-icon" sizes="180x180" href="/assets/images/favicon/apple-touch-icon.png">
        <link rel="icon" type="image/png" sizes="32x32" href="/assets/images/favicon/favicon-32x32.png">
        <link rel="icon" type="image/png" sizes="16x16" href="/assets/images/favicon/favicon-16x16.png">

        <link rel="stylesheet" type="text/css" href="https://fonts.googleapis.com/css?family=Montserrat:400,700&display=swap" >
        <link rel="stylesheet" type="text/css" href="<@hangar.url "css/chunk-common.css" />"/>
        <link rel="stylesheet" type="text/css" href="<@hangar.url "css/chunk-vendors.css" />">
        <link rel="stylesheet" type="text/css" href="<@hangar.url "css/main.css" />"/>

        ${additionalStyling}

        <#-- See https://goo.gl/OOhYW5 and https://goo.gl/qRE0vM -->
        <link rel="manifest" href="<@hangar.url "manifest/manifest.json" />">
        <meta name="theme-color" content="#2980B9">
    </head>
    <body class="site">
    <#if showHeader>
        <#import "header.ftlh" as header>
        <@header.header authEnabled=authEnabled />
    </#if>

    <div tabindex="0" aria-label="Loading" class="loader active" aria-busy="true" id="page-loader">
        <div class="loader-background"></div>
        <div class="loader-icon">
            <svg viewBox="0 0 38 38" xmlns="http://www.w3.org/2000/svg">
                <g fill="none" fill-rule="evenodd">
                    <g transform="translate(1 1)" stroke-width="2">
                        <circle stroke-opacity=".25" cx="18" cy="18" r="18"></circle>
                        <path d="M36 18c0-9.94-8.06-18-18-18" transform="rotate(111.343 18 18)"></path>
                    </g>
                </g>
            </svg>
        </div>
    </div>

    <div class="site-content">
        <#if noContainer>
            <#nested>
        <#else>
            <div class="container<#if showHeader> site-header-margin</#if>">
                <#if config.staging>
                    <div class="row">
                        <div class="col-12">
                            <div class="staging-warning"><@spring.message "general.stagingWarning" /></div>
                        </div>
                    </div>
                </#if>
                <div class="row">
                    <div class="col-12">
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
        <script nonce="${nonce}">
            window.ROUTES = ${mapper.valueToTree(Routes.getJsRoutes())};
            window.ROUTES.parse = function (key, ...params) {
                var route = window.ROUTES[key];
                for (let param of params) {
                    route = route.replace(/{.+?}/, param);
                }
                return route;
            };
        </script>
        <#if _csrf?? && _csrf.token??>
            <script nonce="${nonce}">
                window.csrf = '${_csrf.token}';
                window.csrfInfo = {
                    'parameterName': '${_csrf.parameterName}',
                    'headerName': '${_csrf.headerName}',
                    'token': '${_csrf.token}'
                };
                window.ajaxSettings = {"headers": { '${_csrf.headerName}': window.csrf}};
                window.isLoggedIn = ${headerData.hasUser()?c};
            </script>
        </#if>

        <script type="text/javascript" src="${Routes.JAVA_SCRIPT_ROUTES.getRouteUrl()}"></script>
        <script type="text/javascript" src="<@hangar.url "js/chunk-vendors.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "js/chunk-common.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "js/font-awesome.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "js/bootstrap.js" />"></script>
        <script type="text/javascript" src="<@hangar.url "js/main.js" />"></script>
        ${additionalScripts}
    </#if>

    </body>
    </html>
</#macro>
</@compress>
