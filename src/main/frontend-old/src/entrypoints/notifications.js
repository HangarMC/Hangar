import { createApp } from 'vue';
import Notifications from '@/components/entrypoints/users/Notifications';
import { setupI18n } from '@/plugins/i18n';

const i18n = setupI18n();
createApp(Notifications).use(i18n).mount('#notifications');
