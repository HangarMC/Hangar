import { VuetifyPreset } from 'vuetify';
import { Paper, Velocity, Waterfall } from '~/components/logos';
import buildTheme from '~/plugins/themes/base';
import defaultDark from '~/plugins/themes/default_dark';
import defaultLight from '~/plugins/themes/default_light';

let defaultDarkMode = false;

if (process.browser) {
    if (localStorage.getItem('DarkMode') === 'true') {
        defaultDarkMode = true;
    } else if (!localStorage.getItem('DarkMode') && window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        defaultDarkMode = true;
    }
}

export default {
    icons: {
        iconfont: 'mdi',
        values: {
            paper: {
                component: Paper,
            },
            waterfall: {
                component: Waterfall,
            },
            velocity: {
                component: Velocity,
            },
        },
    },
    theme: buildTheme(defaultDarkMode, defaultDark, defaultLight),
} as Partial<VuetifyPreset>;
