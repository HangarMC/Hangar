import { library, dom, config } from '@fortawesome/fontawesome-svg-core'
import {
    faStar as fasStar, faEye, faDownload, faServer, faComment, faWrench, faMoneyBillAlt, faPuzzlePiece, faGamepad, faLock,
    faMagic, faGlobe, faAsterisk, faChevronUp, faChevronDown, faHome, faComments, faCode, faBook, faGraduationCap, faPlus,
    faUsers, faUserTie, faUser, faBell, faFlag, faThumbsUp as fasThumbsUp, faChartArea, faHeartbeat, faList, faSignOutAlt,
    faTrash, faPlay, faInfoCircle, faQuestionCircle, faExclamationCircle, faSpinner, faCircle, faArrowRight, faCheck,
    faReply, faSave, faTimes, faPencilAlt, faArrowLeft, faCog, faPlayCircle, faEdit, faKey, faCalendar, faUpload,
    faPaperPlane, faSearch, faExternalLinkAlt, faBug, faTerminal, faStopCircle, faClipboard, faWindowClose, faUnlockAlt,
    faGem as fasGem, faLink, faInfo, faCheckCircle as fasCheckCircle, faTimesCircle, faEyeSlash, faUserTag
} from '@fortawesome/free-solid-svg-icons'

import {
    faGem as farGem, faThumbsUp as farThumbsUp, faFile, faStar as farStar, faPlusSquare, faMinusSquare, faFileArchive,
    faSadTear, faCheckCircle as farCheckCircle, faClock
} from '@fortawesome/free-regular-svg-icons'

config.autoAddCss = false;

library.add(fasStar, fasGem, faEye, faDownload, faServer, faComment, faWrench, faMoneyBillAlt, faPuzzlePiece, faGamepad,
    faLock, faMagic, faGlobe, faAsterisk, faChevronUp, faChevronDown, faHome, faComments, faCode, faBook, faGraduationCap,
    faPlus, faUsers, faUserTie, faUser, faBell, faFlag, fasThumbsUp, faChartArea, faHeartbeat, faList, faSignOutAlt,
    farThumbsUp, faTrash, faPlay, faInfoCircle, faQuestionCircle, faExclamationCircle, faSpinner, faCircle, faArrowRight,
    faCheck, faReply, faSave, faTimes, faPencilAlt, faArrowLeft, faCog, faPlayCircle, faEdit, faKey, faCalendar, faFile,
    faUpload, faPaperPlane, faPlusSquare, faSearch, farStar, faExternalLinkAlt, faMinusSquare, faBug, faFileArchive,
    faTerminal, faStopCircle, faClipboard, faWindowClose, faSadTear, faUnlockAlt, farGem, faLink, farCheckCircle, faClock,
    faInfo, fasCheckCircle, faTimesCircle, faEyeSlash, faUserTag);

dom.watch();
