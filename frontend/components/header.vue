<template>
    <v-app-bar fixed app>
        <v-menu bottom offset-y open-on-hover transition="slide-y-transition" close-delay="100">
            <template #activator="{ on, attrs }">
                <v-toolbar-title v-bind="attrs" v-on="on">
                    <NuxtLink to="/" :exact="true">
                        <img src="https://papermc.io/images/logo-marker.svg" alt="Paper logo" />
                    </NuxtLink>
                </v-toolbar-title>
                <v-app-bar-nav-icon :plain="false" v-bind="attrs" v-on="on">
                    <v-icon color="white" class="dropdown-icon">mdi-chevron-down</v-icon>
                </v-app-bar-nav-icon>
            </template>
            <dropdown :controls="dropdown" />
        </v-menu>

        <v-spacer></v-spacer>

        <v-btn icon to="/authors"><v-icon color="white">mdi-account-group</v-icon></v-btn>
        <v-btn icon to="/staff"><v-icon color="white">mdi-account-tie</v-icon></v-btn>

        <template v-if="$store.state.auth.user">
            <v-menu bottom offset-y transition="slide-y-transition">
                <template #activator="{ on, attrs }">
                    <v-btn v-bind="attrs" v-on="on">
                        <v-icon color="white" class="dropdown-icon">mdi-plus</v-icon>
                        <v-icon small color="white" class="dropdown-icon">mdi-chevron-down</v-icon>
                    </v-btn>
                </template>
                <dropdown :controls="newControls" />
            </v-menu>

            <v-menu bottom offset-y transition="slide-y-transition">
                <template #activator="{ on, attrs }">
                    <v-btn v-bind="attrs" v-on="on">
                        <v-icon color="white" class="dropdown-icon">mdi-account-circle</v-icon>
                        <v-icon small color="white" class="dropdown-icon">mdi-chevron-down</v-icon>
                    </v-btn>
                </template>
                <dropdown :controls="userControls" />
            </v-menu>
        </template>
        <template v-else>
            <v-btn to="/signUp" class="mr-2" color="primary">Sign up</v-btn>
            <v-btn color="secondary" @click="$auth.login($route.fullPath)">Log In</v-btn>
        </template>
    </v-app-bar>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Control } from '~/components/dropdown.vue';

@Component
export default class Header extends Vue {
    get dropdown(): Control[] {
        const controls: Control[] = [];
        controls.push({
            link: 'https://www.papermc.io',
            icon: 'mdi-home',
            title: 'Homepage',
        });
        controls.push({
            link: 'https://papermc.io/forums',
            icon: 'mdi-comment-multiple',
            title: 'Forums',
        });
        controls.push({
            link: 'https://github.com/PaperMC',
            icon: 'mdi-code-braces',
            title: 'Code',
        });
        controls.push({
            link: 'https://paper.readthedocs.io',
            icon: 'mdi-book',
            title: 'Docs',
        });
        controls.push({
            link: 'https://papermc.io/javadocs',
            icon: 'mdi-school',
            title: 'JavaDocs',
        });
        controls.push({
            link: '/',
            icon: 'mdi-home',
            title: 'Hangar (Plugins)',
        });
        controls.push({
            link: 'https://papermc.io/downloads',
            icon: 'mdi-download',
            title: 'Downloads',
        });
        controls.push({
            link: 'https://papermc.io/community',
            icon: 'mdi-comment',
            title: 'Community',
        });
        return controls;
    }

    get newControls(): Control[] {
        const controls: Control[] = [];
        controls.push({
            link: '/new',
            icon: 'mdi-book',
            title: 'New Project',
        });
        controls.push({
            link: '/organizations/new',
            icon: 'mdi-account-group',
            title: 'New Organization',
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
            title: 'Notifications',
        });
        // TODO check perms
        controls.push({
            link: '/admin/flags',
            icon: 'mdi-flag',
            title: 'Flags',
        });
        controls.push({
            link: '/admin/approval/projects',
            icon: 'mdi-thumb-up',
            title: 'Project approvals',
        });
        controls.push({
            link: '/admin/approval/versions',
            icon: 'mdi-thumb-up-outline',
            title: 'Version approvals',
        });
        controls.push({
            link: '/admin/stats',
            icon: 'mdi-chart-line',
            title: 'Stats',
        });
        controls.push({
            link: '/admin/health',
            icon: 'mdi-heart-plus',
            title: 'Hangar Health',
        });
        controls.push({
            link: '/admin/log',
            icon: 'mdi-format-list-bulleted',
            title: 'User Action Log',
        });
        controls.push({
            link: '/admin/versions',
            icon: 'mdi-tag-multiple',
            title: 'Platform Versions',
        });
        controls.push({
            action: () => this.$auth.logout(),
            icon: 'mdi-logout',
            title: 'Sign out',
        });
        return controls;
    }
}
</script>

<style lang="scss" scoped>
img {
    height: 40px;
}
</style>
