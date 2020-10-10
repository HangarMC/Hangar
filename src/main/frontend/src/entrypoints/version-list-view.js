import { createApp } from 'vue';
import VersionList from '@/components/entrypoints/versions/VersionListView';
import { setupI18n } from '@/plugins/i18n';

const i18n = setupI18n();
createApp(VersionList).use(i18n).mount('#version-list-view');
