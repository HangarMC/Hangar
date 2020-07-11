import Vue from 'vue'

const root = require('../UserProfile.vue').default;
const app = new Vue({
    el: '#user-profile',
    render: createElement => createElement(root),
});
