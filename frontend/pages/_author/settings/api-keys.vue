<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

// TODO implement AuthorSettingsApiKeysPage (I think this should just be a modal in the user page - Jake)
@Component
export default class AuthorSettingsApiKeysPage extends Vue {}
</script>

<style lang="scss" scoped></style>
