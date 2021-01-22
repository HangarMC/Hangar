import Vue from 'vue';
import { config, dom, library } from '@fortawesome/fontawesome-svg-core';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import {
    faAsterisk,
    faComment,
    faGamepad,
    faGlobe,
    faLock,
    faMagic,
    faMoneyBillAlt,
    faPuzzlePiece,
    faServer,
    faWrench,
} from '@fortawesome/free-solid-svg-icons';

Vue.component('FontAwesomeIcon', FontAwesomeIcon);

config.autoAddCss = false;

library.add(faServer, faComment, faWrench, faMoneyBillAlt, faPuzzlePiece, faGamepad, faLock, faMagic, faGlobe, faAsterisk);

dom.watch();
