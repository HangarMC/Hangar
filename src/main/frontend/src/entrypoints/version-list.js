import { createApp } from 'vue';
import $ from 'jquery';
import VersionList from '@/VersionList';

$.ajaxSetup(window.ajaxSettings);

createApp(VersionList).mount('#version-list');
