import { createApp } from 'vue';
import axios from 'axios';
import PlatformVersionTable from '@/components/entrypoints/PlatformVersionTable';

axios.defaults.headers.post[window.csrfInfo.headerName] = window.csrfInfo.token;

createApp(PlatformVersionTable, {
    platforms: window.PLATFORMS,
}).mount('#platform-version-table');
