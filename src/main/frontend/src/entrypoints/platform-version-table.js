import { createApp } from 'vue';
import axios from 'axios';
import PlatformVersionTable from '@/PlatformVersionTable';

axios.defaults.headers.post[window.csrfInfo.headerName] = window.csrfInfo.token;

createApp(PlatformVersionTable, {
  platforms: window.PLATFORMS
}).mount('#platform-version-table');
