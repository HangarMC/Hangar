<template>
    <v-col cols="12" md="8" offset-md="2">
        <v-card>
            <v-card-title v-text="$t('organization.new.title')" />
            <v-card-subtitle>{{ $t('organization.new.text') }}</v-card-subtitle>
            <!--TODO error message if already at max orgs-->
            <v-card-text>
                <v-form v-model="validForm">
                    <v-text-field
                        v-model="form.name"
                        class="mt-2"
                        filled
                        :loading="validateLoading"
                        :label="$t('organization.new.name')"
                        :rules="[
                            $util.$vc.require($t('organization.new.name')),
                            $util.$vc.regex($t('organization.new.name'), validations.org.regex),
                            $util.$vc.minLength(validations.org.min),
                            $util.$vc.maxLength(validations.org.max),
                        ]"
                        :error-messages="nameErrorMessages"
                    />
                    <v-divider />
                    <MemberList ref="memberList" class="mt-7 elevation-5" no-save-btn :roles="roles" always-editing :search-filter="searchFilter" />
                </v-form>
            </v-card-text>
            <v-card-actions class="justify-end">
                <v-btn color="success" :disabled="!canCreate" :loading="loading" @click="create">
                    <v-icon left>mdi-check</v-icon>
                    {{ $t('form.memberList.create') }}
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-col>
</template>

<script lang="ts">
import { Component, mixins, Watch } from 'nuxt-property-decorator';
import { TranslateResult } from 'vue-i18n';
import { Context } from '@nuxt/types';
import { Role, User } from 'hangar-api';
import { Authed, HangarForm } from '~/components/mixins';
import MemberList from '~/components/projects/MemberList.vue';
import { LoggedIn } from '~/utils/perms';

@Component({
    components: { MemberList },
})
@LoggedIn
export default class OrganizationsNewPage extends mixins(Authed, HangarForm) {
    head() {
        return {
            title: this.$t('organization.new.title'),
        };
    }

    roles!: Role[];
    nameErrorMessages: TranslateResult[] = [];
    validateLoading = false;
    form = {
        name: '',
    };

    $refs!: {
        memberList: MemberList;
    };

    searchFilter(user: User) {
        return user.name !== this.currentUser.name;
    }

    get canCreate() {
        return (
            this.validForm &&
            !this.validateLoading &&
            (this.$refs.memberList.isEdited || (this.$refs.memberList.editedMembers.length === 0 && this.$refs.memberList.editingMembers.length === 0))
        );
    }

    create() {
        this.loading = true;
        this.$api
            .requestInternal('organizations/create', true, 'post', {
                name: this.form.name,
                members: this.$refs.memberList.editedMembers,
            })
            .then(() => {
                this.$router.push({
                    name: 'user',
                    params: {
                        user: this.form.name,
                    },
                });
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading = false;
            });
    }

    @Watch('form.name')
    checkName(val: string) {
        if (!val) return true;
        this.validateLoading = true;
        this.nameErrorMessages = [];
        this.$api
            .requestInternal('organizations/validate', false, 'get', { name: val })
            .catch(() => {
                this.nameErrorMessages.push(this.$t('organization.new.error.duplicateName'));
            })
            .finally(() => {
                this.validateLoading = false;
            });
    }

    async asyncData({ $api, $util }: Context) {
        const orgRoles = await $api.requestInternal('data/orgRoles', false).catch($util.handlePageRequestError);
        return { roles: orgRoles };
    }
}
</script>

<style lang="scss" scoped></style>
