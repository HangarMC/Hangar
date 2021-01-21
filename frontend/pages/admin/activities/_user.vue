<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

@Component
export default class AdminActivitiesUserPage extends Vue {}
</script>

<style lang="scss" scoped></style>
