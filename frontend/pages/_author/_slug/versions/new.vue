<template>
    <v-card max-width="900" class="mx-auto">
        <v-card-title>{{ $t('version.new.title') }}</v-card-title>
        <v-card-text v-if="!pendingVersion">
            <v-row justify="space-around">
                <v-col cols="12">
                    <v-file-input v-model="file" filled :label="$t('version.new.upload')" accept=".jar,.zip" prepend-inner-icon="$file" prepend-icon="" />
                </v-col>
                <v-col cols="12">
                    {{ $t('general.or') }}
                </v-col>
                <v-col cols="12">
                    <v-text-field v-model.trim="url" filled clearable :label="$t('version.new.url')" :rules="[$util.$vc.url]" />
                </v-col>
            </v-row>
        </v-card-text>
        <v-card-text v-else>
            <v-form ref="newVersionForm" v-model="validForm">
                <v-row justify="space-around">
                    <v-col cols="12">
                        <v-btn color="warning" block @click="reset">{{ $t('general.reset') }}</v-btn>
                    </v-col>
                    <v-col :md="isFile ? 4 : 6" sm="6" cols="12">
                        <!-- TODO validate version string against existing versions. complex because they only have to be unique per-platform -->
                        <v-text-field
                            v-model="pendingVersion.versionString"
                            :hide-details="isFile"
                            :label="$t('version.new.form.versionString')"
                            :disabled="isFile"
                            :autofocus="!isFile"
                            filled
                            :rules="[
                                $util.$vc.require($t('version.new.form.versionString')),
                                $util.$vc.regex($t('version.new.form.versionString'), validations.version.regex),
                            ]"
                        />
                    </v-col>
                    <v-col v-if="isFile" md="4" sm="6" cols="12">
                        <v-text-field :value="pendingVersion.fileInfo.name" hide-details :label="$t('version.new.form.fileName')" disabled filled />
                    </v-col>
                    <v-col v-if="isFile" md="4" sm="6" cols="12">
                        <v-text-field
                            :value="$util.formatSize(pendingVersion.fileInfo.sizeBytes)"
                            hide-details
                            :label="$t('version.new.form.fileSize')"
                            disabled
                            filled
                        />
                    </v-col>
                    <v-col v-if="!isFile" sm="6" cols="12">
                        <v-text-field
                            v-model="pendingVersion.externalUrl"
                            :label="$t('version.new.form.externalUrl')"
                            filled
                            :rules="[$util.$vc.require($t('version.new.form.externalUrl')), $util.$vc.url]"
                        />
                    </v-col>
                </v-row>
                <v-row justify="space-around">
                    <v-col>
                        <v-select
                            v-model="pendingVersion.channelName"
                            :label="$t('version.new.form.channel')"
                            :items="channels"
                            item-text="name"
                            item-value="name"
                            hide-details
                            :color="currentChannel.color"
                            class="channel-select"
                        >
                            <template #prepend-inner>
                                <v-icon :color="currentChannel.color">mdi-square-rounded</v-icon>
                            </template>
                        </v-select>
                    </v-col>
                    <v-col class="flex-grow-0 pl-0 pr-4" align-self="center">
                        <ChannelModal :project-id="project.id" @create="addChannel">
                            <template #activator="{ on, attrs }">
                                <v-btn v-if="channels.length < validations.project.maxChannelCount" color="info" v-bind="attrs" v-on="on">
                                    {{ $t('version.new.form.addChannel') }}
                                    <v-icon right>mdi-plus</v-icon>
                                </v-btn>
                            </template>
                        </ChannelModal>
                    </v-col>
                </v-row>
                <v-sheet color="accent darken-1" elevation="1" rounded class="mt-2">
                    <v-row justify="space-around">
                        <v-col md="3" sm="4" cols="12">
                            <v-checkbox v-model="pendingVersion.unstable" :label="$t('version.new.form.unstable')" />
                        </v-col>
                        <v-col md="3" sm="4" cols="12">
                            <v-checkbox v-model="pendingVersion.recommended" :label="$t('version.new.form.recommended')" />
                        </v-col>
                        <v-col md="3" sm="4" cols="12">
                            <v-checkbox v-model="pendingVersion.forumSync" :label="$t('version.new.form.forumPost')" />
                        </v-col>
                    </v-row>
                </v-sheet>
                <v-card color="accent darken-1" elevation="1" class="mt-6">
                    <v-card-title class="pb-0">{{ $t('version.new.form.platforms') }}</v-card-title>
                    <v-card-text>
                        <v-row justify="space-around">
                            <v-select
                                v-model="selectedPlatforms"
                                style="display: none"
                                multiple
                                :items="Array.from($store.state.platforms.keys())"
                                :rules="[(v) => !!v.length || 'Error']"
                            />
                            <v-col v-for="platform in platforms" :key="platform.name" class="flex-grow-0">
                                <div :class="{ platformError: !selectedPlatforms.length }" class="platform-version-label text-center">
                                    {{ platform.name }}
                                </div>
                                <v-checkbox
                                    v-for="version in platform.possibleVersions"
                                    :key="`${platform.name}-${version}`"
                                    v-model="pendingVersion.platformDependencies[platform.enumName]"
                                    :rules="platformVersionRules"
                                    class="platform-version-checkbox"
                                    dense
                                    hide-details
                                    :label="version"
                                    :value="version"
                                    @change="togglePlatformVersion($event, platform.enumName)"
                                />
                            </v-col>
                        </v-row>
                    </v-card-text>
                </v-card>
                <v-card v-if="platformsForPluginDeps.length" color="accent darken-1" elevation="1" class="mt-2 pb-1">
                    <v-card-title class="pb-0">{{ $t('version.new.form.dependencies') }}</v-card-title>
                    <template v-for="platform in platformsForPluginDeps">
                        <v-card-subtitle :key="`${platform}-deps`" class="mt-3 pb-0">{{ $store.state.platforms.get(platform).name }}</v-card-subtitle>
                        <DependencyTable
                            :key="`${platform}-deps-table`"
                            :platform="platform"
                            :version="pendingVersion"
                            :no-editing="pendingVersion.isFile"
                            :new-deps-prop="pendingVersion.pluginDependencies[platform]"
                            :is-new="!pendingVersion.isFile"
                        />
                    </template>
                </v-card>
                <v-row class="mt-3">
                    <v-col cols="12" class="pb-0 ml-6">
                        <h2>{{ $t('version.new.form.release.bulletin') }}</h2>
                        {{ $t('version.new.form.release.desc') }}
                    </v-col>
                    <v-col cols="12">
                        <MarkdownEditor
                            ref="editor"
                            :deletable="false"
                            :cancellable="false"
                            :saveable="false"
                            editing
                            :raw="pendingVersion.description"
                            :rules="[$util.$vc.require($t('version.new.form.release.bulletin'))]"
                        />
                    </v-col>
                </v-row>
            </v-form>
        </v-card-text>
        <v-card-actions class="justify-end">
            <v-btn v-if="!pendingVersion" color="primary" :disabled="!canCreate" :loading="loading.create" @click="createPendingVersion">
                {{ $t('general.continue') }}
            </v-btn>
            <v-btn v-else color="primary" :disabled="!validForm" :loading="loading.submit" @click="createVersion">{{ $t('general.create') }}</v-btn>
        </v-card-actions>
    </v-card>
