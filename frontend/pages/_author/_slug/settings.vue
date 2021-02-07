<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { ProjectPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

// TODO implement ProjectManagePage
@Component
@ProjectPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
export default class ProjectManagePage extends Vue {}
</script>

<style lang="scss" scoped></style>
