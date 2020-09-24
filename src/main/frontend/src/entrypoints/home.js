import {createApp} from "vue";
import $ from "jquery";
import Home from "../Home"

$.ajaxSetup(window.ajaxSettings);

createApp(Home).mount("#home")
