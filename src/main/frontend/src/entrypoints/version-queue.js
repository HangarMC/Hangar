import { createApp } from 'vue';
import { setupI18n } from '@/plugins/i18n';
import VersionQueue from '@/components/entrypoints/admin/VersionQueue';

const i18n = setupI18n();
createApp(VersionQueue).use(i18n).mount('#version-queue');