</template>

<script lang="ts">
import { Component, State } from 'nuxt-property-decorator';
import remove from 'lodash-es/remove';
import { IPlatform, PendingVersion, ProjectChannel } from 'hangar-internal';
import { ProjectNamespace } from 'hangar-api';
import { HangarProjectMixin } from '~/components/mixins';
import { ProjectPermission } from '~/utils/perms';
import { NamedPermission, Platform } from '~/types/enums';
import { MarkdownEditor } from '~/components/markdown';
import ChannelModal from '~/components/modals/ChannelModal.vue';
import { RootState } from '~/store';
import DependencyTable from '~/components/modals/versions/DependencyTable.vue';

@Component({
    components: { DependencyTable, ChannelModal, MarkdownEditor },
})
@ProjectPermission(NamedPermission.CREATE_VERSION)
export default class ProjectVersionsNewPage extends HangarProjectMixin {
    file: File | null = null;
    url: string | null = null;
    pendingVersion: PendingVersion | null = null;
    validForm: boolean = false;
    channels: ProjectChannel[] = [];
    selectedPlatforms: Platform[] = [];
    loading = {
        create: false,
        submit: false,
    };

    head() {
        return {
            title: this.$t('version.new.title'),
        };
    }

    get canCreate(): boolean {
        return (!!this.file || !!this.url) && !(!!this.file && !!this.url);
    }

    get currentChannel() {
        return this.channels.find((c) => c.name === this.pendingVersion?.channelName);
    }

    get platforms(): IPlatform[] {
        if (this.pendingVersion?.isFile) {
            const platforms: IPlatform[] = [];
            for (const platformDependenciesKey in this.pendingVersion.platformDependencies) {
                platforms.push((this.$store.state as RootState).platforms.get(platformDependenciesKey as Platform)!);
            }
            return platforms;
        }
        return Array.from((this.$store.state as RootState).platforms.values());
    }

