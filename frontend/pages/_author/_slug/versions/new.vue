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
            <v-form v-model="validForm">
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
                            filled
                            :rules="[$util.$vc.require('Version string')]"
                        />
                    </v-col>
                    <v-col v-if="isFile" md="4" sm="6" cols="12">
                        <v-text-field :value="pendingVersion.fileInfo.name" hide-details :label="$t('version.new.form.fileName')" disabled filled />
                    </v-col>
                    <v-col v-if="isFile" md="4" sm="6" cols="12">
                        <v-text-field
                            :value="filesize(pendingVersion.fileInfo.sizeBytes)"
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
                <v-sheet color="accent" elevation="1" rounded class="mt-2">
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
                <v-row class="mt-3">
                    <v-col cols="12" class="pb-0 ml-6">
                        <h2>{{ $t('version.new.form.release.bulletin') }}</h2>
                        {{ $t('version.new.form.release.desc') }}
                    </v-col>
                    <v-col cols="12">
                        <MarkdownEditor ref="editor" :deletable="false" :cancellable="false" :saveable="false" editing :raw="pendingVersion.description" />
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
import filesize from 'filesize';
import remove from 'lodash-es/remove';
import { PendingVersion, ProjectChannel } from 'hangar-internal';
import { HangarProjectMixin } from '~/components/mixins';
import { ProjectPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';
import MarkdownEditor from '~/components/MarkdownEditor.vue';
import NewChannelModal from '~/components/modals/NewChannelModal.vue';

// TODO implement ProjectVersionsNewPage
// was multiple pages before, now one
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

    addChannel(channel: ProjectChannel) {
        if (this.pendingVersion) {
            remove(this.channels, (c) => c.temp);
            this.channels.push(Object.assign({}, channel));
            this.pendingVersion.channelName = channel.name;
        }
    }

    $refs!: {
        editor: MarkdownEditor;
    };

    createVersion() {
        if (this.pendingVersion) {
            this.pendingVersion.description = this.$refs.editor.rawEdited;
            this.pendingVersion.channelColor = this.currentChannel!.color;
            this.pendingVersion.channelNonReviewed = this.currentChannel!.nonReviewed;
            console.log(this.pendingVersion);
            this.$api.requestInternal(`versions/version/${this.project.id}/create`, true, 'post', this.pendingVersion).catch(this.$util.handleRequestError);
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
            this.$util.error('Must specify a url or upload file');
            return;
        }
        this.channels = await this.$api
            .requestInternal<ProjectChannel[]>(`channels/all/${this.project.id}`, false)
            .catch<any>(this.$util.handlePageRequestError);
        this.pendingVersion = await this.$api
            .requestInternal<PendingVersion>(`versions/version/${this.project.id}/upload`, true, 'post', data)
            .catch<any>(this.$util.handlePageRequestError);

        this.loading.create = false;
    }

    get isFile() {
        return this.pendingVersion?.isFile;
    }

    filesize(input: any) {
        return filesize(input);
    }

    reset() {
        this.url = null;
        this.file = null;
        this.pendingVersion = null;
    }
}
</script>
