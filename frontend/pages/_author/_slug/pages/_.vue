<template>
    <v-row>
        <v-col v-if="page.contents" cols="12" md="9" class="main-page-content">
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
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { ProjectPage } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { Markdown, MarkdownEditor } from '~/components/markdown';
import { DocPageMixin } from '~/components/mixins';
import { ProjectPageList } from '~/components/projects';

@Component({
    components: {
        ProjectPageList,
        MarkdownEditor,
        Markdown,
    },
})
export default class VueProjectPage extends DocPageMixin {
    async asyncData({ $api, params, $util, redirect }: Context) {
        const page = await $api
            .requestInternal<ProjectPage>(`pages/page/${params.author}/${params.slug}/${params.pathMatch}`, false)
            .catch<any>($util.handlePageRequestError);
        if (page && page.isHome) {
            return redirect(`/${params.author}/${params.slug}`);
        }
        return { page };
    }
}
</script>
