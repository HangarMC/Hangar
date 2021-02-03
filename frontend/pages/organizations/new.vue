<template>
    <v-card>
        <v-card-title v-text="$t('organization.new.title')" />
        <v-card-text v-text="$t('organization.new.text')" />
        <v-text-field type="text" :label="$t('organization.new.name')"></v-text-field>
        <v-divider />
        <UserSelectionForm />
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import UserSelectionForm from '~/components/UserSelectionForm.vue';

// TODO implement OrganizationsNewPage
@Component({
    components: { UserSelectionForm },
})
export default class OrganizationsNewPage extends Vue {}
</script>

<style lang="scss" scoped></style>
