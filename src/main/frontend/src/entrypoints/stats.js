import { createApp } from 'vue';
import Stats from '@/Stats';
import { setupI18n } from '@/plugins/i18n';

const i18n = setupI18n();
createApp(Stats).use(i18n).mount('#stats');
