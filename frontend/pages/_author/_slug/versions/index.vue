<template>
    <v-row>
        <v-col cols="12" md="9">
            <v-data-iterator :items="versions.result" :server-items-length="versions.pagination.count">
                <template #item="props">
                    <v-sheet width="100%" color="accent">
                        {{ props.item.name }}
                    </v-sheet>
                </template>
            </v-data-iterator>
        </v-col>
        <v-col cols="12" md="3">
            <v-btn v-if="canUpload" color="primary" block nuxt to="versions/new">
                Upload a New Version
                <!--TODO i18n-->
            </v-btn>
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

// TODO implement ProjectVersionsPage
@Component
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

<style lang="scss" scoped></style>
