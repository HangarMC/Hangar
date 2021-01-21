import { createApp } from 'vue';
import ProjectView from '@/components/entrypoints/projects/ProjectView';
import { setupI18n } from '@/plugins/i18n';
import DonationResult from '@/components/donation/DonationResult';

const i18n = setupI18n();
createApp(ProjectView).use(i18n).mount('#project-view');

createApp(DonationResult).use(i18n).mount('#donation-result');
