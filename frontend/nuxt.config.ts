// @ts-ignore
import colors from 'vuetify/es5/util/colors';
import { NuxtConfig } from '@nuxt/types';

import en from './locales/en';
import fr from './locales/fr';

require('dotenv').config();

const proxyHost = process.env.proxyHost || 'http://localhost:8080';
const authHost = process.env.authHost || 'http://localhost:8000';
const host = process.env.host || 'localhost';

export default {
    telemetry: false,
    // Global page headers: https://go.nuxtjs.dev/config-head
    head: {
        titleTemplate: (titleChunk: string) => {
            return titleChunk ? `${titleChunk} | Hangar` : 'Hangar';
        },
        meta: [
            { charset: 'utf-8' },
            { name: 'viewport', content: 'width=device-width, initial-scale=1' },
            { hid: 'description', name: 'description', content: '' },
        ],
    },

    // Global CSS: https://go.nuxtjs.dev/config-css
    css: ['~/assets/main.scss'],

    // Plugins to run before rendering page: https://go.nuxtjs.dev/config-plugins
    plugins: ['~/plugins/api.ts', '~/plugins/utils.ts', '~/plugins/auth.ts'],

    // Auto import components: https://go.nuxtjs.dev/config-components
    components: false, // Can change this back if you really want, but it doesn't look like Webstorm or Intellij understand what's going on if they aren't imported. Also, does it really matter? It's just a few imports

    // Modules for dev and build (recommended): https://go.nuxtjs.dev/config-modules
    buildModules: [
        // https://go.nuxtjs.dev/typescript
        '@nuxt/typescript-build',
        // https://go.nuxtjs.dev/vuetify
        '@nuxtjs/vuetify',
        // https://go.nuxtjs.dev/eslint
        '@nuxtjs/eslint-module',
        // https://go.nuxtjs.dev/pwa
        '@nuxtjs/pwa',
        '@nuxtjs/dotenv',
    ],

    // Modules: https://go.nuxtjs.dev/config-modules
    modules: [
        // https://go.nuxtjs.dev/axios
        '@nuxtjs/axios',
        'cookie-universal-nuxt',
        '@nuxtjs/proxy',
        'nuxt-i18n',
    ],

    // Axios module configuration: https://go.nuxtjs.dev/config-axios
    axios: {},

    // PWA module configuration: https://go.nuxtjs.dev/pwa
    pwa: {
        manifest: {
            name: 'Hangar | PaperMC',
            short_name: 'Hangar',
            description: 'Plugin repository for Paper plugins and more!',
            lang: 'en',
        },
    },

    // Vuetify module configuration: https://go.nuxtjs.dev/config-vuetify
    vuetify: {
        customVariables: ['~/assets/variables.scss'],
        treeShake: true,
        theme: {
            default: 'dark',
            dark: true,
            disable: false,
            options: {},
            themes: {
                dark: {
                    anchor: colors.blue.lighten3,
                    primary: colors.blue.darken2,
                    accent: colors.grey.darken3,
                    secondary: colors.amber.darken3,
                    info: colors.lightBlue.base,
                    warning: colors.orange.darken3,
                    error: colors.deepOrange.accent4,
                    success: colors.lightGreen.darken2,
                },
                light: {
                    anchor: colors.blue.lighten3,
                    primary: colors.blue.darken2,
                    accent: colors.grey.darken3,
                    secondary: colors.amber.darken3,
                    info: colors.teal.lighten1,
                    warning: colors.amber.base,
                    error: colors.deepOrange.accent4,
                    success: colors.green.accent3,
                },
            },
        },
    },

    // Build Configuration: https://go.nuxtjs.dev/config-build
    build: {
        transpile: ['lodash-es'],
    },

    router: {
        middleware: ['auth', 'routePermissions'],
    },

    proxy: [
        // backend
        proxyHost + '/api/',
        proxyHost + '/signup',
        proxyHost + '/login',
        proxyHost + '/logout',
        proxyHost + '/refresh',
        proxyHost + '/invalidate',
        proxyHost + '/v2/api-docs/',
        // auth
        authHost + '/avatar',
    ],

    i18n: {
        vueI18nLoader: true,
        strategy: 'no_prefix',
        defaultLocale: 'en',
        locales: [
            { code: 'fr', iso: 'fr-FR', name: 'Français' },
            { code: 'en', iso: 'en-US', name: 'English' },
        ],
        vueI18n: {
            locale: 'en',
            fallbackLocale: 'en',
            messages: {
                en,
                fr,
            },
        },
    },

    server: {
        port: 3000,
        host,
    },

    loading: {
        continuous: true,
    },
} as NuxtConfig;
