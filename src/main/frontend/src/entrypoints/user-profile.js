import { createApp } from 'vue';
import $ from 'jquery';
import UserProfile from '@/UserProfile';

$.ajaxSetup(window.ajaxSettings);

createApp(UserProfile).mount('#user-profile');
