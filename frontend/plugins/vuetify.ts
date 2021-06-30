import { VuetifyPreset } from 'vuetify';
import { Paper, Velocity, Waterfall } from '~/components/logos';
import buildTheme from '~/plugins/themes/base';
import defaultDark from '~/plugins/themes/default_dark';
import defaultLight from '~/plugins/themes/default_light';

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
    theme: buildTheme(true, defaultDark, defaultLight),
} as Partial<VuetifyPreset>;
