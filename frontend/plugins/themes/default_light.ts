import colors from 'vuetify/es5/util/colors';
import { VuetifyThemeVariant } from 'vuetify/types/services/theme';

export default {
    anchor: colors.blue.base,
    primary: colors.blue.darken2,
    accent: '#ffffff',
    secondary: colors.amber.darken3,
    info: '#af00ff',
    warning: colors.amber.base,
    error: colors.deepOrange.accent4,
    success: colors.green.accent3,
} as Partial<VuetifyThemeVariant>;
