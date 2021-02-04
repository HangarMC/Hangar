<template>
    <v-card color="grey lighten-2">
        <div id="swagger-ui"></div>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import SwaggerUIBundle from 'swagger-ui';
import { SwaggerConfigs, SwaggerUIBundle as SwaggerUIBundleType } from 'swagger-ui-dist';

declare global {
    interface Window {
        ui: SwaggerUIBundleType;
    }
}

@Component
export default class ApiPage extends Vue {
    mounted() {
        window.ui = SwaggerUIBundle({
            url: '/v2/api-docs/',
            dom_id: '#swagger-ui',
            deepLinking: true,
            presets: [SwaggerUIBundle.presets.apis, SwaggerUIBundle.SwaggerUIStandalonePreset],
            plugins: [SwaggerUIBundle.plugins.DownloadUrl],
            layout: 'BaseLayout',
            requestInterceptor: (req) => {
                if (!req.loadSpec) {
                    // TODO update this once api auth is in
                    // const promise = this.$api.getSession().then((session) => {
                    //     req.headers.authorization = 'HangarApi session="' + session + '"';
                    //     return req;
                    // });
                    // Workaround for fixing the curl URL
                    // https://github.com/swagger-api/swagger-ui/issues/4778#issuecomment-456403631
                    // @ts-ignore
                    // promise.url = req.url;
                    // return promise;
                } else {
                    return req;
                }
            },
        } as SwaggerConfigs) as SwaggerUIBundleType;
    }
}
</script>

<style lang="scss">
@import '@/node_modules/swagger-ui/dist/swagger-ui.css';

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

.swagger-ui .info {
    background-color: unset !important;
    border-color: unset !important;
}
</style>
