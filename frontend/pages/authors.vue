<template>
    <v-data-table
        v-if="authors"
        :headers="headers"
        :items="authors.result"
        :options.sync="options"
        :server-items-length="authors.pagination.count"
        :loading="loading"
        class="elevation-1"
    >
        <template #item.pic="{ item }">
            <UserAvatar :username="item.name" :avatar-url="$util.avatarUrl(item.name)" clazz="user-avatar-xs" />
        </template>
        <template #item.roles="{ item }">
            {{ item.roles.map((r) => r.title).join(', ') }}
        </template>
        <template #item.joinDate="{ item }">
            {{ $util.prettyDate(item.joinDate) }}
        </template>
    </v-data-table>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { PaginatedResult, User } from 'hangar-api';
import { DataOptions, DataTableHeader } from 'vuetify';
import { UserAvatar } from '~/components/users';

@Component({
    components: { UserAvatar },
})
export default class AuthorsPage extends Vue {
    headers: DataTableHeader[] = [
        { text: '', value: 'pic' },
        { text: 'Username', value: 'name' },
        { text: 'Roles', value: 'roles' },
        { text: 'Joined', value: 'joinDate' },
        { text: 'Projects', value: 'projectCount' },
    ];

    authors?: PaginatedResult<User>;
    loading = false;
    options = { page: 1, itemsPerPage: 10 } as DataOptions;
    initialLoad = true;

    head() {
        return {
            title: this.$t('pages.authors'),
        };
    }

    @Watch('options', { deep: true })
    onOptionsChanged() {
        if (this.initialLoad) {
            this.initialLoad = false;
            return;
        }
        this.loading = true;

        this.$api.request<PaginatedResult<User>>('authors', false, 'get', this.requestOptions).then((authors) => {
            this.authors = authors;
            this.loading = false;
        });
    }

    get requestOptions() {
        if (!this.options) {
            return {};
        }

        let sort = '';
        if (this.options.sortBy.length === 1) {
            sort = this.options.sortBy[0];
            if (this.options.sortDesc[0]) {
                sort = '-' + sort;
            }
        }
        return {
            limit: this.options.itemsPerPage,
            offset: (this.options.page - 1) * this.options.itemsPerPage,
            sort,
        };
    }

    async asyncData({ $api, $util }: Context): Promise<{ authors: PaginatedResult<User> | void }> {
        const authors = await $api
            .request<PaginatedResult<User>>('authors', false, 'get', { limit: 10, offset: 0 })
            .catch($util.handlePageRequestError);
        return { authors };
    }
}
</script>

<style lang="scss" scoped></style>
