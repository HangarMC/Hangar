<template>
    <Editor
        :save-call="ROUTES.parse('VERSIONS_SAVE_DESCRIPTION', project.ownerName, project.slug, version.versionStringUrl)"
        :enabled="canEditPages"
        subject="Version"
        :raw="version.description || ''"
    ></Editor>
</template>
<script>
import Editor from '@/components/Editor';

export default {
    name: 'VersionView',
    components: {
        Editor,
    },
    data() {
        return {
            ROUTES: window.ROUTES,
            project: window.PROJECT,
            version: window.VERSION,
            canEditPages: window.CAN_EDIT_PAGES,
        };
    },
};
</script>
