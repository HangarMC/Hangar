<template>
    <v-row>
        <v-col cols="12" md="9">
            <v-data-iterator :items="versions.result" :server-items-length="versions.pagination.count">
                <template #item="{ item: version }">
                    <v-sheet width="100%" color="accent" rounded class="version">
                        <!-- todo fix url, need to get platform -->
                        <NuxtLink :to="`versions/${version.name}`">
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
        <v-col cols="12" md="3" class="sidebar">
            <v-btn v-if="$perms.canCreateVersion" color="primary" block nuxt to="versions/new">
                {{ $t('version.new.uploadNew') }}
            </v-btn>

            <!-- todo implement filtering -->
            <v-card>
                <v-card-title>
                    {{ $t('version.channels') }}
                    <v-checkbox class="flex-right"></v-checkbox>
                </v-card-title>
                <v-card-text>
                    <v-list dense>
                        <!-- todo channels -->
                        <v-list-item v-for="platform in platforms" :key="platform.name">
                            <Tag :name="platform.name" :color="platform.tagColor"></Tag>
                            <v-checkbox class="flex-right"></v-checkbox>
                        </v-list-item>
                    </v-list>
                </v-card-text>
                <v-card-actions v-if="$perms.canEditTags">
                    <v-btn class="flex-right" :to="`/${project.namespace.owner}/${project.namespace.slug}/channels`">
                        {{ $t('version.editChannels') }}
                    </v-btn>
                </v-card-actions>
            </v-card>

            <v-card>
                <v-card-title>
                    {{ $t('version.platforms') }}
                    <v-checkbox class="flex-right"></v-checkbox>
                </v-card-title>
                <v-card-text>
                    <v-list dense>
                        <v-list-item v-for="platform in platforms" :key="platform.name">
                            <Tag :name="platform.name" :color="platform.tagColor"></Tag>
                            <v-checkbox class="flex-right"></v-checkbox>
                        </v-list-item>
                    </v-list>
                </v-card-text>
                <v-card-actions></v-card-actions>
            </v-card>

            <MemberList
                :can-edit="$perms.canManageSubjectMembers"
                :manage-url="`/${project.namespace.owner}/${project.namespace.slug}/settings`"
                :members="project.members"
            ></MemberList>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { HangarProject, IPlatform } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { PaginatedResult, Tag as ApiTag, Version } from 'hangar-api';
import Tag from '~/components/Tag.vue';
import MemberList from '~/components/MemberList.vue';
import { RootState } from '~/store';

@Component({
    components: { MemberList, Tag },
})
export default class ProjectVersionsPage extends Vue {
    versions!: PaginatedResult<Version>;

    @Prop({ type: Object as PropType<HangarProject>, required: true })
    project!: HangarProject;

    get platforms(): IPlatform[] {
        return Array.from((this.$store.state as RootState).platforms.values());
    }

    async asyncData({ params, $api, $util }: Context) {
        const versions = await $api
            .request<PaginatedResult<Version>>(`projects/${params.author}/${params.slug}/versions`, false, 'get', {
                limit: 25,
                offset: 0,
                // TODO pagination
            })
            .catch<any>($util.handlePageRequestError);
        return { versions };
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

.sidebar {
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
}
</style>
