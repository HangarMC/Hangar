import { createApp } from 'vue';
import $ from 'jquery';
import VersionList from '@/components/entrypoints/VersionList';

$.ajaxSetup(window.ajaxSettings);

createApp(VersionList).mount('#version-list');
