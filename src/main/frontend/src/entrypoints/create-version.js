import { createApp } from 'vue';
import CreateVersion from '@/CreateVersion';

createApp(CreateVersion, {
    defaultColor: window.DEFAULT_COLOR,
    pendingVersion: window.PENDING_VERSION,
    ownerName: window.OWNER_NAME,
    projectSlug: window.PROJECT_SLUG,
    channels: window.CHANNELS,
    forumSync: window.FORUM_SYNC,
}).mount('#create-version');
