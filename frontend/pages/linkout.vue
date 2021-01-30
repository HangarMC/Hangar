<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

// TODO linkout page

@Component
export default class LinkoutPage extends Vue {}
</script>

<style lang="scss" scoped></style>
