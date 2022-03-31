import { createApp } from 'vue';
import $ from 'jquery';
import UserProfile from '@/components/entrypoints/users/UserProfile';

$.ajaxSetup(window.ajaxSettings);

createApp(UserProfile).mount('#user-profile');
