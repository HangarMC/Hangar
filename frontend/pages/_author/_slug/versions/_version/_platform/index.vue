<template>
    <div>
        <v-row>
            <v-col>
                <h1>{{ projectVersion.name }}</h1>
                <TagComponent :tag="channel" :short-form="true" />
                <v-subheader>{{ $t('version.page.subheader', [projectVersion.author, $util.prettyDate(projectVersion.createdAt)]) }}</v-subheader>
            </v-col>
            <v-col class="text-right">
                <v-subheader style="justify-content: end">
                    <i v-if="$perms.isReviewer && projectVersion.approvedBy">{{
                        $t('version.page.adminMsg', [projectVersion.approvedBy, $util.prettyDate(projectVersion.createdAt)])
                    }}</i>
                    <v-icon v-if="projectVersion.recommended" :title="$t('version.page.recommended')">mdi-diamond-stone</v-icon>
                    <v-icon v-if="isReviewStateChecked" :title="approvalTooltip">mdi-check-circle-outline</v-icon>
                </v-subheader>
                <!-- todo maybe move the review logs to the admin actions dropdown? -->
                <template v-if="$perms.isReviewer">
                    <v-btn v-if="isReviewStateChecked" color="success" :to="$route.path + '/reviews'" nuxt>
                        <v-icon left>mdi-list-status</v-icon>
                        {{ $t('version.page.reviewLogs') }}
                    </v-btn>
                    <v-btn v-else-if="isUnderReview" color="info" :to="$route.path + '/reviews'" nuxt>
                        <v-icon left>mdi-list-status</v-icon>
                        {{ $t('version.page.reviewLogs') }}
                    </v-btn>
                    <v-btn v-else color="success" :to="$route.path + '/reviews'" nuxt>
                        <v-icon left>mdi-play</v-icon>
                        {{ $t('version.page.reviewStart') }}
                    </v-btn>
                </template>

                <v-btn v-if="$perms.canDeleteVersion" color="error" @click="deleteVersion">{{ $t('version.page.delete') }}</v-btn>
                <v-btn v-if="!projectVersion.externalUrl" color="primary" :to="$route.path + '/download'">{{ $t('version.page.download') }}</v-btn>
                <v-btn v-else color="primary" :to="$route.path + '/download'">{{ $t('version.page.downloadExternal') }}</v-btn>
                <v-menu v-if="$perms.canViewLogs || $perms.isReviewer || $perms.canHardDeleteVersion" offset-y open-on-hover>
                    <template #activator="{ on, attrs }">
                        <v-btn v-ripple="false" plain v-bind="attrs" v-on="on">
                            {{ $t('version.page.adminActions') }}
                            <v-icon right>mdi-chevron-down</v-icon>
                        </v-btn>
                    </template>
                    <v-list>
                        <!--todo route for user action log-->
                        <v-list-item nuxt :to="`ddd`">
                            <v-list-item-title>
                                {{ $t('version.page.userAdminLogs') }}
                            </v-list-item-title>
                        </v-list-item>
                        <v-list-item v-if="$perms.isReviewer && projectVersion.visibility === 'softDelete'">
                            <!--todo i18n & restore modal-->
                            <v-list-item-title>Undo delete</v-list-item-title>
                        </v-list-item>
                        <v-list-item
                            v-if="
                                $perms.canHardDeleteVersion &&
                                !projectVersion.recommended &&
                                (project.info.publicVersions > 1 || projectVersion.visibility === 'softDelete')
                            "
                        >
                            <!--todo i18n & hard delete modal-->
                            <v-list-item-title>Hard delete</v-list-item-title>
                        </v-list-item>
                    </v-list>
                </v-menu>
            </v-col>
        </v-row>
        <v-row>
            <v-col cols="12" md="8">
                <MarkdownEditor
                    v-if="$perms.canEditVersion"
                    ref="editor"
                    :raw="projectVersion.description"
                    :editing.sync="editingPage"
                    :deletable="false"
                    @save="savePage"
                />
                <Markdown v-else :raw="projectVersion.description" />
            </v-col>
            <v-col cols="12" md="4">
                <v-card>
                    <v-card-subtitle>{{ $t('version.page.platform') }}<PlatformVersionEditModal :project="project" :versions="versions" /></v-card-subtitle>
                    <v-card-text>
                        <v-icon v-text="`$vuetify.icons.${platform.name.toLowerCase()}`"></v-icon>
                        {{ $store.state.platforms.get($route.params.platform.toUpperCase()).name }}:
                        {{ platformTag.data }}
                    </v-card-text>
                    <v-divider />
                    <v-card-subtitle class="pb-0">
                        {{ $t('version.page.dependencies') }}
                        <DependencyEditModal :project="project" :versions="versions" />
                    </v-card-subtitle>
                    <v-list class="pa-2">
                        <v-list-item
                            v-for="dep in projectVersion.pluginDependencies[platform.name.toUpperCase()]"
                            :key="dep.name"
                            :href="dep.externalUrl || undefined"
                            :target="dep.externalUrl ? '_blank' : undefined"
                            :nuxt="!!dep.namespace"
                            :to="!!dep.namespace ? { name: 'author-slug', params: { author: dep.namespace.owner, slug: dep.namespace.slug } } : undefined"
                        >
                            <v-list-item-title>
                                {{ dep.name }}
                                <small v-if="dep.required">({{ $t('general.required') }})</small>
                            </v-list-item-title>
                        </v-list-item>
                    </v-list>
                </v-card>
            </v-col>
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Tag } from 'hangar-api';
import { TranslateResult } from 'vue-i18n';
import { HangarProjectVersionMixin } from '~/components/mixins';
import { ReviewState } from '~/types/enums';
import TagComponent from '~/components/Tag.vue';
import { Markdown, MarkdownEditor } from '~/components/markdown';
import PlatformVersionEditModal from '~/components/modals/versions/PlatformVersionEditModal.vue';
import DependencyEditModal from '~/components/modals/versions/DependencyEditModal.vue';

@Component({
    components: { DependencyEditModal, PlatformVersionEditModal, TagComponent, Markdown, MarkdownEditor },
})
export default class ProjectVersionPage extends HangarProjectVersionMixin {
    editingPage: boolean = false;

    get channel(): Tag | null {
        return this.projectVersion.tags?.find((t) => t.name === 'Channel') || null;
    }

    get approvalTooltip(): TranslateResult {
        return this.projectVersion.reviewState === ReviewState.PARTIALLY_REVIEWED
            ? this.$t('version.page.partiallyApproved')
            : this.$t('version.page.approved');
    }

    get platformTag(): Tag {
        return this.projectVersion.tags.find((t) => t.name === this.platform.name)!;
    }

    $refs!: {
        editor: MarkdownEditor;
    };

    savePage(content: string) {
        this.$api
            .requestInternal(`versions/version/${this.project.id}/${this.projectVersion.id}/saveDescription`, true, 'post', {
                content,
            })
            .then(() => {
                this.projectVersion.description = content;
                this.editingPage = false;
            })
            .catch((err) => {
                this.$refs.editor.loading.save = false;
                // TODO i18n for version desc save?
                this.$util.handleRequestError(err, 'page.new.error.save');
            });
    }

    // TODO implement all of the below
    deleteVersion() {}
}
</script>

<style lang="scss" scoped></style>
