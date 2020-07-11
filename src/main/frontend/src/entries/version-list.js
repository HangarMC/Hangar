import Vue from 'vue'

const root = require('../VersionList.vue').default;
const app = new Vue({
    el: '#version-list',
    render: createElement => createElement(root),
});
