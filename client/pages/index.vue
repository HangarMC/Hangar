<template>
    <v-row justify="center" align="center">
        <v-col cols="12" sm="8" md="6">
            <ProjectList :projects="projects.result"></ProjectList>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { PaginatedProjectList } from 'hangar-api';

@Component
export default class Home extends Vue {
    projects?: PaginatedProjectList;

    async asyncData({ $api }: Context): Promise<{ projects: PaginatedProjectList }> {
        return { projects: await $api.request<PaginatedProjectList>('projects', 'get', { limit: 25, offset: 0 }) };
    }
}
</script>
