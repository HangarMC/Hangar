<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

// TODO implement AdminStatsPage

@Component
export default class AdminStatsPage extends Vue {}
</script>

<style lang="scss" scoped></style>
