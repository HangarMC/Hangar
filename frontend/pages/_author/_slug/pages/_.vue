<template>
    <div>
        <v-row class="mt-5">
            <v-col v-if="!$fetchState.pending && page.contents" cols="12">
                <MarkdownEditor v-if="canEdit" ref="editor" :raw="page.contents" :editing.sync="editingPage" :deletable="page.deletable" @save="savePage" />
                <Markdown v-else :raw="page.contents" />
            </v-col>
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { HangarProject, ProjectPage } from 'hangar-internal';
import { PropType } from 'vue';
import { NamedPermission } from '~/types/enums';
import MarkdownEditor from '~/components/MarkdownEditor.vue';
import Markdown from '~/components/Markdown.vue';

@Component({
    components: {
        MarkdownEditor,
        Markdown,
    },
})
export default class VueProjectPage extends Vue {
    editingPage: boolean = false;
    page: ProjectPage = {
        contents: null,
        deletable: false,
    };

    @Prop({ type: Object as PropType<HangarProject>, required: true })
    project!: HangarProject;

    async fetch() {
        this.page = await this.$api
            .requestInternal<ProjectPage>(`pages/page/${this.$route.params.author}/${this.$route.params.slug}/${this.$route.params.pathMatch}`, false)
            .catch<any>(this.$util.handlePageRequestError);
    }

    get canEdit(): boolean {
        return this.$util.hasPerms(NamedPermission.EDIT_PAGE);
    }

    savePage(content: string) {
        this.$api
            .requestInternal(`pages/save/${this.project.id}/${this.page.id}`, true, 'post', {
                content,
            })
            .then(() => {
                this.page.contents = content;
                this.editingPage = false;
            })
            .catch((err) => {
                this.$refs.editor.loading.save = false;
                this.$util.handleRequestError(err, 'Unable to save page');
            });
    }
}
</script>

<style lang="scss" scoped></style>
