<template>
    <v-card>
        <v-card-title>
            <span v-text="$t('flags.header')"></span>&nbsp;
            <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">{{ project.namespace.owner + '/' + project.namespace.slug }}</NuxtLink>
        </v-card-title>
        <v-card-text>
            <v-data-table v-if="flags && flags.length > 0" :headers="headers" :items="flags" disable-filtering disable-sort hide-default-footer>
                <template #item.user="{ item }">{{ item.reportedByName }}</template>
                <template #item.reason="{ item }">{{ item.reason }}</template>
                <template #item.createdAt="{ item }">{{ $util.prettyDateTime(item.createdAt) }}</template>
                <template #item.resolved="{ item }">
                    <span v-if="item.resolved">{{ $t('flags.resolved', [item.resolvedByName, $util.prettyDate(item.resolvedAt)]) }}</span>
                    <span v-else v-text="$t('flags.notResolved')"></span>
                </template>
            </v-data-table>
            <v-alert v-else type="info" prominent>{{ $t('flags.noFlags') }}</v-alert>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { Project } from 'hangar-api';
import { Flag } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

@Component
@GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
export default class ProjectFlagsPage extends Vue {
    @Prop({ required: true })
    project!: Project;

    flags!: Flag[];

    get headers() {
        return [
            { text: 'Submitter', value: 'user' },
            { text: 'Reason', value: 'reason' },
            { text: 'Comment', value: 'comment' },
            { text: 'When', value: 'createdAt' },
            { text: 'Resolved', value: 'resolved' },
        ];
    }

    async asyncData({ $api, $util }: Context) {
        const flags = await $api.requestInternal<Flag[]>(`flags/`, false).catch<any>($util.handlePageRequestError);
        return { flags };
    }
}
</script>

<style lang="scss" scoped></style>