    get platformsForPluginDeps(): Platform[] {
        const platforms: Platform[] = [];
        if (this.pendingVersion?.isFile) {
            for (const key of Object.keys(this.pendingVersion!.pluginDependencies)) {
                if (this.pendingVersion?.pluginDependencies[key as Platform].length) {
                    platforms.push(key as Platform);
                }
            }
        } else {
            platforms.push(...this.selectedPlatforms);
        }
        return platforms;
    }

    get platformVersionRules(): Function[] {
        if (!this.pendingVersion?.isFile) {
            return [];
        } else {
            return [(v: string[]) => !!v.length || 'Error'];
        }
    }

    addChannel(channel: ProjectChannel) {
        if (this.pendingVersion) {
            remove(this.channels, (c) => c.temp);
            this.channels.push(Object.assign({}, channel));
            this.pendingVersion.channelName = channel.name;
        }
    }

    $refs!: {
        editor: MarkdownEditor;
        newVersionForm: any;
    };

    getNamespace(namespace: ProjectNamespace) {
        return `${namespace.owner}/${namespace.slug}`;
    }

    createVersion() {
        if (this.pendingVersion) {
            if (!this.$refs.newVersionForm.validate()) {
                return;
            }
            this.loading.submit = true;
            this.pendingVersion.description = this.$refs.editor.rawEdited;
            this.pendingVersion.channelColor = this.currentChannel!.color;
            this.pendingVersion.channelNonReviewed = this.currentChannel!.nonReviewed;
            // played around trying to get this to happen in jackson's deserialization, but couldn't figure it out.
            for (const platform in this.pendingVersion.platformDependencies) {
                if (this.pendingVersion.platformDependencies[platform as Platform].length < 1) {
                    delete this.pendingVersion.platformDependencies[platform as Platform];
                }
            }
            for (const platform in this.pendingVersion.pluginDependencies) {
                if (this.pendingVersion.pluginDependencies[platform as Platform].length < 1) {
                    delete this.pendingVersion.pluginDependencies[platform as Platform];
                }
            }
            console.log(this.pendingVersion);
            this.$api
                .requestInternal(`versions/version/${this.project.id}/create`, true, 'post', this.pendingVersion)
                .then(() => {
                    this.$router.push(`/${this.$route.params.author}/${this.$route.params.slug}/versions`);
                })
                .catch(this.$util.handleRequestError)
                .finally(() => {
                    this.loading.submit = false;
                });
        }
    }

    togglePlatformVersion(value: string[], platform: Platform) {
        if (value.length === 0 && this.selectedPlatforms.includes(platform)) {
            this.$delete(this.selectedPlatforms, this.selectedPlatforms.indexOf(platform));
        } else if (!this.selectedPlatforms.includes(platform)) {
            this.selectedPlatforms.push(platform);
        }
    }

    async createPendingVersion() {
        this.loading.create = true;
        const data: FormData = new FormData();
        if (this.url) {
            data.append('url', this.url);
        } else if (this.file) {
            data.append('pluginFile', this.file);
        } else {
            return;
        }
        this.channels = await this.$api
            .requestInternal<ProjectChannel[]>(`channels/${this.$route.params.author}/${this.$route.params.slug}`, false)
            .catch<any>(this.$util.handlePageRequestError);
        this.pendingVersion = await this.$api
            .requestInternal<PendingVersion>(`versions/version/${this.project.id}/upload`, true, 'post', data)
            .catch<any>(this.$util.handlePageRequestError);
        for (const platformDependenciesKey in this.pendingVersion?.platformDependencies) {
            if (this.pendingVersion?.platformDependencies[platformDependenciesKey as Platform].length) {
                this.selectedPlatforms.push(platformDependenciesKey as Platform);
            }
        }
        this.loading.create = false;
    }

    get isFile() {
        return this.pendingVersion?.isFile;
    }

    reset() {
        this.url = null;
        this.file = null;
        this.pendingVersion = null;
    }

    @State((state: RootState) => state.validations)
    validations!: RootState['validations'];
}
</script>
<style lang="scss" scoped>
@import '~vuetify/src/styles/styles.sass';

.platform-version-checkbox.v-input--selection-controls {
    margin-top: 0;
    padding-top: 0;
}

.platform-version-label {
    color: map-deep-get($material-dark, 'text', 'secondary');

    &.platformError {
        color: var(--v-error-base);
    }
}
</style>
