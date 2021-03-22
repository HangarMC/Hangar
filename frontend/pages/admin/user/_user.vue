<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

// TODO implement AdminUserPage

@Component
@GlobalPermission(NamedPermission.EDIT_ALL_USER_SETTINGS)
export default class AdminUserPage extends Vue {}
</script>

<style lang="scss" scoped></style>
