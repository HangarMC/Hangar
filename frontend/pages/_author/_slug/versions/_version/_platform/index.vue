<template>
    <div>
        <v-row>
            <v-col>
                <h1>{{ version.name }}</h1>
                <TagComponent :tag="channel" :short-form="true" />
                <v-subheader>{{ $t('version.page.subheader', [version.author, $util.prettyDate(version.createdAt)]) }}</v-subheader>
            </v-col>
            <v-col class="text-right">
                <v-subheader style="justify-content: end">
                    <i v-if="isReviewer && version.approvedBy">{{ $t('version.page.adminMsg', [version.approvedBy, $util.prettyDate(version.createdAt)]) }}</i>
                    <v-icon v-if="version.recommended" :title="$t('version.page.recommended')">mdi-diamond-stone</v-icon>
                    <v-icon v-if="isReviewStateChecked" :title="approvalTooltip">mdi-check-circle-outline</v-icon>
                </v-subheader>
                <!-- todo maybe move the review logs to the admin actions dropdown? -->
                <template v-if="isReviewer">
                    <v-btn v-if="isReviewStateChecked" color="secondary" :to="$route.path + '/reviews'" nuxt>{{ $t('version.page.reviewLogs') }}</v-btn>
                    <v-btn v-else color="secondary" :to="$route.path + '/reviews'" nuxt>
                        <v-icon left>mdi-play</v-icon>
                        {{ $t('version.page.reviewStart') }}
                    </v-btn>
                </template>

                <v-btn v-if="canDeleteVersion" color="error" @click="deleteVersion">{{ $t('version.page.delete') }}</v-btn>
                <v-btn v-if="!version.externalUrl" color="primary" :to="$route.path + '/download'">{{ $t('version.page.download') }}</v-btn>
                <v-btn v-else color="primary" :to="$route.path + '/download'">{{ $t('version.page.downloadExternal') }}</v-btn>
                <v-menu v-if="canViewLogs || isReviewer || canHardDeleteVersion" offset-y open-on-hover>
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
                        <v-list-item v-if="isReviewer && version.visibility === 'softDelete'">
                            <!--todo i18n & restore modal-->
                            <v-list-item-title>Undo delete</v-list-item-title>
                        </v-list-item>
                        <v-list-item
                            v-if="canHardDeleteVersion && !version.recommended && (project.info.publicVersions > 1 || version.visibility === 'softDelete')"
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
                <MarkdownEditor v-if="canEdit" ref="editor" :raw="version.description" :editing.sync="editingPage" :deletable="false" @save="savePage" />
                <Markdown v-else :raw="version.description" />
            </v-col>
            <v-col cols="12" md="4">
                <v-card>
                    <v-card-subtitle>{{ $t('version.page.platform') }}</v-card-subtitle>
                    <v-card-text>
                        <v-icon v-text="`$vuetify.icons.${platform.name.toLowerCase()}`"></v-icon>
                        {{ $store.state.platforms.get($route.params.platform.toUpperCase()).name }}:
                        {{ version.platformDependencies[$route.params.platform.toUpperCase()].join(', ') }}
                    </v-card-text>
                    <v-divider />
                    <v-card-subtitle>{{ $t('version.page.dependencies') }}</v-card-subtitle>
                    <v-list>
                        <v-list-item v-for="dep in version.pluginDependencies[platform.name.toUpperCase()]" :key="dep.name">
                            <v-list-item-title>
                                {{ dep.name }}
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
import { IPlatform } from 'hangar-internal';
import { HangarProjectVersionMixin } from '~/components/mixins';
import { NamedPermission, Platform, ReviewState } from '~/types/enums';
import TagComponent from '~/components/Tag.vue';
import Markdown from '~/components/Markdown.vue';
import MarkdownEditor from '~/components/MarkdownEditor.vue';
import { RootState } from '~/store';

@Component({
    components: { TagComponent, Markdown, MarkdownEditor },
})
export default class ProjectVersionPage extends HangarProjectVersionMixin {
    editingPage: boolean = false;

    get channel(): Tag | null {
        return this.version?.tags?.find((t) => t.name === 'Channel') || null;
    }

    get canEdit() {
        return this.$util.hasPerms(NamedPermission.EDIT_VERSION);
    }

    get canDeleteVersion() {
        return this.$util.hasPerms(NamedPermission.DELETE_VERSION);
    }

    get canHardDeleteVersion() {
        return this.$util.hasPerms(NamedPermission.HARD_DELETE_VERSION);
    }

    get canViewLogs() {
        return this.$util.hasPerms(NamedPermission.VIEW_LOGS);
    }

    get isReviewer() {
        return this.$util.hasPerms(NamedPermission.REVIEWER);
    }

    get isReviewStateChecked() {
        return this.version?.reviewState === ReviewState.PARTIALLY_REVIEWED || this.version?.reviewState === ReviewState.REVIEWED;
    }

    get approvalTooltip() {
        return this.version?.reviewState === ReviewState.PARTIALLY_REVIEWED ? this.$t('version.page.partiallyApproved') : this.$t('version.page.approved');
    }

    get platform(): IPlatform {
        return (this.$store.state as RootState).platforms.get(this.$route.params.platform.toUpperCase() as Platform)!;
    }

    $refs!: {
        editor: MarkdownEditor;
    };

    savePage(content: string) {
        this.$api
            .requestInternal(`versions/version/${this.project.id}/${this.version.id}/saveDescription`, true, 'post', {
                content,
            })
            .then(() => {
                this.version.description = content;
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
