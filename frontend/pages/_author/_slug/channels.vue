<template>
    <v-col cols="12" md="8" offset-md="2">
        <v-card>
            <v-card-title>{{ $t('channel.manage.title') }}</v-card-title>
            <v-card-subtitle>{{ $t('channel.manage.subtitle') }}</v-card-subtitle>
            <v-card-text>
                <v-simple-table>
                    <thead>
                        <tr>
                            <th><v-icon>mdi-tag</v-icon>{{ $t('channel.manage.channelName') }}</th>
                            <th><v-icon>mdi-format-list-numbered</v-icon>{{ $t('channel.manage.versionCount') }}</th>
                            <th><v-icon>mdi-file-find</v-icon>{{ $t('channel.manage.reviewed') }}</th>
                            <th><v-icon>mdi-pencil</v-icon>{{ $t('channel.manage.edit') }}</th>
                            <th><v-icon>mdi-delete</v-icon>{{ $t('channel.manage.trash') }}</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="channel in channels" :key="channel.name">
                            <td><Tag :name="channel.name" :color="{ background: channel.color }"></Tag></td>
                            <!-- todo get number of versions in channel -->
                            <td>X</td>
                            <td>
                                <v-icon v-if="channel.nonReviewed">mdi-checkbox-blank-circle-outline</v-icon>
                                <v-icon v-else>mdi-check-circle</v-icon>
                            </td>
                            <td>
                                <ChannelModal :edit="true" :channel="channel" @create="editChannel">
                                    <template #activator="{ on, attrs }">
                                        <v-btn color="warning" v-bind="attrs" v-on="on">{{ $t('channel.manage.editButton') }}</v-btn>
                                    </template>
                                </ChannelModal>
                            </td>
                            <td>
                                <!-- todo we need to properly think about how channel deletion works -->
                                <v-btn color="error" @click="deleteChannel(channel.name)">{{ $t('channel.manage.deleteButton') }}</v-btn>
                            </td>
                        </tr>
                    </tbody>
                </v-simple-table>
            </v-card-text>
            <v-card-actions>
                <ChannelModal @create="addChannel">
                    <template #activator="{ on, attrs }">
                        <v-btn v-if="channels.length < validations.project.maxChannelCount" color="primary" v-bind="attrs" v-on="on">
                            {{ $t('channel.manage.add') }}
                            <v-icon right>mdi-plus</v-icon>
                        </v-btn>
                    </template>
                </ChannelModal>
            </v-card-actions>
        </v-card>
    </v-col>
</template>

<script lang="ts">
import { Component, State } from 'nuxt-property-decorator';
import { ProjectChannel } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { HangarProjectMixin } from '~/components/mixins';
import Tag from '~/components/Tag.vue';
import ChannelModal from '~/components/modals/ChannelModal.vue';
import { RootState } from '~/store';

@Component({
    components: { ChannelModal, Tag },
})
export default class ProjectChannelsPage extends HangarProjectMixin {
    channels!: ProjectChannel[];

    // TODO editChannel
    editChannel(name: String) {
        console.log('edit channel ', name);
    }

    // TODO deleteChannel
    deleteChannel(name: String) {
        console.log('delete channel ', name);
    }

    // TODO addChannel
    addChannel(channel: ProjectChannel) {
        this.channels.push(Object.assign({}, channel));
    }

    async asyncData({ $api, $util, params }: Context) {
        const channels = await $api
            .requestInternal<ProjectChannel[]>(`channels/${params.author}/${params.slug}`, false)
            .catch<any>($util.handlePageRequestError);
        return { channels };
    }

    @State((state: RootState) => state.validations)
    validations!: RootState['validations'];
}
</script>

<style lang="scss" scoped></style>
