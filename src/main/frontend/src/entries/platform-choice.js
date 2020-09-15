import Vue from 'vue'

const root = require('../PlatformChoice.vue').default;
const app = new Vue({
    el: '#platform-choice',
    render: createElement => createElement(root)
});