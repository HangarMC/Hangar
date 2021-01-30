<template>
    <div>
        <v-row>
            <v-col cols="1">
                <UserAvatar :username="author.name" :avatar-url="$util.avatarUrl(author.name)" :clazz="avatarClazz"></UserAvatar>
            </v-col>
            <v-col cols="auto">
                <h1 class="d-inline">{{ author.name }}</h1>
                <v-list dense flat class="d-inline-flex">
                    <v-list-item v-for="btn in buttons" :key="btn.name">
                        <v-list-item-content>
                            <v-btn icon
                                ><v-icon>{{ btn.icon }}</v-icon></v-btn
                            >
                        </v-list-item-content>
                    </v-list-item>
                </v-list>
                <div>
                    <v-subheader>
                        <template v-if="author.tagline">{{ author.tagline }}</template>
                        <!-- TODO tagline edit -->
                        <!--<template v-else-if="u.isCurrent() || canEditOrgSettings(u, o, so)">Add a tagline</template>-->
                        <v-btn icon>
                            <v-icon>mdi-pencil</v-icon>
                        </v-btn>
                    </v-subheader>
                </div>
            </v-col>
            <v-spacer />
            <v-col cols="2">
                <v-subheader>{{ author.projectCount }} project(s)</v-subheader>
                <v-subheader>A member since {{ $util.prettyDate(author.joinDate) }}</v-subheader>
                <a :href="$util.forumUrl(author.name)">View on forums <v-icon>mdi-open-in-new</v-icon></a>
            </v-col>
        </v-row>
        <v-divider />
        <v-row>
            <v-col cols="12" md="8">
                <ProjectList :projects="projects" />
            </v-col>
            <v-col cols="12" md="4">
                <v-list dense>
                    <v-subheader>Organizations</v-subheader>
                    <!-- todo display orgs -->
                </v-list>
                <v-list dense>
                    <v-subheader>Stars</v-subheader>
                    <!-- todo display stars -->
                </v-list>
                <v-list dense>
                    <v-subheader>Watching</v-subheader>
                    <!-- todo watching -->
                </v-list>
            </v-col>
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Color, Project, Role } from 'hangar-api';

interface Author {
    fullName: String;
    name: String;
    tagline: String;
    joinDate: Date;
    isLocked: boolean;
    role: Role;
    projectCount: Number;
}

interface Button {
    icon: String;
    action?: Function;
    url: String;
    name: String;
}

@Component
export default class AuthorPage extends Vue {
    // TODO load author
    author: Author = {
        fullName: 'Martin',
        name: 'MiniDigger',
        tagline: 'This is a test',
        joinDate: new Date(),
        isLocked: false,
        role: { title: 'Test', color: { hex: '#ffffff' } as Color } as Role,
        projectCount: 2,
    };

    projects: Project[] = [];

    get avatarClazz(): String {
        return 'user-avatar-md';
        // todo check org an add 'organization-avatar'
    }

    get buttons(): Button[] {
        const buttons = [] as Button[];
        // TODO user admin
        buttons.push({ icon: 'mdi-cog', url: '', name: 'Settings' });
        buttons.push({ icon: 'mdi-lock-open-outline', url: '', name: 'Lock Account' });
        buttons.push({ icon: 'mdi-lock-outline', url: '', name: 'Unlock Account' });
        buttons.push({ icon: 'mdi-key', url: '', name: 'API Keys' });
        buttons.push({ icon: 'mdi-calendar', url: '', name: 'Activity' });
        buttons.push({ icon: 'mdi-wrench', url: '', name: 'User Admin' });
        return buttons;
    }
}
</script>

<style lang="scss" scoped></style>
