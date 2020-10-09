import { createApp } from 'vue';
// import OrganizationMemberList from '@/components/entrypoints/organizations/OrganizationMemberList';
import ProjectManager from '@/components/entrypoints/organizations/ProjectManager';
import { setupI18n } from '@/plugins/i18n';

const i18n = setupI18n();
const managerEl = document.getElementById('org-project-manager');
if (managerEl) {
    createApp(ProjectManager).use(i18n).mount(managerEl);
}

// createApp(OrganizationMemberList).use(i18n).mount('#org-member-list');
