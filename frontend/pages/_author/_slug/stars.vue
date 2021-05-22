<template>
    <v-card>
        <v-card-title>
            {{ $t('project.stargazers') }}
        </v-card-title>
        <v-card-text>
            <v-row v-if="stargazers.result && stargazers.result.length > 0">
                <v-col v-for="stargazer in stargazers.result" :key="stargazer.name" md="4">
                    <UserAvatar clazz="user-avatar-xs" :username="stargazer.name" :avatar-url="$util.avatarUrl(stargazer.name)"></UserAvatar>
                    {{ stargazer.name }}
                </v-col>
            </v-row>
            <v-alert v-else type="info" prominent>
                {{ $t('project.noStargazers') }}
            </v-alert>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { PaginatedResult, User } from 'hangar-api';
import { Context } from '@nuxt/types';
import { UserAvatar } from '~/components/users';
import { HangarProjectMixin } from '~/components/mixins';

@Component({
    components: { UserAvatar },
})
export default class ProjectStarsPage extends HangarProjectMixin {
    stargazers!: PaginatedResult<User>;

    head() {
        return this.$seo.head(
            this.$t('project.stargazers') + ' | ' + this.project.name,
            this.project.description,
            this.$route,
            this.$util.projectUrl(this.project.namespace.owner, this.project.namespace.slug)
        );
    }

    async asyncData({ $api, params, $util }: Context) {
        const stargazers = await $api
            .request<PaginatedResult<User>>(`projects/${params.author}/${params.slug}/stargazers`, false)
            .catch<any>($util.handlePageRequestError);
        return { stargazers };
    }
}
</script>

<style lang="scss" scoped></style>
