import { createApp } from 'vue';
import CreateVersion from '@/CreateVersion';

createApp(CreateVersion, {
    pendingVersion: window.PENDING_VERSION,
    versionUploadRoute: window.VERSION_UPLOAD_ROUTE,
    channels: window.CHANNELS,
}).mount('#create-version');
