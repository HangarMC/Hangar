<template>
    <v-card>
        <v-card-title>{{ $t('userActionLog.title') }}</v-card-title>
        <v-card-text>
            <v-data-table
                :items="loggedActions.result"
                :headers="headers"
                :server-items-length="loggedActions.pagination.count"
                :items-per-page="50"
                :options.sync="options"
                :footer-props="{ itemsPerPageOptions: [10, 25, 50] }"
                :loading="loading"
                disable-sort
            >
                <template #item.user="{ item }">
                    <NuxtLink :to="'/' + item.userName">{{ item.userName }}</NuxtLink>
                </template>
                <template #item.time="{ item }">
                    {{ $util.prettyDateTime(item.createdAt) }}
                </template>
                <template #item.action="{ item }">
                    {{ $t(item.action.description) }}
                </template>
                <template #item.context="{ item }">
                    <span v-if="item.page">
                        <NuxtLink :to="'/' + item.project.owner + '/' + item.project.slug + '/pages/' + item.page.slug">
                            {{ item.project.owner + '/' + item.project.slug + '/' + item.page.slug }}
                        </NuxtLink>
                    </span>
                    <span v-else-if="item.version">
                        <NuxtLink :to="'/' + item.project.owner + '/' + item.project.slug + '/versions/' + item.version.versionString">
                            {{ `${item.project.owner}/${item.project.slug}/${item.version.versionString}/${item.version.platforms[0].toLowerCase()}` }}
                        </NuxtLink>
                    </span>
                    <span v-else-if="item.project && item.project.owner">
                        <NuxtLink :to="'/' + item.project.owner + '/' + item.project.slug">{{ item.project.owner + '/' + item.project.slug }}</NuxtLink>
                    </span>
                    <span v-else-if="item.subject">
                        <NuxtLink :to="'/' + item.subject.name">{{ item.subject.name }}</NuxtLink>
                    </span>
                </template>
                <template #item.oldState="{ item }">
                    <template v-if="item.contextType === 'PAGE' && item.oldState">
                        <MarkdownModal :markdown="item.oldState" :title="$t('userActionLog.markdownView')">
                            <template #activator="{ on, attrs }">
                                <v-btn small color="primary" v-bind="attrs" v-on="on">{{ $t('userActionLog.markdownView') }}</v-btn>
                            </template>
                        </MarkdownModal>
                    </template>
                    <template v-else-if="item.action.pgLoggedAction === 'project_icon_changed'">
                        <span v-if="item.oldState === '#empty'">default</span>
                        <img v-else class="inline-img" :src="'data:image/png;base64,' + item.oldState" alt="" />
                    </template>
                    <template v-else>
                        {{ $te(item.oldState) ? $t(item.oldState) : item.oldState }}
                    </template>
                </template>
                <template #item.newState="{ item }">
                    <template v-if="item.contextType === 'PAGE'">
                        <MarkdownModal :markdown="item.newState" :title="$t('userActionLog.markdownView')">
                            <template #activator="{ on, attrs }">
                                <v-btn small color="primary" v-bind="attrs" v-on="on">{{ $t('userActionLog.markdownView') }}</v-btn>
                            </template>
                        </MarkdownModal>
                        <DiffModal :left="item.oldState" :right="item.newState" :title="$t('userActionLog.diffView')">
                            <template #activator="{ on, attrs }">
                                <v-btn small color="primary" v-bind="attrs" v-on="on">{{ $t('userActionLog.diffView') }}</v-btn>
                            </template>
                        </DiffModal>
                    </template>
                    <template v-else-if="item.action.pgLoggedAction === 'project_icon_changed'">
                        <span v-if="item.newState === '#empty'">default</span>
                        <img v-else class="inline-img" :src="'data:image/png;base64,' + item.newState" alt="" />
                    </template>
                    <template v-else>
                        {{ $te(item.newState) ? $t(item.newState) : item.newState }}
                    </template>
                </template>
            </v-data-table>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { LoggedAction } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { PaginatedResult } from 'hangar-api';
import { DataOptions } from 'vuetify';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';
import MarkdownModal from '~/components/modals/MarkdownModal.vue';
import DiffModal from '~/components/modals/DiffModal.vue';
import { HangarComponent } from '~/components/mixins';

// TODO figure out a nice way to do filters for AdminLogPage
@Component({
    components: { DiffModal, MarkdownModal },
})
@GlobalPermission(NamedPermission.VIEW_LOGS)
export default class AdminLogPage extends HangarComponent {
    loggedActions!: PaginatedResult<LoggedAction>;
    loading = false;
    options = { page: 1, itemsPerPage: 50 } as DataOptions;
    headers = [
        { text: this.$t('userActionLog.user'), value: 'user' },
        { text: this.$t('userActionLog.address'), value: 'address' },
        { text: this.$t('userActionLog.time'), value: 'time' },
        { text: this.$t('userActionLog.action'), value: 'action' },
        { text: this.$t('userActionLog.context'), value: 'context' },
        { text: this.$t('userActionLog.oldState'), value: 'oldState' },
        { text: this.$t('userActionLog.newState'), value: 'newState' },
    ];

    head() {
        return {
            title: this.$t('userActionLog.title'),
        };
    }

    // TODO I'd like to move these things to a mixin since they are common across multiple components (see authors.vue, staff.vue, etc.)
    mounted() {
        this.$watch('options', this.onOptionsChanged, { deep: true });
    }

    onOptionsChanged() {
        this.loading = true;
        this.$api
            .requestInternal<PaginatedResult<LoggedAction>>('admin/log', true, 'get', this.requestOptions)
            .then((log) => {
                this.loggedActions = log;
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading = false;
            });
    }

    get requestOptions() {
        return {
            limit: this.options.itemsPerPage,
            offset: (this.options.page - 1) * this.options.itemsPerPage,
        };
    }

    async asyncData({ $api, $util }: Context) {
        const loggedActions = await $api.requestInternal<PaginatedResult<LoggedAction>>(`admin/log/`, false).catch<any>($util.handlePageRequestError);
        return { loggedActions };
    }
}
</script>

<style lang="scss" scoped>
.inline-img {
    height: 100%;
}
</style>
