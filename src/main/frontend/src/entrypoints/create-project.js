import { createApp } from 'vue';
import CreateProject from '@/components/entrypoints/CreateProject';
import { setupI18n } from '@/plugins/i18n';

const i18n = setupI18n();
createApp(CreateProject).use(i18n).mount('#create-project');
