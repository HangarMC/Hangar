<template>
    <div>{{ project.name }} Versions</div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { HangarProject } from 'hangar-internal';

// TODO implement ProjectVersionsPage
@Component
export default class ProjectVersionsPage extends Vue {
    @Prop({ type: Object as PropType<HangarProject>, required: true })
    project!: HangarProject;
}
</script>

<style lang="scss" scoped></style>
