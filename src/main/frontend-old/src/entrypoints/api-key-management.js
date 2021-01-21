import { createApp } from 'vue';
import ApiKeyManagement from '@/components/entrypoints/users/ApiKeyManagement';
import { setupI18n } from '@/plugins/i18n';

const i18n = setupI18n();
createApp(ApiKeyManagement).use(i18n).mount('#api-key-management');
