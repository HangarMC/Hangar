import Vue from "vue";
import $ from "jquery";

$.ajaxSetup(window.ajaxSettings);

const root = require("../Home.vue").default;
new Vue({
  el: "#home",
  render: createElement => createElement(root)
});
