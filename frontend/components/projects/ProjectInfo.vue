<template>
    <v-card>
        <v-card-title>
            <v-col cols="auto">
                {{ $t('project.info.title') }}
            </v-col>
            <v-col v-if="isLoggedIn && !$util.isCurrentUser(project.owner.id)" cols="auto">
                <FlagModal :project="project" activator-class="ml-1" />
            </v-col>
            <v-col v-if="$perms.isStaff" cols="auto">
                <v-menu bottom offset-y open-on-hover>
                    <template #activator="{ on, attrs }">
                        <v-btn v-bind="attrs" small class="ml-1" color="info" v-on="on">
                            {{ $t('project.actions.adminActions') }}
                        </v-btn>
                    </template>
                    <v-list>
                        <v-list-item :to="slug + '/flags'" nuxt>
                            <v-list-item-title>
                                {{ $t('project.actions.flagHistory', [project.info.flagCount]) }}
                            </v-list-item-title>
                        </v-list-item>
                        <v-list-item :to="slug + '/notes'" nuxt>
                            <v-list-item-title>
                                {{ $t('project.actions.staffNotes', [project.info.noteCount]) }}
                            </v-list-item-title>
                        </v-list-item>
                        <v-list-item :to="'/admin/log/?projectFilter=' + slug" nuxt>
                            <v-list-item-title>
                                {{ $t('project.actions.userActionLogs') }}
                            </v-list-item-title>
                        </v-list-item>
                        <v-list-item :href="$util.forumUrl(project.namespace.owner)">
                            <v-list-item-title>
                                {{ $t('project.actions.forum') }}
                                <v-icon>mdi-open-in-new</v-icon>
                            </v-list-item-title>
                        </v-list-item>
                    </v-list>
                </v-menu>
            </v-col>
            <v-col cols="auto">
                <DonationModal
                    v-if="project.settings.donation.enable"
                    :donation-email="project.settings.donation.email"
                    :default-amount="project.settings.donation.defaultAmount"
                    :one-time-amounts="project.settings.donation.oneTimeAmounts"
                    :monthly-amounts="project.settings.donation.monthlyAmounts"
                    :donation-target="project.namespace.owner + '/' + project.name"
                    :return-url="$nuxt.context.env.publicHost + '/' + project.namespace.owner + '/' + project.name + '?donation=success'"
                    :cancel-return-url="$nuxt.context.env.publicHost + '/' + project.namespace.owner + '/' + project.name + '?donation=failure'"
                >
                    <template #activator="{ on, attrs }">
                        <v-btn v-bind="attrs" v-on="on">
                            <v-icon left> mdi-currency-usd</v-icon>
                            {{ $t('general.donate') }}
                        </v-btn>
                    </template>
                </DonationModal>
            </v-col>

            <v-col cols="auto">
                <v-btn v-if="!$util.isCurrentUser(project.owner.id)" @click="toggleStar">
                    <v-icon v-if="project.userActions.starred" left color="amber"> mdi-star</v-icon>
                    <v-icon v-else left> mdi-star-outline</v-icon>
                    <span v-if="project.userActions.starred">{{ $t('project.actions.unstar') }}</span>
                    <span v-else>{{ $t('project.actions.star') }}</span>
                </v-btn>
            </v-col>

            <v-col cols="auto">
                <v-btn v-if="!$util.isCurrentUser(project.owner.id)" @click="toggleWatch">
                    <v-icon v-if="project.userActions.watching" left> mdi-eye-off</v-icon>
                    <v-icon v-else left> mdi-eye</v-icon>
                    <span v-if="project.userActions.watching">{{ $t('project.actions.unwatch') }}</span>
                    <span v-else>{{ $t('project.actions.watch') }}</span>
                </v-btn>
            </v-col>
        </v-card-title>

        <v-divider class="mx-4 mt-n4" />

        <v-card-text>
            <div class="project-info">
                <!-- TODO this should really be a table -->
                <div class="float-left">
                    <p>{{ $t('project.category.info') }}</p>
                    <p>{{ $t('project.info.publishDate') }}</p>
                    <p>{{ $tc('project.info.views', project.stats.views) }}</p>
                    <p>{{ $tc('project.info.totalDownloads', project.stats.downloads) }}</p>
                    <NuxtLink :to="`${$route.params.slug}/stars`" nuxt small class="d-block">
                        {{ $tc('project.info.stars', project.stats.stars) }}
                    </NuxtLink>
                    <NuxtLink :to="`${$route.params.slug}/watchers`" nuxt small class="d-block">
                        {{ $tc('project.info.watchers', project.stats.watchers) }}
                    </NuxtLink>
                    <p v-if="project && project.settings.license && project.settings.license.name">
                        {{ $t('project.license.link') }}
                    </p>
                </div>
                <div class="float-right text-right">
                    <p>{{ $store.state.projectCategories.get(project.category).title }}</p>
                    <p>{{ $util.prettyDate(project.createdAt) }}</p>
                    <p>{{ project.stats.views }}</p>
                    <p>{{ project.stats.downloads }}</p>
                    <p>{{ project.stats.stars }}</p>
                    <p>{{ project.stats.watchers }}</p>
                    <p v-if="project && project.settings.license && project.settings.license.name">
                        <a ref="noopener" :href="project.settings.license.url" target="_blank">{{ project.settings.license.name }}</a>
                    </p>
                </div>
                <div style="clear: both" />
            </div>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { UserActions } from 'hangar-api';
import { HangarProjectMixin } from '~/components/mixins';
import DonationModal from '~/components/donation/DonationModal.vue';
import FlagModal from '~/components/modals/projects/FlagModal.vue';

@Component({
    components: {
        FlagModal,
        DonationModal,
    },
})
export default class ProjectInfo extends HangarProjectMixin {
    toggleStar() {
        this.toggleState('starred', 'star');
    }

    toggleWatch() {
        this.toggleState('watching', 'watch');
    }

    toggleState(stateType: keyof UserActions, route: string, i18nName: string = route) {
        this.$api
            .requestInternal(`projects/project/${this.project.id}/${route}/${!this.project.userActions[stateType]}`, true, 'post')
            .then(() => {
                this.project.userActions[stateType] = !this.project.userActions[stateType];
            })
            .catch((err) => this.$util.handleRequestError(err, `project.error.${i18nName}`));
    }
}
</script>
<style lang="scss" scoped>
.project-info {
    p {
        margin-bottom: 3px;
    }
}
</style>
