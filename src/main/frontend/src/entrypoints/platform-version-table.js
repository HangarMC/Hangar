import Vue from "vue";
import axios from "axios";

import PlatformVersionTable from "../PlatformVersionTable.vue";

axios.defaults.headers.post[window.csrfInfo.headerName] = window.csrfInfo.token;

new Vue({
  el: "#platform-version-table",
  render: createElement =>
    createElement(PlatformVersionTable, {
      props: {
        platforms: window.PLATFORMS
      }
    })
});
