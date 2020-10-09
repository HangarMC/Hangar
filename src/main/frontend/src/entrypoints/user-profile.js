import { createApp } from 'vue';
import $ from 'jquery';
import UserProfile from '@/components/entrypoints/UserProfile';

$.ajaxSetup(window.ajaxSettings);

createApp(UserProfile).mount('#user-profile');
