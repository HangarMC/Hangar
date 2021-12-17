import { NuxtConfig } from '@nuxt/types';
import colors from 'vuetify/lib/util/colors';

import de from './locales/de';
import en from './locales/en';
import es from './locales/es';
import fr from './locales/fr';
import it from './locales/it';
import nl from './locales/nl';
import tr from './locales/tr';
import zhHans from './locales/zh_hans';
import zhHant from './locales/zh_hant';
import dum from './locales/dum';

require('events').EventEmitter.defaultMaxListeners = 20;
require('dotenv').config();

const proxyHost = process.env.proxyHost || 'http://localhost:8080';
const oauthHost = process.env.oauthHost || 'http://localhost:4444';
const authHost = process.env.authHost || 'http://localhost:3001';
const publicHost = process.env.PUBLIC_HOST || 'http://localhost:3000';
const host = process.env.host || 'localhost';
const nodeEnv = process.env.NODE_ENV;
const publicPath = process.env.PUBLIC_PATH || '/_nuxt/';

// noinspection JSUnusedGlobalSymbols
export default {
    telemetry: false,
    modern: 'server',

    // PWA module configuration: https://go.nuxtjs.dev/pwa
    pwa: {
        manifest: {
            name: 'Hangar | PaperMC',
            short_name: 'Hangar',
            description: 'Plugin repository for Paper plugins and more!',
            lang: 'en',
        },
        workbox: {
            enabled: false,
        },
    },

    // Global page headers: https://go.nuxtjs.dev/config-head
    head: {
        htmlAttrs: {
            dir: 'ltr',
        },
        titleTemplate: (titleChunk) => (titleChunk ? `${titleChunk} | Hangar` : 'Hangar'),
        meta: [
            { hid: 'charset', charset: 'utf-8' },
            { name: 'viewport', content: 'width=device-width, initial-scale=1' },
            { property: 'twitter:site', name: 'twitter:site', hid: 'twitter:site', content: 'hangar' },
            { property: 'twitter:card', name: 'twitter:card', hid: 'twitter:card', content: 'summary' },
        ],
        noscript: [{ innerHTML: 'We are sorry, but Hangar requires JavaScript to work properly.' }],
    },

    env: {
        proxyHost,
        oauthHost,
        authHost,
        publicHost,
        host,
        nodeEnv,
    },

    // Global CSS: https://go.nuxtjs.dev/config-css
    css: ['~/assets/main.scss'],

    // Plugins to run before rendering page: https://go.nuxtjs.dev/config-plugins
    plugins: ['~/plugins/api.ts', '~/plugins/utils.ts', '~/plugins/auth.ts', '~/plugins/perms.ts', '~/plugins/seo.ts'],

    // Auto import components: https://go.nuxtjs.dev/config-components
    components: false,

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
        '@dansmaculotte/nuxt-security',
    ],

    // Axios module configuration: https://go.nuxtjs.dev/config-axios
    axios: {},

    // Vuetify module configuration: https://go.nuxtjs.dev/config-vuetify
    vuetify: {
        customVariables: ['~/assets/variables.scss'],
        optionsPath: '~/plugins/vuetify.ts',
        treeShake: true,
    },

    // Build Configuration: https://go.nuxtjs.dev/config-build
    build: {
        publicPath,
        parallel: true,
        cache: true,
        babel: {
            presets({ envName }) {
                const envTargets = {
                    client: { browsers: ['last 2 versions'], ie: 11 },
                    server: { node: 'current' },
                    modern: { esmodules: true },
                };
                return [
                    [
                        '@nuxt/babel-preset-app',
                        {
                            targets: envTargets[envName],
                            corejs: 3,
                        },
                    ],
                ];
            },
        },
    },

    router: {
        middleware: ['routePermissions'],
    },

    proxy: [
        // backend
        proxyHost + '/api/',
        proxyHost + '/signup',
        proxyHost + '/login',
        proxyHost + '/logout',
        proxyHost + '/handle-logout',
        proxyHost + '/refresh',
        proxyHost + '/invalidate',
        proxyHost + '/v2/api-docs/',
        proxyHost + '/robots.txt',
        proxyHost + '/sitemap.xml',
        proxyHost + '/global-sitemap.xml',
        proxyHost + '/*/sitemap.xml',
        proxyHost + '/statusz',
        // auth
        authHost + '/avatar',
        authHost + '/oauth/logout',
        oauthHost + '/oauth2',
    ],

    i18n: {
        vueI18nLoader: true,
        strategy: 'no_prefix',
        defaultLocale: 'en',
        locales: [
            { code: 'de', iso: 'de-DE', name: 'Deutsch', icon: 'Test' },
            { code: 'fr', iso: 'fr-FR', name: 'Français', icon: 'Test' },
            { code: 'en', iso: 'en-US', name: 'English', icon: 'Test' },
            { code: 'es', iso: 'es-ES', name: 'Español', icon: 'Test' },
            { code: 'it', iso: 'it-IT', name: 'Italiano', icon: 'Test' },
            { code: 'nl', iso: 'nl-NL', name: 'Nederlands', icon: 'Test' },
            { code: 'tr', iso: 'tr-TR', name: 'Türkçe', icon: 'Test' },
            { code: 'zhS', iso: 'zh-HANS', name: 'Simplified Chinese', icon: 'Test' },
            { code: 'zhT', iso: 'zh-HANT', name: 'Traditional Chinese', icon: 'Test' },
            { code: 'dum', iso: 'dum', name: 'Crowdin Dummy', icon: 'Test' },
        ],
        vueI18n: {
            locale: 'en',
            fallbackLocale: 'en',
            messages: {
                de,
                en,
                fr,
                es,
                it,
                nl,
                tr,
                zh_hans: zhHans,
                zh_hant: zhHant,
                dum,
            },
        },
        detectBrowserLanguage: {
            useCookie: true,
            cookieKey: 'i18n_redirected',
            onlyOnRoot: true,
        },
    },

    security: {
        dev: true,
        hsts: {
            maxAge: 15552000,
            includeSubDomains: true,
            preload: true,
        },
        csp: {
            directives: {
                defaultSrc: [
                    "'self'",
                    'https://google-analytics.com',
                    'https://fonts.gstatic.com',
                    'https://fonts.googleapis.com',
                    'https://static.cloudflareinsights.com',
                ],
                styleSrc: ["'self'", 'https://fonts.googleapis.com', 'cdn.jsdelivr.net', "'unsafe-inline'", 'https://cdn.crowdin.com'],
                fontSrc: ['fonts.gstatic.com', 'cdn.jsdelivr.net'],
                scriptSrc: [
                    "'self'" /* , "'nonce-{nonce}'" */,
                    "'unsafe-inline'",
                    "'unsafe-eval'",
                    'https://static.cloudflareinsights.com',
                    'https://cdn.crowdin.com',
                ],
                imgSrc: [
                    "'self'",
                    'https://www.google-analytics.com',
                    'https://www.gravatar.com',
                    authHost,
                    'data: papermc.io paper.readthedocs.io',
                    'https:', // ppl can use images in descriptions, we would need an image proxy or smth
                ],
                frameSrc: ["'self'", 'http://localhost/', 'https://papermc.io/', 'https://hangar.crowdin.com', 'https://www.youtube-nocookie.com'],
                manifestSrc: ["'self'"],
                connectSrc: [
                    "'self'",
                    'https://www.google-analytics.com',
                    'https://stats.g.doubleclick.net',
                    'https://hangar.crowdin.com',
                    'http://localhost:3001',
                    'https://hangar-auth.benndorf.dev',
                ],
                mediaSrc: ["'self'"],
                objectSrc: ["'none'"],
                baseUri: ["'none'"],
            },
            reportOnly: false,
        },
        referrer: 'same-origin',
        additionalHeaders: true,
    },

    server: {
        port: 3000,
        host,
    },

    loading: {
        color: colors.blue.lighten2,
        continuous: true,
    },

    publicRuntimeConfig: {
        axios: {
            browserBaseURL: publicHost,
        },
    },

    privateRuntimeConfig: {
        axios: {
            baseURL: proxyHost,
        },
    },
} as NuxtConfig;
