<template>
    <v-col cols="12" md="8" offset-md="2">
        <v-card>
            <v-card-title v-text="$t('organization.new.title')" />
            <v-card-text>
                {{ $t('organization.new.text') }}
                <v-text-field type="text" :label="$t('organization.new.name')"></v-text-field>
                <v-divider />
                <!--<UserSelectionForm />-->
            </v-card-text>
            <v-card-actions>
                <v-btn>{{ $t('form.memberList.create') }}</v-btn>
            </v-card-actions>
        </v-card>
    </v-col>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

// TODO implement OrganizationsNewPage
@Component
export default class OrganizationsNewPage extends Vue {}
</script>

<style lang="scss" scoped></style>
