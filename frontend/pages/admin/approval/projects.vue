<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

// TODO implement AdminApprovalProjectsPage
@Component
@GlobalPermission(NamedPermission.REVIEWER)
export default class AdminApprovalProjectsPage extends Vue {}
</script>

<style lang="scss" scoped></style>
