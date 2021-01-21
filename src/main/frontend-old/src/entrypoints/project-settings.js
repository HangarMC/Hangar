import { createApp } from 'vue';
import ProjectSettings from '@/components/entrypoints/projects/ProjectSettings';
import { setupI18n } from '@/plugins/i18n';

const i18n = setupI18n();
createApp(ProjectSettings).use(i18n).mount('#project-settings');
