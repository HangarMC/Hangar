import colors from 'vuetify/es5/util/colors';
import { VuetifyThemeVariant } from 'vuetify/types/services/theme';

export default {
    anchor: colors.blue.base,
    primary: '#0080ff',
    accent: '#ffffff',
    secondary: colors.amber.darken3,
    info: '#004ee9',
    warning: colors.amber.base,
    error: colors.deepOrange.accent4,
    success: colors.green.accent3,
} as Partial<VuetifyThemeVariant>;
