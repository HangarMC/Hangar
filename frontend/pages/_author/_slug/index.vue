<template>
    <div>{{ project.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { Project } from 'hangar-api';

@Component
export default class ProjectPage extends Vue {
    project?: Project;

    async asyncData({ $api, params }: Context): Promise<{ project: Project }> {
        return { project: await $api.request<Project>(`projects/${params.author}/${params.slug}`) };
    }
}
</script>

<style lang="scss" scoped></style>
