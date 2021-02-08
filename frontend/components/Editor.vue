<template>
    <div>
        <!-- todo editor -->
        {{ $route.name }}
        hur dur am a editor
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

@Component
export default class Editor extends Vue {}
</script>

<style lang="scss" scoped></style>
