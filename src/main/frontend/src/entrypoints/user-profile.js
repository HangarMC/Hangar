import Vue from "vue";

const root = require("../UserProfile.vue").default;
new Vue({
  el: "#user-profile",
  render: createElement => createElement(root)
});
