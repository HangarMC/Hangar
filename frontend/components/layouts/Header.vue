<template>
    <v-app-bar class="navbar" height="65px" elevate-on-scroll fixed>
        <v-menu bottom offset-y open-on-hover transition="slide-y-transition" close-delay="100">
            <template #activator="{ on, attrs }">
                <v-app-bar-nav-icon class="drawer" v-bind="attrs" v-on="on"> </v-app-bar-nav-icon>
            </template>
            <Dropdown :controls="dropdown" />
        </v-menu>

        <v-btn text x-large class="align-self-center px-1" style="margin-left: 0" :ripple="false">
            <NuxtLink class="float-left" to="/" exact>
                <v-img
                    v-if="!$vuetify.theme.dark"
                    class="site-logo"
                    style="margin-left: 0"
                    height="60"
                    width="60"
                    src="/images/hangar-icon-2.svg"
                    alt="Paper logo"
                />
                <v-img v-else class="site-logo" style="margin-left: 0" height="60" width="60" src="images/hangar-icon-2.svg" alt="Paper logo" />
            </NuxtLink>
        </v-btn>

        <div class="navbar-links hideOnMobile">
            <NuxtLink class="float-left" to="/" exact>
                <span>Home</span>
            </NuxtLink>
            <NuxtLink v-if="isLoggedIn" class="float-left" :to="'/' + currentUser.name" exact>
                <span>Your Projects</span>
            </NuxtLink>
            <NuxtLink class="float-left" to="/" exact>
                <span>Top 10</span>
            </NuxtLink>
            <NuxtLink class="float-left" to="/staff" exact>
                <span>Team</span>
            </NuxtLink>
            <a class="float-left" href="https://discord.gg/papermc">
                <span>Discord</span>
            </a>
        </div>

        <v-spacer />

        <v-menu v-if="isLoggedIn" bottom offset-y transition="slide-y-transition" open-on-hover>
            <template #activator="{ on, attrs }">
                <v-btn v-bind="attrs" color="primary" class="mr-1 createNewButton" v-on="on">
                    {{ $t('nav.createNew') }}
                    <v-icon right> mdi-chevron-down </v-icon>
                </v-btn>
            </template>
            <Dropdown :controls="newControls" />
        </v-menu>

        <div>
            <v-tooltip v-if="!$vuetify.theme.dark" bottom>
                <template #activator="{ on }">
                    <v-btn class="mr-1 darkModeSwitchButton" v-on="on" @click="darkMode">
                        <v-icon>mdi-weather-night</v-icon>
                    </v-btn>
                </template>
                <span>{{ $t('nav.darkModeOn') }}</span>
            </v-tooltip>

            <v-tooltip v-else bottom>
                <template #activator="{ on }">
                    <v-btn class="mr-1 darkModeSwitchButton" v-on="on" @click="darkMode">
                        <v-icon color="yellow">mdi-white-balance-sunny</v-icon>
                    </v-btn>
                </template>
                <span>{{ $t('nav.darkModeOff') }}</span>
            </v-tooltip>
        </div>
        <div class="hideOnMobile">
            <v-tooltip bottom>
                <template #activator="{ on }">
                    <v-btn icon to="/authors" nuxt class="mr-1" v-on="on">
                        <v-icon>mdi-account-group</v-icon>
                    </v-btn>
                </template>
                <span>{{ $t('pages.authorsTitle') }}</span>
            </v-tooltip>
            <v-tooltip bottom>
                <template #activator="{ on }">
                    <v-btn icon to="/staff" nuxt class="mr-1" v-on="on">
                        <v-icon>mdi-account-tie</v-icon>
                    </v-btn>
                </template>
                <span>{{ $t('pages.staffTitle') }}</span>
            </v-tooltip>
        </div>

        <v-menu v-if="isLoggedIn" bottom offset-y transition="slide-y-transition" close-delay="100">
            <template #activator="{ on, attrs }">
                <v-btn color="info" text class="text-transform-unset iHateVuetify" x-large v-bind="attrs" v-on="on">
                    <span class="hideOnMobile mr-2">{{ currentUser.name }}</span>
                    <v-badge overlap :content="totalNotificationCount" :value="totalNotificationCount">
                        <v-avatar size="44">
                            <img
                                :src="$util.avatarUrl(currentUser.name)"
                                :alt="currentUser.name"
                                @error="$event.target.src = 'https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png'"
                            />
                        </v-avatar>
                    </v-badge>
                </v-btn>
            </template>
            <Dropdown :controls="userControls" />
        </v-menu>
        <template v-else>
            <v-btn href="/signup" class="mr-2" color="primary">
                {{ $t('nav.signup') }}
            </v-btn>
            <v-btn color="secondary" :href="$auth.loginUrl($route.fullPath)">
                {{ $t('nav.login') }}
            </v-btn>
        </template>
    </v-app-bar>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import Dropdown, { Control } from '~/components/layouts/Dropdown.vue';
import { HangarComponent } from '~/components/mixins';
import UserAvatar from '~/components/users/UserAvatar.vue';

