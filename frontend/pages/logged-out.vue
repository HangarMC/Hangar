<template>
    <div>Logged out</div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarComponent } from '~/components/mixins';
import { NotLoggedIn } from '~/utils/perms';

@Component({})
@NotLoggedIn
export default class LoggedOutPage extends HangarComponent {
    head() {
        return this.$seo.head('Logged out', null, this.$route, null);
    }
}
</script>

<style scoped></style>
