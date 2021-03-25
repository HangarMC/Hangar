<template>
    <v-row>
        <v-col cols="12" lg="9">
            <v-data-iterator :items="filteredVersions">
                <template #item="{ item: version }">
                    <v-sheet width="100%" color="accent" rounded class="version mt-2">
                        <NuxtLink :to="`/${project.namespace.owner}/${project.namespace.slug}/versions/${version.name}`">
                            <v-row>
                                <v-col cols="4" md="2" lg="2">
                                    <v-row>
                                        <v-col cols="12">{{ version.name }}</v-col>
                                        <Tag :tag="getChannelTag(version)" />
                                    </v-row>
                                </v-col>
                                <v-col cols="8" md="6" lg="4">
                                    <Tag v-for="(tag, index) in getNonChannelTags(version)" :key="index" :tag="tag" />
                                </v-col>
                                <v-col cols="0" md="4" lg="3">
                                    <v-row>
                                        <v-col cols="12">
                                            <v-icon>mdi-calendar</v-icon>
                                            {{ $util.prettyDate(version.createdAt) }}
                                        </v-col>
                                        <v-col cols="12">
                                            <v-icon>mdi-file</v-icon>
                                            <span v-if="version.fileInfo.sizeBytes">
                                                {{ $util.formatSize(version.fileInfo.sizeBytes) }}
                                            </span>
                                            <span v-else> (external) </span>
                                        </v-col>
                                    </v-row>
                                </v-col>
                                <v-col cols="0" md="3" lg="3">
                                    <v-row>
                                        <v-col cols="12">
                                            <v-icon>mdi-account-arrow-right</v-icon>
                                            <NuxtLink :to="'/' + version.author">{{ version.author }}</NuxtLink>
                                        </v-col>
                                        <v-col cols="12">
                                            <v-icon>mdi-download</v-icon>
                                            {{ version.stats.downloads }}
                                        </v-col>
                                    </v-row>
                                </v-col>
                            </v-row>
                        </NuxtLink>
                    </v-sheet>
                </template>
            </v-data-iterator>
        </v-col>
        <v-col cols="12" lg="3">
            <v-row>
                <v-col cols="12">
                    <v-btn v-if="$perms.canCreateVersion" color="primary" block nuxt to="versions/new">
                        {{ $t('version.new.uploadNew') }}
                    </v-btn>
                </v-col>
                <v-col sm="6" md="4" lg="12">
                    <v-card class="sidebar-card">
                        <v-card-title>
                            {{ $t('version.channels') }}
                            <v-tooltip bottom>
                                <template #activator="{ on }">
                                    <v-btn
                                        icon
                                        small
                                        color="warning"
                                        :to="`/${project.namespace.owner}/${project.namespace.slug}/channels`"
                                        class="ml-1"
                                        nuxt
                                        v-on="on"
                                    >
                                        <v-icon small>mdi-pencil</v-icon>
                                    </v-btn>
                                </template>
                                {{ $t('version.editChannels') }}
                            </v-tooltip>
                            <v-checkbox v-model="filter.allChecked.channels" class="flex-right" dense hide-details @change="checkAllChannels" />
                        </v-card-title>
                        <v-card-text>
                            <v-list dense>
                                <v-list-item v-for="channel in channels" :key="channel.name">
                                    <v-checkbox v-model="filter.channels" dense hide-details :value="channel.name" @change="updateChannelCheckAll" />
                                    <Tag :name="channel.name" :color="{ background: channel.color }" />
                                </v-list-item>
                            </v-list>
                        </v-card-text>
                    </v-card>
                </v-col>
                <v-col sm="6" md="4" lg="12">
                    <v-card class="sidebar-card">
                        <v-card-title>
                            {{ $t('version.platforms') }}
                            <v-checkbox v-model="filter.allChecked.platforms" class="flex-right" dense hide-details @change="checkAllPlatforms" />
                        </v-card-title>
                        <v-card-text>
                            <v-list dense>
                                <v-list-item v-for="platform in platforms" :key="platform.name">
                                    <v-checkbox v-model="filter.platforms" dense hide-details :value="platform.enumName" @change="updatePlatformCheckAll" />
                                    <Tag :name="platform.name" :color="platform.tagColor"></Tag>
                                </v-list-item>
                            </v-list>
                        </v-card-text>
                    </v-card>
                </v-col>
            </v-row>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { IPlatform, ProjectChannel } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { PaginatedResult, Tag as ApiTag, Version } from 'hangar-api';
