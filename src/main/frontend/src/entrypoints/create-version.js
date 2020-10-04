import { createApp } from 'vue';
import CreateVersion from '@/CreateVersion';

createApp(CreateVersion, {
    pendingVersion: window.PENDING_VERSION,
    ownerName: window.OWNER_NAME,
    projectSlug: window.PROJECT_SLUG,
    forumSync: window.FORUM_SYNC,
}).mount('#create-version');
