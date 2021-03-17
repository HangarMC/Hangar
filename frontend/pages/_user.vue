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
                        <template v-if="user.tagline">{{ user.tagline }}</template>
                        <!-- TODO tagline edit -->
                        <!--<template v-else-if="u.isCurrent() || canEditOrgSettings(u, o, so)">{{ $t('author.addTagline') }}</template>-->
                        <v-btn icon>
                            <v-icon>mdi-pencil</v-icon>
                        </v-btn>
                    </v-subheader>
                </div>
            </v-col>
            <v-spacer />
            <v-col cols="2">
                <v-subheader>{{ $tc('author.numProjects', user.projectCount, [user.projectCount]) }}</v-subheader>
                <v-subheader>{{ $t('author.memberSince', [$util.prettyDate(user.joinDate)]) }}</v-subheader>
                <a :href="$util.forumUrl(user.name)">{{ $t('author.viewOnForums') }}<v-icon>mdi-open-in-new</v-icon></a>
            </v-col>
        </v-row>
        <v-divider />
        <NuxtChild :user="user" />
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { HangarUser } from 'hangar-internal';
import { Context } from '@nuxt/types';
import UserAvatar from '../components/UserAvatar.vue';

interface Button {
    icon: string;
    action?: Function;
    external?: boolean;
    url: string;
    name: string;
}

@Component({
    components: { UserAvatar },
})
export default class UserParentPage extends Vue {
    user!: HangarUser;

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

    get avatarClazz(): String {
        return 'user-avatar-md';
        // todo check org an add 'organization-avatar'
    }

    async asyncData({ $api, $util, params }: Context) {
        const user = await $api.requestInternal<HangarUser>(`users/${params.user}`, false).catch<any>($util.handlePageRequestError);
        if (typeof user === 'undefined') return;
        return { user };
    }
}
</script>

<style lang="scss" scoped></style>
