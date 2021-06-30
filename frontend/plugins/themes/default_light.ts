import colors from 'vuetify/es5/util/colors';
import { VuetifyThemeVariant } from 'vuetify/types/services/theme';

export default {
    anchor: colors.blue.lighten3,
    primary: colors.blue.darken2,
    accent: colors.grey.darken3,
    secondary: colors.amber.darken3,
    info: colors.teal.lighten1,
    warning: colors.amber.base,
    error: colors.deepOrange.accent4,
    success: colors.green.accent3,
} as Partial<VuetifyThemeVariant>;
