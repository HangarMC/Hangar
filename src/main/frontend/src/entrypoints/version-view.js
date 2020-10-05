import { createApp } from 'vue';
import { setupI18n } from '@/plugins/i18n';
import VersionView from '@/VersionView';

const i18n = setupI18n();
createApp(VersionView).use(i18n).mount('#version-view');
