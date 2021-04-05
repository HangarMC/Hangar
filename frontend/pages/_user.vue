<template>
    <div>
        <v-row>
            <v-col class="flex-grow-0">
                <UserAvatar :username="user.name" :avatar-url="$util.avatarUrl(user.name)" :clazz="avatarClazz"></UserAvatar>
            </v-col>
            <v-col>
                <h1>
                    {{ user.name }}
                    <v-tooltip v-if="!user.isOrganization" bottom>
                        <template #activator="{ on }">
                            <v-btn icon small color="info" class="ml-n2" :href="$util.forumUrl(user.name)" v-on="on">
                                <v-icon small>mdi-open-in-new</v-icon>
                            </v-btn>
                        </template>
                        <span>{{ $t('author.viewOnForums') }}</span>
                    </v-tooltip>
                </h1>
                <div class="text--secondary">
                    <span v-if="user.tagline">{{ user.tagline }}</span>
                    <span v-else-if="canEditCurrentUser">{{ $t('author.addTagline') }}</span>
                    <HangarModal
                        v-if="canEditCurrentUser"
                        ref="taglineModal"
                        :title="$t('author.editTagline')"
                        :submit-label="$t('general.change')"
                        :submit-disabled="taglineForm === user.tagline"
                        :submit="changeTagline"
                        @open="taglineForm = user.tagline"
                    >
                        <template #activator="{ on, attrs }">
                            <v-btn icon small color="warning" v-bind="attrs" v-on="on">
                                <v-icon small>mdi-pencil</v-icon>
                            </v-btn>
                        </template>
                        <v-text-field
                            v-model.trim="taglineForm"
                            :counter="validations.userTagline.max"
                            :label="$t('author.taglineLabel')"
                            :rules="[$util.$vc.require($t('author.taglineLabel')), $util.$vc.maxLength(validations.userTagline.max)]"
                        />
                        <template #other-btns>
                            <v-btn color="info" text :loading="loading.resetTagline" :disabled="!user.tagline" @click.stop="resetTagline">
                                {{ $t('general.reset') }}
                            </v-btn>
                        </template>
                    </HangarModal>
                </div>
                <v-tooltip v-for="btn in buttons" :key="btn.name" bottom>
                    <template #activator="{ on }">
                        <v-btn icon small :href="btn.external ? btn.url : undefined" :to="btn.external ? undefined : btn.url" :nuxt="!btn.external" v-on="on">
                            <v-icon small>{{ btn.icon }}</v-icon>
                        </v-btn>
                    </template>
                    <span>{{ $t(`author.tooltips.${btn.name}`) }}</span>
                </v-tooltip>
            </v-col>
            <v-spacer />
            <v-col cols="auto">
                <v-sheet rounded color="lighten-1" class="text--secondary px-3 py-2 text-right">
                    <span>{{ $tc('author.numProjects', user.projectCount, [user.projectCount]) }}</span>
                    <br />
                    <span>{{ $t('author.memberSince', [$util.prettyDate(user.joinDate)]) }}</span>
                    <br />
                    <span v-for="role in user.roles" :key="role.roleId" :style="{ backgroundColor: role.color }" class="user-role-badge">{{ role.title }}</span>
                </v-sheet>
            </v-col>
        </v-row>
        <v-divider class="my-3" />
        <NuxtChild :user="user" :organization="organization" />
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { User } from 'hangar-api';
import { Organization } from 'hangar-internal';
import UserAvatar from '../components/users/UserAvatar.vue';
import HangarModal from '~/components/modals/HangarModal.vue';
import { UserPage } from '~/components/mixins';

interface Button {
    icon: string;
    action?: Function;
    external?: boolean;
    url: string;
    name: string;
}

@Component({
    components: { HangarModal, UserAvatar },
})
export default class UserParentPage extends UserPage {
    taglineForm: string | null = null;
    loading = {
        resetTagline: false,
    };

    get buttons(): Button[] {
        const buttons = [] as Button[];
        if (this.isCurrentUser && !this.user.isOrganization) {
            buttons.push({ icon: 'mdi-cog', url: `${process.env.authHost}/accounts/settings`, external: true, name: 'settings' });
            buttons.push({ icon: 'mdi-lock-open-outline', url: '', name: 'lock' });
            buttons.push({ icon: 'mdi-lock-outline', url: '', name: 'unlock' });
            buttons.push({ icon: 'mdi-key', url: '/' + this.user.name + '/settings/api-keys', name: 'apiKeys' });
        }
        if (this.$perms.canAccessModNotesAndFlags || this.$perms.isReviewer) {
            buttons.push({ icon: 'mdi-calendar', url: '', name: 'activity' });
        }
        if (this.$perms.canEditAllUserSettings) {
            buttons.push({ icon: 'mdi-wrench', url: '/admin/user/' + this.user.name, name: 'admin' });
        }

        return buttons;
    }

    get canEditCurrentUser() {
        return this.$perms.canEditAllUserSettings || this.isCurrentUser || this.$perms.canEditSubjectSettings;
    }

    get avatarClazz(): String {
        return 'user-avatar-md' + (this.user.isOrganization ? ' organization-avatar' : '');
    }

    changeTagline() {
        return this.$api
            .requestInternal(`${this.user.isOrganization ? 'organizations/org' : 'users'}/${this.user.name}/settings/tagline`, true, 'post', {
                content: this.taglineForm,
            })
            .then(() => {
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError);
    }

    $refs!: {
        taglineModal: HangarModal;
    };

    resetTagline() {
        this.loading.resetTagline = true;
        this.$api
            .requestInternal(`users/${this.user.name}/settings/resetTagline`, true, 'post')
            .then(() => {
                this.$refs.taglineModal.close();
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading.resetTagline = false;
            });
    }

    async asyncData({ $api, $util, params }: Context) {
        const user = await $api.request<User>(`users/${params.user}`, false).catch<any>($util.handlePageRequestError);
        if (typeof user === 'undefined') return;
        let org: Organization | null = null;
        if (user.isOrganization) {
            org = await $api.requestInternal(`organizations/org/${params.user}`, false).catch<any>($util.handlePageRequestError);
        }
        return { user, organization: org };
    }
}
</script>

<style lang="scss" scoped></style>
