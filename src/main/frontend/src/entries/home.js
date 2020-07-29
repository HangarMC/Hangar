import Vue from 'vue'
import $ from "jquery";

$.ajaxSetup(window.ajaxSettings);

const root = require('../Home.vue').default;
const app = new Vue({
    el: '#home',
    render: createElement => createElement(root),
});
