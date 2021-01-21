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
            <v-list dense>
                <v-list-item link href="https://www.papermc.io">
                    <v-list-item-icon>
                        <v-icon color="white">mdi-home</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>Homepage</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <v-list-item link href="https://papermc.io/forums">
                    <v-list-item-icon>
                        <v-icon color="white">mdi-comment-multiple</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>Forums</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <v-list-item link href="https://github.com/PaperMC">
                    <v-list-item-icon>
                        <v-icon color="white">mdi-code-braces</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>Code</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <v-list-item link href="https://paper.readthedocs.io">
                    <v-list-item-icon>
                        <v-icon color="white">mdi-book</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>Docs</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <v-list-item link href="https://papermc.io/javadocs">
                    <v-list-item-icon>
                        <v-icon color="white">mdi-school</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>JavaDocs</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <v-list-item link to="/">
                    <v-list-item-icon>
                        <v-icon color="white">mdi-home</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>Hangar (Plugins)</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <v-list-item link href="https://papermc.io/downloads">
                    <v-list-item-icon>
                        <v-icon color="white">mdi-download</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>Downloads</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <v-list-item link href="https://papermc.io/community">
                    <v-list-item-icon>
                        <v-icon color="white">mdi-comment</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>Community</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
            </v-list>
        </v-menu>

        <v-spacer></v-spacer>

        <v-btn icon to="/authors"><v-icon color="white">mdi-account-group</v-icon></v-btn>
        <v-btn icon to="/staff"><v-icon color="white">mdi-account-tie</v-icon></v-btn>

        <template v-if="loggedIn">
            <v-menu bottom offset-y transition="slide-y-transition">
                <template #activator="{ on, attrs }">
                    <v-btn v-bind="attrs" v-on="on">
                        <v-icon color="white" class="dropdown-icon">mdi-plus</v-icon>
                        <v-icon small color="white" class="dropdown-icon">mdi-chevron-down</v-icon>
                    </v-btn>
                </template>
                <v-list dense>
                    <v-list-item link to="/new">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-book</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>New Project</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>

                    <v-list-item link to="/organizations/new">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-account-group</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>New Organization</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>
                </v-list>
            </v-menu>

            <v-menu bottom offset-y transition="slide-y-transition">
                <template #activator="{ on, attrs }">
                    <v-btn v-bind="attrs" v-on="on">
                        <v-icon color="white" class="dropdown-icon">mdi-account-circle</v-icon>
                        <v-icon small color="white" class="dropdown-icon">mdi-chevron-down</v-icon>
                    </v-btn>
                </template>
                <v-list dense>
                    <v-list-item link :to="'/' + user">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-account</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>{{ user }}</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>

                    <v-list-item link to="/notifications">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-bell</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>Notifications</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>

                    <!-- todo: check perms -->
                    <v-list-item link to="/admin/flags">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-flag</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>Flags</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>

                    <v-list-item link to="/admin/approval/projects">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-thumb-up</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>Project approvals</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>

                    <v-list-item link to="/admin/approval/versions">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-thumb-up-outline</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>Version approvals</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>

                    <v-list-item link to="/admin/stats">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-chart-line</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>Stats</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>

                    <v-list-item link to="/admin/health">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-heart-plus</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>Hangar Health</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>

                    <v-list-item link to="/admin/log">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-format-list-bulleted</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>User Action Log</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>

                    <v-list-item link to="/admin/versions">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-tag-multiple</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>Platform Versions</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>

                    <v-divider />

                    <v-list-item link to="/logout">
                        <v-list-item-icon>
                            <v-icon color="white">mdi-logout</v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            <v-list-item-title>Sign out</v-list-item-title>
                        </v-list-item-content>
                    </v-list-item>
                </v-list>
            </v-menu>
        </template>
        <template v-else>
            <v-btn>Sign up</v-btn>
            <v-btn>Log In</v-btn>
        </template>
    </v-app-bar>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

@Component
export default class NewPage extends Vue {
    loggedIn = true;
    user = "MiniDigger";
}
</script>

<style lang="scss" scoped>
img {
    height: 40px;
}
</style>
