import { createApp } from 'vue';
import Swagger from '@/components/Swagger';

require('swagger-ui');

createApp(Swagger).mount('#swagger-ui-vue');
