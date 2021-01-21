import { createApp } from 'vue';
import UserAdmin from '@/components/entrypoints/users/UserAdmin';
import { setupI18n } from '@/plugins/i18n';

const i18n = setupI18n();
createApp(UserAdmin).use(i18n).mount('#user-admin');
