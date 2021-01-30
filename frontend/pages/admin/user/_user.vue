<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

// TODO implement AdminUserPage

@Component
export default class AdminUserPage extends Vue {}
</script>

<style lang="scss" scoped></style>
