<template>
    <v-card max-width="900" class="mx-auto">
        <v-card-title>{{ $t('version.new.title') }}</v-card-title>
        <v-card-text v-if="!pendingVersion">
            <v-row justify="space-around">
                <v-col cols="12">
                    <v-file-input v-model="file" filled :label="$t('version.new.upload')" accepts=".jar,.zip" />
                </v-col>
                <v-col cols="12">
                    {{ $t('general.or') }}
                </v-col>
                <v-col cols="12">
                    <v-text-field v-model.trim="url" filled clearable :label="$t('version.new.url')" />
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
                        <v-text-field
                            v-model="pendingVersion.versionString"
                            :hide-details="isFile"
                            :label="$t('version.new.form.versionString')"
                            :disabled="isFile"
                            :autofocus="!isFile"
                            filled
                            :rules="[$util.$vc.require('Version string')]"
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
                            :rules="[$util.$vc.require('External URL')]"
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
                        <NewChannelModal @create="addChannel">
                            <template #activator="{ on, attrs }">
                                <v-btn color="info" v-bind="attrs" v-on="on">
                                    {{ $t('version.new.form.addChannel') }}
                                    <v-icon right>mdi-plus</v-icon>
                                </v-btn>
                            </template>
                        </NewChannelModal>
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
                                    v-model="pendingVersion.platformDependencies[platform.name.toUpperCase()]"
                                    :rules="platformVersionRules"
                                    class="platform-version-checkbox"
                                    dense
                                    hide-details
                                    :label="version"
                                    :value="version"
                                    @change="togglePlatformVersion($event, platform.name.toUpperCase())"
                                />
                            </v-col>
                        </v-row>
                    </v-card-text>
                </v-card>
                <v-card v-if="platformsForPluginDeps.length" color="accent darken-1" elevation="1" class="mt-2 pb-1">
                    <v-card-title class="pb-0">{{ $t('version.new.form.dependencies') }}</v-card-title>
                    <template v-for="platform in platformsForPluginDeps">
                        <v-card-subtitle :key="`${platform}-deps`" class="mt-3 pb-0">{{ $store.state.platforms.get(platform).name }}</v-card-subtitle>
                        <v-simple-table :key="`${platform}-deps-table`" class="ma-2">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Required?</th>
                                    <th>Link</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="dep in pendingVersion.pluginDependencies[platform]" :key="`${platform}-${dep.name}`">
                                    <td>{{ dep.name }}</td>
                                    <!--TODO having ripple here produces console errors?-->
                                    <td><v-simple-checkbox v-model="dep.required" :ripple="false" /></td>
                                    <td>
                                        <v-text-field
                                            v-model="dep.externalUrl"
                                            dense
                                            hide-details
                                            placeholder="External Link"
                                            :disabled="dep.namespace !== null && Object.keys(dep.namespace).length !== 0"
                                            :rules="
                                                dep.namespace !== null && Object.keys(dep.namespace).length !== 0
                                                    ? []
                                                    : [$util.$vc.require('version.new.form.externalUrl')]
                                            "
                                            clearable
                                        />
                                        <v-autocomplete
                                            v-model="dep.namespace"
                                            dense
                                            hide-details
                                            hide-no-data
                                            placeholder="Hangar Project"
                                            class="mb-2"
                                            :items="hangarProjectSearchResults"
                                            :item-text="getNamespace"
                                            return-object
                                            clearable
                                            auto-select-first
                                            :disabled="!!dep.externalUrl"
                                            :rules="!!dep.externalUrl ? [] : [$util.$vc.require('version.new.form.hangarProject')]"
                                            @update:search-input="onSearch"
                                        />
                                    </td>
                                </tr>
                            </tbody>
                        </v-simple-table>
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
            <v-btn v-else color="primary" :disabled="!validForm" @click="createVersion">{{ $t('general.create') }}</v-btn>
        </v-card-actions>
    </v-card>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import remove from 'lodash-es/remove';
import { IPlatform, PendingVersion, ProjectChannel } from 'hangar-internal';
import { PaginatedResult, Project, ProjectNamespace } from 'hangar-api';
import { HangarProjectMixin } from '~/components/mixins';
import { ProjectPermission } from '~/utils/perms';
import { NamedPermission, Platform } from '~/types/enums';
import MarkdownEditor from '~/components/MarkdownEditor.vue';
import NewChannelModal from '~/components/modals/NewChannelModal.vue';
import { RootState } from '~/store';

// TODO implement setting up dependencies
@Component({
    components: { NewChannelModal, MarkdownEditor },
})
@ProjectPermission(NamedPermission.CREATE_VERSION)
export default class ProjectVersionsNewPage extends HangarProjectMixin {
    file: File | null = null;
    url: string | null = null;
    pendingVersion: PendingVersion | null = null;
    validForm: boolean = false;
    channels: ProjectChannel[] = [];
    selectedPlatforms: string[] = [];
    hangarProjectSearchResults: ProjectNamespace[] = [];

    loading = {
        create: false,
        submit: false,
    };

    head() {
        return {
            title: this.$t('version.new.title'),
        };
    }

    onSearch(val: string) {
        if (val) {
            console.log(val);
            this.$api
                .request<PaginatedResult<Project>>(`projects?relevance=true&limit=25&offset=0&q=${val}`)
                .then((projects) => {
                    // console.log(projects.result);
                    this.hangarProjectSearchResults = projects.result.map((p) => p.namespace);
                })
                .catch(console.error);
        }
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
        for (const key of Object.keys(this.pendingVersion!.pluginDependencies)) {
            if (this.pendingVersion?.pluginDependencies[key as Platform].length) {
                platforms.push(key as Platform);
            }
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
                    // this.$router.push(`/${this.$route.params.author}/${this.$route.params.slug}/versions`);
                })
                .catch(this.$util.handleRequestError);
        }
    }

    togglePlatformVersion(value: string[], platform: string) {
        if (value.length === 0 && this.selectedPlatforms.includes(platform)) {
            this.$delete(this.selectedPlatforms, this.selectedPlatforms.indexOf(platform));
        } else if (!this.selectedPlatforms.includes(platform)) {
            this.selectedPlatforms.push(platform);
        }
    }

    // todo handle errors better, for example "version.new.error.duplicateNameAndPlatform"
    // TODO should have a set of validate name endpoints to provide this check while the user is changing the name (Project name, Version name, page name, channel name, etc)
    async createPendingVersion() {
        this.loading.create = true;
        const data: FormData = new FormData();
        if (this.url) {
            data.append('url', this.url);
        } else if (this.file) {
            data.append('pluginFile', this.file);
        } else {
            this.$util.error('Must specify a url or upload file');
            return;
        }
        this.channels = await this.$api
            .requestInternal<ProjectChannel[]>(`channels/all/${this.project.id}`, false)
            .catch<any>(this.$util.handlePageRequestError);
        this.pendingVersion = await this.$api
            .requestInternal<PendingVersion>(`versions/version/${this.project.id}/upload`, true, 'post', data)
            .catch<any>(this.$util.handlePageRequestError);
        for (const platformDependenciesKey in this.pendingVersion?.platformDependencies) {
            if (this.pendingVersion?.platformDependencies[platformDependenciesKey as Platform].length) {
                this.selectedPlatforms.push(platformDependenciesKey);
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
