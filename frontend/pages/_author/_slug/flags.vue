<template>
    <v-card>
        <v-card-title>
            <span v-text="$t('flags.header')"></span>&nbsp;
            <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">{{ project.namespace.owner + '/' + project.namespace.slug }}</NuxtLink>
        </v-card-title>
        <v-card-text>
            <v-data-table :loading="$fetchState.pending" :headers="headers" :items="flags" disable-filtering disable-sort hide-default-footer>
                <template #no-data>
                    <v-alert type="info" class="mt-2">{{ $t('flags.noFlags') }}</v-alert>
                </template>
                <template #item.user="{ item }">{{ item.reportedByName }}</template>
                <template #item.reason="{ item }">{{ $t(item.reason) }}</template>
                <template #item.createdAt="{ item }">{{ $util.prettyDateTime(item.createdAt) }}</template>
                <template #item.resolved="{ item }">
                    <span v-if="item.resolved">{{ $t('flags.resolved', [item.resolvedByName, $util.prettyDate(item.resolvedAt)]) }}</span>
                    <span v-else v-text="$t('flags.notResolved')"></span>
                </template>
            </v-data-table>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Flag } from 'hangar-internal';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';
import { HangarProjectMixin } from '~/components/mixins';

@Component
@GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
export default class ProjectFlagsPage extends HangarProjectMixin {
    flags: Flag[] = [];

    head() {
        return this.$seo.head(
            'Flags | ' + this.project.name,
            this.project.description,
            this.$route,
            this.$util.projectUrl(this.project.namespace.owner, this.project.namespace.slug)
        );
    }

    get headers() {
        return [
            { text: 'Submitter', value: 'user' },
            { text: 'Reason', value: 'reason' },
            { text: 'Comment', value: 'comment' },
            { text: 'When', value: 'createdAt' },
            { text: 'Resolved', value: 'resolved' },
        ];
    }

    async fetch() {
        this.flags = (await this.$api.requestInternal<Flag[]>(`flags/${this.project.id}`, false).catch<any>(this.$util.handlePageRequestError)) || [];
    }
}
</script>

<style lang="scss" scoped></style>
