<template>
    <div>Logged out</div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarComponent } from '~/components/mixins';
import { NotLoggedIn } from '~/utils/perms';

@Component
@NotLoggedIn
export default class LoggedOutPage extends HangarComponent {}
</script>

<style scoped></style>
