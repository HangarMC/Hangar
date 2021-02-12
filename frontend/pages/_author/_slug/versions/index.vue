<template>
    <v-row>
        <v-col cols="12" md="9">
            <v-data-iterator :items="versions.result" :server-items-length="versions.pagination.count">
                <template #item="{ item: version }">
                    <v-sheet width="100%" color="accent" rounded>
                        <!-- todo fix url, need to get platform -->
                        <NuxtLink :to="`versions/${Object.keys(version.platformDependencies)[0].toLowerCase()}/${version.name}`">
                            <v-row>
                                <v-col cols="4" md="2" lg="2">
                                    <v-row>
                                        <v-col cols="12">{{ version.name }}</v-col>
                                        <!-- todo is this order always this way? -->
                                        <Tag :tag="version.tags[1]" />
                                    </v-row>
                                </v-col>
                                <v-col cols="8" md="6" lg="4">
                                    <Tag :tag="version.tags[0]" />
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
        <v-col cols="12" md="3">
            <v-btn v-if="canUpload" color="primary" block nuxt to="versions/new">
                {{ $t('version.new.uploadNew') }}
            </v-btn>

            <!-- todo channel filter -->
            <!-- todo platform filter -->
            <!-- todo member list -->
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { HangarProject } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { PaginatedResult, Version } from 'hangar-api';
import { NamedPermission } from '~/types/enums';
import Tag from '~/components/Tag.vue';

@Component({
    components: { Tag },
})
export default class ProjectVersionsPage extends Vue {
    versions!: PaginatedResult<Version>;

    @Prop({ type: Object as PropType<HangarProject>, required: true })
    project!: HangarProject;

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

    get canUpload() {
        return this.$util.hasPerms(NamedPermission.CREATE_VERSION);
    }
}
</script>

<style lang="scss" scoped>
.v-sheet {
    padding: 10px;
    transition: all 0.2s ease-in-out;

    &:hover {
        transform: scale(1.015);
    }

    a {
        text-decoration: none;
    }
}
</style>
