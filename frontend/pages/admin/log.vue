<template>
    <v-card>
        <v-card-title>{{ $t('userActionLog.title') }}</v-card-title>
        <v-card-text>
            <v-data-table :items="loggedActions.result" :headers="headers">
                <template #item.user="{ item }">
                    <NuxtLink :to="'/' + item.userName">{{ item.userName }}</NuxtLink>
                </template>
                <template #item.time="{ item }">
                    {{ $util.prettyDateTime(item.createdAt) }}
                </template>
                <template #item.action="{ item }">
                    {{ item.action.description }}
                </template>
                <template #item.context="{ item }">
                    <span v-if="item.page">
                        <NuxtLink :to="'/' + item.project.owner + '/' + item.project.slug + '/pages/' + item.page.slug">
                            {{ item.project.owner + '/' + item.project.slug + '/' + item.page.slug }}
                        </NuxtLink>
                    </span>
                    <span v-else-if="item.version">
                        <NuxtLink :to="'/' + item.project.owner + '/' + item.project.slug + '/versions/' + item.version.versionString">
                            {{ item.project.owner + '/' + item.project.slug + '/' + item.version.versionString }}
                        </NuxtLink>
                    </span>
                    <span v-else-if="item.project && item.project.owner">
                        <NuxtLink :to="'/' + item.project.owner + '/' + item.project.slug">{{ item.project.owner + '/' + item.project.slug }}</NuxtLink>
                    </span>
                    <span v-else-if="item.subject">
                        <NuxtLink :to="'/' + item.subject.username">{{ item.subject.username }}</NuxtLink>
                    </span>
                </template>
                <template #item.oldState="{ item }">
                    <template v-if="item.page">
                        <MarkdownModal :markdown="item.oldState" :title="$t('userActionLog.markdownView')">
                            <template #activator="{ on, attrs }">
                                <v-btn small color="primary" v-bind="attrs" v-on="on">{{ $t('userActionLog.markdownView') }}</v-btn>
                            </template>
                        </MarkdownModal>
                    </template>
                    <template v-else>
                        {{ item.oldState }}
                    </template>
                </template>
                <template #item.newState="{ item }">
                    <template v-if="item.page">
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
                    <template v-else>
                        {{ item.newState }}
                    </template>
                </template>
            </v-data-table>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { LoggedAction } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { PaginatedResult } from 'hangar-api';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';
import MarkdownModal from '~/components/modals/MarkdownModal.vue';
import DiffModal from '~/components/modals/DiffModal.vue';

// TODO figure out a nice way to do filters for AdminLogPage
@Component({
    components: { DiffModal, MarkdownModal },
})
@GlobalPermission(NamedPermission.VIEW_LOGS)
export default class AdminLogPage extends Vue {
    loggedActions!: PaginatedResult<LoggedAction>;
    headers = [
        { text: this.$t('userActionLog.user'), value: 'user' },
        { text: this.$t('userActionLog.address'), value: 'address' },
        { text: this.$t('userActionLog.time'), value: 'time' },
        { text: this.$t('userActionLog.action'), value: 'action' },
        { text: this.$t('userActionLog.context'), value: 'context' },
        { text: this.$t('userActionLog.oldState'), value: 'oldState' },
        { text: this.$t('userActionLog.newState'), value: 'newState' },
    ];

    async asyncData({ $api, $util }: Context) {
        const loggedActions = await $api.requestInternal<PaginatedResult<LoggedAction>>(`actionlog/`, false).catch<any>($util.handlePageRequestError);
        return { loggedActions };
    }
}
</script>

<style lang="scss" scoped></style>
