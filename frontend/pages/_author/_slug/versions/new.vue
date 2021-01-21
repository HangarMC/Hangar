<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

// was multiple pages before, now one
@Component
export default class ProjectVersionsNewPage extends Vue {}
</script>

<style lang="scss" scoped></style>
