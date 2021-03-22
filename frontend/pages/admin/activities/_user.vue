<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

// TODO implement AdminActivitiesUserPage
@Component
@GlobalPermission(NamedPermission.REVIEWER)
export default class AdminActivitiesUserPage extends Vue {}
</script>

<style lang="scss" scoped></style>
