<template>
    <div>
        <v-row v-if="version">
            <div style="float: left">
                <h1>{{ version.name }}</h1>
                <TagComponent :tag="channel" :short-form="true" />
                <v-subheader>{{ $t('version.page.subheader', [version.author, $util.prettyDate(version.createdAt)]) }}</v-subheader>
            </div>
            <div style="float: right">
                <v-subheader>
                    <!-- todo approver and stuff, perm -->
                    <i v-if="true">{{ $t('version.page.adminMsg', [version.author, $util.prettyDate(version.createdAt)]) }}</i>
                    <!-- todo check if recommended -->
                    <v-icon v-if="true" :title="$t('version.page.recommended')">mdi-diamond-stone</v-icon>
                    <v-icon v-if="isChecked" :title="approvalTooltip">mdi-check-circle-outline</v-icon>
                </v-subheader>
                <!-- todo perms -->
                <v-btn color="secondary" :to="$route.path + '/reviews'">{{ $t('version.page.reviewLogs') }}</v-btn>
                <v-btn color="error" @click="deleteVersion">{{ $t('version.page.delete') }}</v-btn>
                <!-- todo check recommended -->
                <v-btn v-if="true" color="primary" :to="$route.path + '/download'">{{ $t('version.page.download') }}</v-btn>
                <v-btn v-else color="primary" :to="$route.path + '/download'">{{ $t('version.page.downloadExternal') }}</v-btn>
                <!-- todo perms -->
                <v-menu offset-y>
                    <template #activator="{ on, attrs }">
                        <v-btn plain dark v-bind="attrs" v-on="on">{{ $t('version.page.adminActions') }}</v-btn>
                    </template>
                    <v-list>
                        <v-list-item>
                            <v-list-item-title>
                                <nuxt-link to="ddd" class="text-decoration-none">
                                    {{ $t('version.page.userAdminLogs') }}
                                </nuxt-link>
                            </v-list-item-title>
                        </v-list-item>
                    </v-list>
                </v-menu>
            </div>
            <v-divider />
        </v-row>
        <v-row v-if="version">
            <v-col cols="12" md="8">
                <MarkdownEditor v-if="canEdit" ref="editor" :raw="version.description" :editing.sync="editingPage" :deletable="false" @save="save" />
                <Markdown v-else :raw="version.description" />
            </v-col>
            <v-col cols="12" md="4">
                <v-list>
                    <v-subheader>{{ $t('version.page.dependencies') }}</v-subheader>
                </v-list>
            </v-col>
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { Project, Tag, Version } from 'hangar-api';
import { Prop } from 'vue-property-decorator';
import MarkdownEditor from '~/components/MarkdownEditor.vue';
import Markdown from '~/components/Markdown.vue';
import { NamedPermission, ReviewState } from '~/types/enums';
import TagComponent from '~/components/Tag.vue';

// TODO implement ProjectVersionsVersionPage
@Component({
    components: { TagComponent, Markdown, MarkdownEditor },
})
export default class ProjectVersionsVersionPage extends Vue {
    @Prop()
    project!: Project;

    versions!: Version[];

    editingPage: boolean = false;

    get version(): Version | null {
        return this.versions && this.versions.length > 0 ? this.versions[0] : null;
    }

    get channel(): Tag | null {
        return this.version?.tags?.find((t) => t.name === 'Channel') || null;
    }

    get canEdit() {
        return this.$util.hasPerms(NamedPermission.EDIT_VERSION);
    }

    get isChecked() {
        return this.version?.reviewState === ReviewState.PARTIALLY_REVIEWED || this.version?.reviewState === ReviewState.REVIEWED;
    }

    get approvalTooltip() {
        return this.version?.reviewState === ReviewState.PARTIALLY_REVIEWED ? this.$t('version.page.partiallyApproved') : this.$t('version.page.approved');
    }

    async asyncData({ $api, $util, params }: Context) {
        const versions = await $api
            .request<Version[]>(`projects/${params.author}/${params.slug}/versions/${params.version}`)
            .catch($util.handlePageRequestError);
        return { versions };
    }

    // TODO implement all of the below
    save() {}

    deleteVersion() {}
}
</script>

<style lang="scss" scoped></style>
