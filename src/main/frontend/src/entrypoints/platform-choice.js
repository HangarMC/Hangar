import Vue from "vue";

const root = require("../PlatformChoice.vue").default;
new Vue({
  el: "#platform-choice",
  render: createElement => createElement(root)
});