import Tag from '~/components/Tag.vue';
import { RootState } from '~/store';
import { HangarProjectMixin } from '~/components/mixins';
import { Platform } from '~/types/enums';

@Component({
    components: { Tag },
})
export default class ProjectVersionsPage extends HangarProjectMixin {
    versions!: PaginatedResult<Version>;
    channels!: ProjectChannel[];

    filter = {
        channels: [] as string[],
        platforms: [] as Platform[],
        allChecked: {
            channels: true,
            platforms: true,
        },
    };

    get platforms(): IPlatform[] {
        return Array.from((this.$store.state as RootState).platforms.values());
    }

    get filteredVersions(): Version[] {
        return this.versions.result
            .filter((v) => (Object.keys(v.platformDependencies) as Platform[]).some((pl) => this.filter.platforms.includes(pl)))
            .filter((v) => this.filter.channels.includes(v.tags.find((t) => t.name === 'Channel')!.data));
    }

    async asyncData({ params, $api, $util }: Context) {
        const data = await Promise.all([
            $api.request<PaginatedResult<Version>>(`projects/${params.author}/${params.slug}/versions`, false, 'get', {
                limit: 25,
                offset: 0,
                // TODO pagination
            }),
            $api.requestInternal(`channels/${params.author}/${params.slug}`, false),
        ]).catch<any>($util.handlePageRequestError);
        return { versions: data[0], channels: data[1] };
    }

    created() {
        this.filter.channels.push(...this.channels.map((c) => c.name));
        this.filter.platforms.push(...this.platforms.map((p) => p.enumName));
    }

    checkAllChannels() {
        if (this.filter.allChecked.channels) {
            this.filter.channels = this.channels.map((c) => c.name);
        } else {
            this.filter.channels = [];
        }
    }

    checkAllPlatforms() {
        if (this.filter.allChecked.platforms) {
            this.filter.platforms = this.platforms.map((c) => c.enumName);
        } else {
            this.filter.platforms = [];
        }
    }

    updateChannelCheckAll() {
        this.filter.allChecked.channels = this.filter.channels.length === this.channels.length;
    }

    updatePlatformCheckAll() {
        this.filter.allChecked.platforms = this.filter.platforms.length === this.platforms.length;
    }

    getChannelTag(version: Version): ApiTag {
        const channelTag = version.tags.find((t) => t.name === 'Channel');
        if (typeof channelTag === 'undefined') {
            throw new TypeError('Version missing a channel tag');
        }
        return channelTag;
    }

    getNonChannelTags(version: Version): ApiTag[] {
        return version.tags.filter((t) => t.name !== 'Channel');
    }
}
</script>

<style lang="scss" scoped>
.version {
    padding: 10px;
    transition: all 0.2s ease-in-out;

    &:hover {
        transform: scale(1.015);
    }

    a {
        text-decoration: none;
    }
}

.sidebar-card {
    .v-card__title {
        padding-bottom: 0;
    }

    .v-list {
        padding: 0;

        .v-list-item {
            padding: 0;
        }
    }

    .v-input--selection-controls {
        margin-top: 0;
        padding-top: 0;
        //padding-bottom: 4px;
    }
}

/*.sidebar {
    .v-input--selection-controls {
        margin-top: 0;
    }

    .v-card__text {
        padding-bottom: 0;
    }

    .v-card {
        margin-top: 20px;
    }
    &.v-list .v-card {
        margin-top: 0;
    }
}*/
</style>
