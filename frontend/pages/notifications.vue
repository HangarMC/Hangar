<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

// TODO implement NotificationsPage

@Component
export default class NotificationsPage extends Vue {}
</script>

<style lang="scss" scoped></style>
