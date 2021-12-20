<template>
    <v-col cols="12" md="8" offset-md="2">
        <v-card>
            <v-card-title>{{ $t('channel.manage.title') }}</v-card-title>
            <v-card-subtitle>{{ $t('channel.manage.subtitle') }}</v-card-subtitle>
            <v-card-text>
                <v-simple-table>
                    <thead>
                        <tr>
                            <th><v-icon small left> mdi-tag </v-icon>{{ $t('channel.manage.channelName') }}</th>
                            <th><v-icon small left> mdi-format-list-numbered </v-icon>{{ $t('channel.manage.versionCount') }}</th>
                            <th><v-icon small left> mdi-file-find </v-icon>{{ $t('channel.manage.reviewed') }}</th>
                            <th><v-icon small left> mdi-pencil </v-icon>{{ $t('channel.manage.edit') }}</th>
                            <th v-if="channels.length !== 1"><v-icon small left> mdi-delete </v-icon>{{ $t('channel.manage.trash') }}</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="channel in channels" :key="channel.name">
                            <td><Tag :name="channel.name" :color="{ background: channel.color }" /></td>
                            <td>{{ channel.versionCount }}</td>
                            <td>
                                <v-icon v-if="channel.nonReviewed"> mdi-checkbox-blank-circle-outline </v-icon>
                                <v-icon v-else> mdi-check-circle </v-icon>
                            </td>
                            <td>
                                <ChannelModal :project-id="project.id" edit :channel="channel" @create="editChannel">
                                    <template #activator="{ on, attrs }">
                                        <v-btn small color="warning" v-bind="attrs" v-on="on">
                                            {{ $t('channel.manage.editButton') }}
                                        </v-btn>
                                    </template>
                                </ChannelModal>
                            </td>
                            <td v-if="channels.length !== 1">
                                <v-btn v-if="channel.versionCount === 0" small color="error" @click="deleteChannel(channel)">
                                    {{ $t('channel.manage.deleteButton') }}
                                </v-btn>
                            </td>
                        </tr>
                    </tbody>
                </v-simple-table>
            </v-card-text>
            <v-card-actions>
                <ChannelModal :project-id="project.id" @create="addChannel">
                    <template #activator="{ on, attrs }">
                        <v-btn
                            v-if="channels.length < validations.project.maxChannelCount"
                            :loading="loading.add"
                            :disabled="channels.length >= validations.project.maxChannelCount"
                            color="primary"
                            v-bind="attrs"
                            v-on="on"
                        >
                            <v-icon left> mdi-plus </v-icon>
                            {{ $t('channel.manage.add') }}
                        </v-btn>
                    </template>
                </ChannelModal>
            </v-card-actions>
        </v-card>
    </v-col>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { ProjectChannel } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { HangarProjectMixin } from '~/components/mixins';
import Tag from '~/components/Tag.vue';
import ChannelModal from '~/components/modals/projects/ChannelModal.vue';
import { ProjectPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

@Component({
    components: { ChannelModal, Tag },
})
@ProjectPermission(NamedPermission.EDIT_TAGS)
export default class ProjectChannelsPage extends HangarProjectMixin {
    channels!: ProjectChannel[];
    loading = {
        add: false,
    };

    head() {
        return this.$seo.head(
            'Channels | ' + this.project.name,
            this.project.description,
            this.$route,
            this.$util.projectUrl(this.project.namespace.owner, this.project.namespace.slug)
        );
    }

    editChannel(channel: ProjectChannel) {
        if (!channel.id) return;
        this.$api
            .requestInternal(`channels/${this.project.id}/edit`, true, 'post', {
                id: channel.id,
                name: channel.name,
                color: channel.color,
                nonReviewed: channel.nonReviewed,
            })
            .then(() => {
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError);
    }

    deleteChannel(channel: ProjectChannel) {
        if (!channel.id) return;
        this.$api
            .requestInternal(`channels/${this.project.id}/delete/${channel.id}`, true, 'post')
            .then(() => {
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError);
    }

    addChannel(channel: ProjectChannel) {
        this.loading.add = true;
        this.$api
            .requestInternal(`channels/${this.project.id}/create`, true, 'post', {
                name: channel.name,
                color: channel.color,
                nonReviewed: channel.nonReviewed,
            })
            .then(() => {
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading.add = false;
            });
    }

    async asyncData({ $api, $util, params }: Context) {
        const channels = await $api
            .requestInternal<ProjectChannel[]>(`channels/${params.author}/${params.slug}`, false)
            .catch<any>($util.handlePageRequestError);
        return { channels };
    }
}
</script>

<style lang="scss" scoped></style>
