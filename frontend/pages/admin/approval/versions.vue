<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

// TODO implement AdminApprovalVersionsPage
@Component
@GlobalPermission(NamedPermission.REVIEWER)
export default class AdminApprovalVersionsPage extends Vue {}
</script>

<style lang="scss" scoped></style>
