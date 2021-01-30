<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

// TODO implement ProjectManagePage
@Component
export default class ProjectManagePage extends Vue {}
</script>

<style lang="scss" scoped></style>
