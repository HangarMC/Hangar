<template>
    <v-card>
        <v-card-title>
            <span v-text="$t('flags.header')"></span>&nbsp;
            <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">{{ project.namespace.owner + '/' + project.namespace.slug }}</NuxtLink>
        </v-card-title>
        <v-card-text>
            <v-data-table v-if="flags && flags.length > 0" :headers="headers" :items="flags" disable-filtering disable-sort hide-default-footer>
                <template #item.user="{ item }">{{ item.user.name }}</template>
                <template #item.reason="{ item }">{{ item.reason.title }}</template>
                <template #item.createdAt="{ item }">{{ $util.prettyDate(item.createdAt) }}</template>
                <template #item.resolved="{ item }">
                    <span v-if="item.resolved">{{ $t('flags.resolved', [item.resolvedBy.name, $util.prettyDate(item.resolvedAt)]) }}</span>
                    <span v-else v-text="$t('flags.notResolved')"></span>
                </template>
            </v-data-table>
            <v-alert v-else type="info" prominent>{{ $t('flags.noFlags') }}</v-alert>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Project } from 'hangar-api';
import { Prop } from 'vue-property-decorator';
import { Flag } from 'hangar-internal';

@Component
export default class ProjectFlagsPage extends Vue {
    @Prop({ required: true })
    project!: Project;

    // todo load flags
    flags: Array<Flag> = [
        {
            id: 1,
            createdAt: '2021-02-13T12:18:52.456Z',
            user: this.$util.dummyUser(),
            comment: 'Naughty',
            reason: { title: 'Test', type: 'Test' },
            resolved: true,
            resolvedAt: '2021-02-13T12:18:52.456Z',
            resolvedBy: this.$util.dummyUser(),
        },
    ] as Array<Flag>;

    get headers() {
        return [
            { text: 'Submitter', value: 'user' },
            { text: 'Reason', value: 'reason' },
            { text: 'Comment', value: 'comment' },
            { text: 'When', value: 'createdAt' },
            { text: 'Resolved', value: 'resolved' },
        ];
    }
}
</script>

<style lang="scss" scoped></style>
