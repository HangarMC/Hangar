import LRUCache from 'lru-cache';
// @ts-ignore
import minifyTheme from 'minify-css-string';
import { ThemeOptions, VuetifyThemeCache, VuetifyThemeVariant } from 'vuetify/types/services/theme';

const themeCache = new LRUCache({
    max: 10,
    maxAge: 1000 * 60 * 60, // 1 hour
}) as VuetifyThemeCache;

export default function buildTheme(darkTheme: boolean, dark: Partial<VuetifyThemeVariant>, light: Partial<VuetifyThemeVariant>): ThemeOptions {
    return {
        default: darkTheme ? 'dark' : 'light',
        dark: darkTheme,
        disable: false,
        options: {
            customProperties: true,
            themeCache,
            minifyTheme,
        },
        themes: {
            dark,
            light,
        },
    };
}
