import { createApp } from 'vue';
import ProjectControls from '@/ProjectControls';
import { setupI18n } from '@/plugins/i18n';

const mountPoint = document.getElementById('project-controls');
if (mountPoint) {
    const i18n = setupI18n();
    createApp(ProjectControls, {
        visibility: mountPoint.dataset.visibility,
        hasUser: mountPoint.dataset.hasUser === 'true',
        isOwner: mountPoint.dataset.isOwner === 'true',
        isStarred: mountPoint.dataset.isStarred === 'true',
        isWatching: mountPoint.dataset.isWatching === 'true',
        hasUserFlags: mountPoint.dataset.hasUserFlags === 'true',
        starCount: parseInt(mountPoint.dataset.starCount),
        flagCount: parseInt(mountPoint.dataset.flagCount),
        noteCount: parseInt(mountPoint.dataset.noteCount),
        hasModNotes: mountPoint.dataset.hasModNotes === 'true',
        hasViewLogs: mountPoint.dataset.hasViewLogs === 'true',
    })
        .use(i18n)
        .mount('#project-controls');
}
