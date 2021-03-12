<template>
    <div>
        <v-row>
            <v-col v-if="page.contents" cols="12" md="9">
                <MarkdownEditor
                    v-if="$perms.canEditPage"
                    ref="editor"
                    :raw="page.contents"
                    :editing.sync="editingPage"
                    :deletable="page.deletable"
                    @save="savePage"
                    @delete="deletePage"
                />
                <Markdown v-else :raw="page.contents" />
            </v-col>
            <v-col cols="12" md="3">
                <ProjectPageList :project="project" />
            </v-col>
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { ProjectPage } from 'hangar-internal';
import { Context } from '@nuxt/types';
import MarkdownEditor from '~/components/MarkdownEditor.vue';
import Markdown from '~/components/Markdown.vue';
import { DocPageMixin } from '~/components/mixins';
import ProjectPageList from '~/components/projects/ProjectPageList.vue';

@Component({
    components: {
        ProjectPageList,
        MarkdownEditor,
        Markdown,
    },
})
export default class VueProjectPage extends DocPageMixin {
    async asyncData({ $api, params, $util }: Context) {
        const page = await $api
            .requestInternal<ProjectPage>(`pages/page/${params.author}/${params.slug}/${params.pathMatch}`, false)
            .catch<any>($util.handlePageRequestError);
        return { page };
    }
}
</script>
