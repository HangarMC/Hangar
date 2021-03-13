<template>
    <v-app-bar fixed app>
        <v-menu bottom offset-y open-on-hover transition="slide-y-transition" close-delay="100">
            <template #activator="{ on, attrs }">
                <v-btn text x-large class="align-self-center px-1" v-bind="attrs" :ripple="false" v-on="on">
                    <NuxtLink class="float-left" to="/" exact>
                        <v-img height="55" width="220" src="https://papermc.io/images/logo-marker.svg" alt="Paper logo" />
                    </NuxtLink>

                    <v-icon>mdi-chevron-down</v-icon>
                </v-btn>
            </template>
            <Dropdown :controls="dropdown" />
        </v-menu>

        <v-spacer></v-spacer>

        <v-menu v-if="$util.isLoggedIn()" bottom offset-y transition="slide-y-transition" open-on-hover>
            <template #activator="{ on, attrs }">
                <v-btn v-bind="attrs" color="primary" class="mr-1" v-on="on">
                    {{ $t('nav.createNew') }}
                    <v-icon right>mdi-chevron-down</v-icon>
                </v-btn>
            </template>
            <Dropdown :controls="newControls" />
        </v-menu>

        <v-tooltip bottom>
            <template #activator="{ on }">
                <v-btn icon to="/authors" nuxt class="mr-1" v-on="on"><v-icon>mdi-account-group</v-icon></v-btn>
            </template>
            <span>{{ $t('pages.authors') }}</span>
        </v-tooltip>
        <v-tooltip bottom>
            <template #activator="{ on }">
                <v-btn icon to="/staff" nuxt class="mr-1" v-on="on"><v-icon>mdi-account-tie</v-icon></v-btn>
            </template>
            <span>{{ $t('pages.staff') }}</span>
        </v-tooltip>

        <v-menu v-if="$util.isLoggedIn()" bottom offset-y transition="slide-y-transition" close-delay="100">
            <template #activator="{ on, attrs }">
                <v-btn color="info" text class="px-3 text-transform-unset" x-large v-bind="attrs" v-on="on">
                    {{ $store.state.auth.user.name }}
                    <v-badge
                        overlap
                        :content="$store.state.auth.user.headerData.unreadNotifications"
                        :value="$store.state.auth.user.headerData.unreadNotifications"
                    >
                        <v-avatar size="44" class="ml-2">
                            <img
                                :src="$util.avatarUrl($store.state.auth.user.name)"
                                :alt="$store.state.auth.user.name"
                                @error="$event.target.src = 'https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png'"
                            />
                        </v-avatar>
                    </v-badge>
                </v-btn>
            </template>
            <Dropdown :controls="userControls" />
        </v-menu>
        <template v-else>
            <v-btn href="/signup" class="mr-2" color="primary">{{ $t('nav.signup') }}</v-btn>
            <v-btn color="secondary" @click="$auth.login($route.fullPath)">{{ $t('nav.login') }}</v-btn>
        </template>
    </v-app-bar>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import Dropdown, { Control } from '~/components/layouts/Dropdown.vue';

@Component({
    components: {
        Dropdown,
    },
})
export default class Header extends Vue {
    get dropdown(): Control[] {
        const controls: Control[] = [];
        controls.push({
            link: 'https://www.papermc.io',
            icon: 'mdi-home',
            title: this.$t('nav.hangar.home'),
        });
        controls.push({
            link: 'https://papermc.io/forums',
            icon: 'mdi-comment-multiple',
            title: this.$t('nav.hangar.forums'),
        });
        controls.push({
            link: 'https://github.com/PaperMC',
            icon: 'mdi-code-braces',
            title: this.$t('nav.hangar.code'),
        });
        controls.push({
            link: 'https://paper.readthedocs.io',
            icon: 'mdi-book',
            title: this.$t('nav.hangar.docs'),
        });
        controls.push({
            link: 'https://papermc.io/javadocs',
            icon: 'mdi-school',
            title: this.$t('nav.hangar.javadocs'),
        });
        controls.push({
            link: '/',
            icon: 'mdi-home',
            title: this.$t('nav.hangar.hangar'),
        });
        controls.push({
            link: 'https://papermc.io/downloads',
            icon: 'mdi-download',
            title: this.$t('nav.hangar.downloads'),
        });
        controls.push({
            link: 'https://papermc.io/community',
            icon: 'mdi-comment',
            title: this.$t('nav.hangar.community'),
        });
        return controls;
    }

    get newControls(): Control[] {
        const controls: Control[] = [];
        controls.push({
            link: '/new',
            icon: 'mdi-book',
            title: this.$t('nav.new.project'),
        });
        controls.push({
            link: '/organizations/new',
            icon: 'mdi-account-group',
            title: this.$t('nav.new.organization'),
        });
        return controls;
    }

    get userControls(): Control[] {
        const controls: Control[] = [];
        controls.push({
            link: '/' + this.$store.state.auth.user.name,
            icon: 'mdi-account',
            title: this.$store.state.auth.user.name,
        });
        controls.push({
            link: '/notifications',
            icon: 'mdi-bell',
            title: this.$t('nav.user.notifications'),
        });
        controls.push({
            isDivider: true,
        });
        if (this.$perms.canAccessModNotesAndFlags) {
            controls.push({
                link: '/admin/flags',
                icon: 'mdi-flag',
                title: this.$t('nav.user.flags'),
            });
            controls.push({
                link: '/admin/approval/projects',
                icon: 'mdi-thumb-up',
                title: this.$t('nav.user.projectApprovals'),
            });
        }
        if (this.$perms.isReviewer) {
            controls.push({
                link: '/admin/approval/versions',
                icon: 'mdi-thumb-up-outline',
                title: this.$t('nav.user.versionApprovals'),
            });
        }
        if (this.$perms.canViewStats) {
            controls.push({
                link: '/admin/stats',
                icon: 'mdi-chart-line',
                title: this.$t('nav.user.stats'),
            });
        }
        if (this.$perms.canViewHealth) {
            controls.push({
                link: '/admin/health',
                icon: 'mdi-heart-plus',
                title: this.$t('nav.user.health'),
            });
        }
        if (this.$perms.canViewLogs) {
            controls.push({
                link: '/admin/log',
                icon: 'mdi-format-list-bulleted',
                title: this.$t('nav.user.log'),
            });
        }
        if (this.$perms.canDoManualValueChanges) {
            controls.push({
                link: '/admin/versions',
                icon: 'mdi-tag-multiple',
                title: this.$t('nav.user.platformVersions'),
            });
        }
        if (controls.length > 3) {
            controls.push({
                isDivider: true,
            });
        }
        controls.push({
            action: () => this.$auth.logout(),
            icon: 'mdi-logout',
            title: this.$t('nav.user.logout'),
        });
        return controls;
    }
}
</script>

<style lang="scss">
.v-badge--bordered.header-badge .v-badge__badge::after {
    border-color: #272727 !important;
}
</style>
<style lang="scss">
.avatar-button {
    //border: 2px white solid;
}
</style>
