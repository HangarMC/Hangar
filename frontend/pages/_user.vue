<template>
    <div>
        <v-row>
            <v-col class="flex-grow-0">
                <div style="position: relative" class="mt-2">
                    <UserAvatar :username="user.name" :avatar-url="$util.avatarUrl(user.name)" :clazz="avatarClazz">
                        <Prompt v-if="user.isOrganization" prompt="CHANGE_AVATAR">
                            <template #activator>
                                <v-tooltip left>
                                    <template #activator="{ on }">
                                        <v-btn fab x-small color="warning" absolute style="right: -16px; top: -16px" v-on="on" @click.stop="changeAvatar">
                                            <v-icon>mdi-pencil</v-icon>
                                        </v-btn>
                                    </template>
                                    <span>{{ $t('author.org.editAvatar') }}</span>
                                </v-tooltip>
                            </template>
                        </Prompt>
                    </UserAvatar>
                </div>
            </v-col>
            <v-col>
                <h1>
                    {{ user.name }}
                    <v-tooltip v-if="!user.isOrganization" bottom>
                        <template #activator="{ on }">
                            <v-btn icon small color="info" class="ml-n2" :href="$util.forumUrl(user.name)" v-on="on">
                                <v-icon small> mdi-open-in-new </v-icon>
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
                                <v-icon small> mdi-pencil </v-icon>
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
                            <v-icon small>
                                {{ btn.icon }}
                            </v-icon>
                        </v-btn>
                    </template>
                    <span>{{ $t(`author.tooltips.${btn.name}`) }}</span>
                </v-tooltip>
                <ConfirmModal
                    v-if="!isCurrentUser && !user.isOrganization && $perms.isStaff"
                    :title="$t(`author.lock.confirm${user.locked ? 'Unlock' : 'Lock'}`, [user.name])"
                    :submit="toggleUserLock"
                    comment
                >
                    <template #activator="{ on: onModal, attrs }">
                        <v-tooltip bottom>
                            <template #activator="{ on: onTooltip }">
                                <v-btn icon small v-bind="attrs" v-on="{ ...onModal, ...onTooltip }">
                                    <v-icon small :color="user.locked ? 'success' : 'error'">
                                        {{ user.locked ? 'mdi-lock-open-outline' : 'mdi-lock-outline' }}
                                    </v-icon>
                                </v-btn>
                            </template>
                            <span>{{ $t(`author.tooltips.${user.locked ? 'unlock' : 'lock'}`) }}</span>
                        </v-tooltip>
                    </template>
                </ConfirmModal>
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
import Prompt from '~/components/users/Prompt.vue';
import ConfirmModal from '~/components/modals/ConfirmModal.vue';

interface Button {
    icon: string;
    action?: Function;
    external?: boolean;
    url?: string;
    name: string;
}

@Component({
    components: { ConfirmModal, Prompt, HangarModal, UserAvatar },
})
export default class UserParentPage extends UserPage {
    taglineForm: string | null = null;
    loading = {
        resetTagline: false,
    };

    get buttons(): Button[] {
        const buttons = [] as Button[];
        if (!this.user.isOrganization) {
            if (this.isCurrentUser) {
                buttons.push({ icon: 'mdi-cog', url: `${process.env.authHost}/accounts/settings`, external: true, name: 'settings' });
            }
            if (this.isCurrentUser || this.$perms.canEditAllUserSettings) {
                buttons.push({ icon: 'mdi-key', url: '/' + this.user.name + '/settings/api-keys', name: 'apiKeys' });
            }
        }
        if ((this.$perms.canAccessModNotesAndFlags || this.$perms.isReviewer) && !this.user.isOrganization) {
            buttons.push({ icon: 'mdi-calendar', url: `/admin/activities/${this.user.name}`, name: 'activity' });
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

    changeAvatar() {
        if (!this.user.isOrganization) {
            return;
        }

        this.$api
            .requestInternal<string>(`organizations/org/${this.user.name}/settings/avatar`)
            .then((uri) => {
                window.location.assign(uri);
            })
            .catch<any>(this.$util.handleRequestError);
    }

    toggleUserLock(comment: string) {
        return this.$api
            .requestInternal(`admin/lock-user/${this.user.name}/${!this.user.locked}`, true, 'post', {
                content: comment,
            })
            .then(() => {
                this.$nuxt.refresh();
                this.$util.success(this.$t(`author.lock.success${this.user.locked ? 'Unlock' : 'Lock'}`, [this.user.name]));
            })
            .catch(this.$util.handleRequestError);
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
