import { createApp } from 'vue';
import $ from 'jquery';
import Home from '../components/entrypoints/Home';

$.ajaxSetup(window.ajaxSettings);

createApp(Home).mount('#home');
