import { createApp } from 'vue';
import CreateVersion from '@/CreateVersion';
import { setupI18n } from '@/plugins/i18n';

const i18n = setupI18n();
createApp(CreateVersion, {
    pendingVersion: window.PENDING_VERSION,
    ownerName: window.OWNER_NAME,
    projectSlug: window.PROJECT_SLUG,
    forumSync: window.FORUM_SYNC,
})
    .use(i18n)
    .mount('#create-version');
