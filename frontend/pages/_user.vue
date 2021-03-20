<template>
    <div>
        <v-row>
            <v-col cols="1">
                <UserAvatar :username="user.name" :avatar-url="$util.avatarUrl(user.name)" :clazz="avatarClazz"></UserAvatar>
            </v-col>
            <v-col cols="auto">
                <h1 class="d-inline">{{ user.name }}</h1>
                <v-list dense flat class="d-inline-flex">
                    <v-list-item v-for="btn in buttons" :key="btn.name">
                        <v-list-item-content>
                            <v-tooltip bottom>
                                <template #activator="{ on }">
                                    <v-btn
                                        icon
                                        :href="btn.external ? btn.url : undefined"
                                        :to="btn.external ? undefined : btn.url"
                                        :nuxt="!btn.external"
                                        v-on="on"
                                    >
                                        <v-icon>{{ btn.icon }}</v-icon>
                                    </v-btn>
                                </template>
                                <span>{{ $t(`author.tooltips.${btn.name}`) }}</span>
                            </v-tooltip>
                        </v-list-item-content>
                    </v-list-item>
                </v-list>
                <div>
                    <v-subheader>
                        <span v-if="user.tagline">{{ user.tagline }}</span>
                        <span v-else-if="canEditCurrent">{{ $t('author.addTagline') }}</span>
                        <HangarModal
                            v-if="canEditCurrent"
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
                                counter="100"
                                :label="$t('author.taglineLabel')"
                                :rules="[$util.$vc.require($t('author.taglineLabel')), $util.$vc.maxLength(validations.userTagline.max)]"
                            />
                            <template #other-btns>
                                <v-btn color="info" text :loading="loading.resetTagline" :disabled="!user.tagline" @click.stop="resetTagline">{{
                                    $t('general.reset')
                                }}</v-btn>
                            </template>
                        </HangarModal>
                    </v-subheader>
                </div>
            </v-col>
            <v-spacer />
            <v-col cols="2">
                <v-subheader>{{ $tc('author.numProjects', user.projectCount, [user.projectCount]) }}</v-subheader>
                <v-subheader>{{ $t('author.memberSince', [$util.prettyDate(user.joinDate)]) }}</v-subheader>
                <a :href="$util.forumUrl(user.name)">{{ $t('author.viewOnForums') }}<v-icon small>mdi-open-in-new</v-icon></a>
            </v-col>
        </v-row>
        <v-divider />
        <NuxtChild :user="user" />
    </div>
</template>

<script lang="ts">
import { Component, State, Vue } from 'nuxt-property-decorator';
import { HangarUser } from 'hangar-internal';
import { Context } from '@nuxt/types';
import UserAvatar from '../components/UserAvatar.vue';
import HangarModal from '~/components/modals/HangarModal.vue';
import { RootState } from '~/store';

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
export default class UserParentPage extends Vue {
    user!: HangarUser;
    taglineForm: string | null = null;
    loading = {
        resetTagline: false,
    };

    get buttons(): Button[] {
        const buttons = [] as Button[];
        buttons.push({ icon: 'mdi-cog', url: `${process.env.authHost}/accounts/settings`, external: true, name: 'settings' });
        buttons.push({ icon: 'mdi-lock-open-outline', url: '', name: 'lock' });
        buttons.push({ icon: 'mdi-lock-outline', url: '', name: 'unlock' });
        buttons.push({ icon: 'mdi-key', url: '/' + this.user.name + '/settings/api-keys', name: 'apiKeys' });
        buttons.push({ icon: 'mdi-calendar', url: '', name: 'activity' });
        buttons.push({ icon: 'mdi-wrench', url: '', name: 'admin' });
        return buttons;
    }

    get canEditCurrent() {
        return this.user.id === this.$store.state.auth.user.id /* || org perms */;
    }

    get avatarClazz(): String {
        return 'user-avatar-md';
        // todo check org an add 'organization-avatar'
    }

    changeTagline() {
        return this.$api
            .requestInternal(`users/${this.user.id}/settings/tagline`, true, 'post', {
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
            .requestInternal(`users/${this.user.id}/settings/resetTagline`, true, 'post')
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
        const user = await $api.requestInternal<HangarUser>(`users/${params.user}`, false).catch<any>($util.handlePageRequestError);
        if (typeof user === 'undefined') return;
        return { user };
    }

    @State((state: RootState) => state.validations)
    validations!: RootState['validations'];
}
</script>

<style lang="scss" scoped></style>
