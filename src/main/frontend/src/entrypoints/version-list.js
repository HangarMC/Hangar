import Vue from "vue";

const root = require("../VersionList.vue").default;
new Vue({
  el: "#version-list",
  render: createElement => createElement(root)
});
