import colors from 'vuetify/es5/util/colors';
import { VuetifyThemeVariant } from 'vuetify/types/services/theme';

export default {
    anchor: colors.blue.lighten3,
    primary: colors.lightBlue.darken1,
    background: '#111827',
    box: '#212121',
    accent: '#272727',
    secondary: colors.amber.darken3,
    info: '#af00ff',
    warning: colors.orange.darken3,
    error: colors.deepOrange.accent4,
    success: colors.lightGreen.darken2,
} as Partial<VuetifyThemeVariant>;
