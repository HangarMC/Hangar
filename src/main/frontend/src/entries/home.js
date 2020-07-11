import Vue from 'vue'

const root = require('../Home.vue').default;
const app = new Vue({
    el: '#home',
    render: createElement => createElement(root),
});