@Component({
    components: {
        UserAvatar,
        Dropdown,
    },
})
export default class Header extends HangarComponent {
    darkMode() {
        if (process.browser) {
            if (this.$vuetify.theme.dark) {
                this.$vuetify.theme.dark = false;
                localStorage.setItem('DarkMode', 'false');
            } else if (!this.$vuetify.theme.dark) {
                this.$vuetify.theme.dark = true;
                localStorage.setItem('DarkMode', 'true');
            }
        }
    }

    get dropdown(): Control[] {
        const controls: Control[] = [];
        if (process.browser && window.innerWidth < 816) {
            controls.push({
                link: '/authors',
                icon: 'mdi-account-group',
                title: this.$t('pages.authorsTitle'),
            });
            controls.push({
                link: '/staff',
                icon: 'mdi-account-tie',
                title: this.$t('pages.staffTitle'),
            });
            controls.push({
                link: '',
                icon: '',
                title: '',
            });
        }
        controls.push({
            link: 'https://papermc.io/',
            icon: 'mdi-home',
            title: this.$t('nav.hangar.home'),
        });
        controls.push({
            link: 'https://forums.papermc.io/',
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
            icon: 'mdi-puzzle',
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
        controls.push({
            link: process.env.authHost,
            icon: 'mdi-key',
            title: this.$t('nav.hangar.auth'),
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
        if (this.currentUser!.headerData.organizationCount - 1 < this.validations.maxOrgCount) {
            controls.push({
                link: '/organizations/new',
                icon: 'mdi-account-group',
                title: this.$t('nav.new.organization'),
            });
        }
        return controls;
    }

    get userControls(): Control[] {
        const controls: Control[] = [];
        controls.push({
            link: `/${this.currentUser!.name}`,
            icon: 'mdi-account',
            title: this.currentUser!.name,
        });
        controls.push({
            link: '/notifications',
            icon: 'mdi-bell',
            title: this.$t('nav.user.notifications'),
            badge: true,
            badgeContent: this.currentUser!.headerData.unansweredInvites + this.currentUser!.headerData.unreadNotifications,
        });
        controls.push({
            isDivider: true,
        });
        if (this.$perms.canAccessModNotesAndFlags) {
            controls.push({
                link: '/admin/flags',
                icon: 'mdi-flag',
                title: this.$t('nav.user.flags'),
                badge: true,
                badgeContent: this.currentUser!.headerData.unresolvedFlags,
            });
            controls.push({
                link: '/admin/approval/projects',
                icon: 'mdi-thumb-up',
                title: this.$t('nav.user.projectApprovals'),
                badge: true,
                badgeContent: this.currentUser!.headerData.projectApprovals,
            });
        }
        if (this.$perms.isReviewer) {
            controls.push({
                link: '/admin/approval/versions',
                icon: 'mdi-thumb-up-outline',
                title: this.$t('nav.user.versionApprovals'),
                badge: true,
                badgeContent: this.currentUser!.headerData.reviewQueueCount,
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

    get totalNotificationCount() {
        return (
            this.currentUser!.headerData.unreadNotifications +
            this.currentUser!.headerData.unansweredInvites +
            this.currentUser!.headerData.unresolvedFlags +
            this.currentUser!.headerData.reviewQueueCount +
            this.currentUser!.headerData.projectApprovals
        );
    }
}
</script>

<style lang="scss">
.v-badge--bordered.header-badge .v-badge__badge::after {
    border-color: #272727 !important;
}
.v-image {
    margin: 20px;
}

@media (min-width: 816px) {
    .hideOnMobile {
        display: flex;
        align-items: center;
    }
}
@media (max-width: 815px) {
    .hideOnMobile {
        display: none;
    }
}

.navbar {
    display: flex !important;
    justify-content: center;
    z-index: 200 !important;
}

.theme--light .navbar {
    background-color: #fff !important;
}

.navbar .v-toolbar__content {
    max-width: 1200px;
    width: calc(100% - 40px);
    padding: 0;
    display: flex;
    align-items: center;
    position: relative;
}

.navbar-links a {
    text-transform: uppercase;
    padding: 0 16px;
    font-size: 13px;
    color: #262626 !important;
}
.theme--dark .navbar-links a {
    color: #fff !important;
}

.v-btn.v-btn--has-bg.darkModeSwitchButton {
    background-color: transparent !important;
    box-shadow: none;
    padding: 0 4px;
    min-width: 32px;
}

.createNewButton {
    padding: 0 10px !important;
}

.iHateVuetify {
    min-width: 0 !important;
    padding: 0 !important;
}

.navbar-links a:after {
    content: '';
    width: 0;
    top: 25px;
    left: 0;
    height: 4px;
    border-radius: 8px;
}

.navbar-links a {
    position: relative;
}

.navbar-links a:hover:after {
    position: absolute;
    background: #d3e1f6;
    transition: ease-in width 0.2s;
    width: 100%;
}

.site-logo {
    margin: 0 !important;
}
</style>
