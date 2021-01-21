<template>
    <div id="swagger-ui"></div>
</template>
<script>
import SwaggerUIBundle from 'swagger-ui';
import { API } from '@/api';

export default {
    name: 'SwaggerUI',
    mounted() {
        window.onload = () => {
            window.ui = SwaggerUIBundle({
                url: '/v2/api-docs',
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [SwaggerUIBundle.presets.apis, SwaggerUIBundle.SwaggerUIStandalonePreset],
                plugins: [SwaggerUIBundle.plugins.DownloadUrl],
                layout: 'BaseLayout',
                requestInterceptor: (req) => {
                    if (!req.loadSpec) {
                        const promise = API.getSession().then((session) => {
                            req.headers.authorization = 'HangarApi session="' + session + '"';
                            return req;
                        });
                        // Workaround for fixing the curl URL
                        // https://github.com/swagger-api/swagger-ui/issues/4778#issuecomment-456403631
                        promise.url = req.url;
                        return promise;
                    } else {
                        return req;
                    }
                },
            });
        };
    },
};
</script>
<style lang="scss">
@use '~swagger-ui/dist/swagger-ui';

html {
    box-sizing: border-box;
    overflow: -moz-scrollbars-vertical;
    overflow-y: scroll;
}

*,
*:before,
*:after {
    box-sizing: inherit;
}

body {
    margin: 0;
    background: #fafafa;
}

.swagger-ui .topbar .download-url-wrapper,
.swagger-ui .info hgroup.main a {
    display: none;
}

.swagger-ui .info {
    margin: 2rem 0;
}

.swagger-ui .info .title small pre {
    background-color: unset;
    border: unset;
}

.model-container,
.responses-inner {
    overflow-x: auto;
}

.swagger-ui .info .description h2 {
    padding-top: 1.5rem;
    margin: 1.5rem 0 0;
    border-top: 3px solid #333333;
}

.swagger-ui .scheme-container {
    border-top: 1px solid rgba(0, 0, 0, 0.15);
}
</style>
