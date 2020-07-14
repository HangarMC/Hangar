<#--
Mostly copied from swagger-ui's index file
-->

<#import "/spring.ftl" as spring />
<#import "*/utils/hangar.ftlh" as hangar />
<#import "*/layout/base.ftlh" as base />

<#macro scripts>
    <script type="text/javascript" src="<@hangar.url "lib/jquery/dist/jquery.min.js" />"></script>
    <script src="<@hangar.url "lib/swagger-ui/swagger-ui-bundle.js" />"></script>
    <script src="<@hangar.url "lib/swagger-ui/swagger-ui-standalone-preset.js" />"></script>
    <script src="<@hangar.url "javascripts/apiRequests.js" />"></script>
    <script @CSPNonce.attr>
        window.onload = () => {
            window.ui = SwaggerUIBundle({
                url: "/v2/api-docs",
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "BaseLayout",
                requestInterceptor: (req) => {
                    if (!req.loadSpec) {
                        const promise = getApiSession().then((session) => {
                            req.headers.authorization = 'HangarApi session=' + session;
                            return req;
                        });
                        // Workaround for fixing the curl URL
                        // https://github.com/swagger-api/swagger-ui/issues/4778#issuecomment-456403631
                        promise.url = req.url;
                        return promise;
                    } else {
                        return req;
                    }
                }
            })
        }
    </script>
    <#if request??>
        <script @CSPNonce.attr>
            csrf = '@play.filters.csrf.CSRF.getToken.get.value';
            isLoggedIn = ${request.hasUser};
        </script>
    </#if>
</#macro>

<#macro stylesheets>
    <link rel="stylesheet" type="text/css" href="<@hangar.url "lib/swagger-ui/swagger-ui.css" />">

    <style>
        html {
            box-sizing: border-box;
            overflow: -moz-scrollbars-vertical;
            overflow-y: scroll;
        }
        *, *:before, *:after {
            box-sizing: inherit;
        }
        body {
            margin:0;
            background: #fafafa;
        }

        .swagger-ui .topbar .download-url-wrapper, .swagger-ui .info hgroup.main a {
            display: none;
        }
        .swagger-ui .info {
            margin: 2rem 0;
        }
        .swagger-ui .info .title small pre {
            background-color: unset;
            border: unset;
        }
        .model-container, .responses-inner {
            overflow-x: auto;
        }
        .swagger-ui .info .description h2 {
            padding-top: 1.5rem;
            margin: 1.5rem 0 0;
            border-top: 3px solid #333333;
        }
        .swagger-ui .scheme-container {
            border-top: 1px solid rgba(0,0,0,.15);
        }
    </style>
</#macro>

<#assign message><@spring.message "general.openapi" /></#assign>
<#assign scriptsVar><@scripts /></#assign>
<#assign stylesheetsVar><@stylesheets /></#assign>
<@base.base title="${message}" additionalScripts=scriptsVar additionalStyling=stylesheetsVar>
    <div id="swagger-ui"></div>
</@base.base>
