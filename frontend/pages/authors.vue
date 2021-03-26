<template>
    <v-data-table
        v-if="authors"
        :headers="headers"
        :items="authors.result"
        :options.sync="options"
        :server-items-length="authors.pagination.count"
        multi-sort
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
import { Component, Watch } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { PaginatedResult, User } from 'hangar-api';
import { DataOptions, DataTableHeader } from 'vuetify';
import { UserAvatar } from '~/components/users';
import { HangarComponent } from '~/components/mixins';

@Component({
    components: { UserAvatar },
})
export default class AuthorsPage extends HangarComponent {
    headers: DataTableHeader[] = [
        { text: '', value: 'pic', sortable: false },
        { text: 'Username', value: 'name' },
        { text: 'Roles', value: 'roles', sortable: false },
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

        this.$api
            .request<PaginatedResult<User>>('authors', false, 'get', this.requestOptions)
            .then((authors) => {
                this.authors = authors;
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading = false;
            });
    }

    get requestOptions() {
        if (!this.options) {
            return {};
        }

        const sort: string[] = [];
        for (let i = 0; i < this.options.sortBy.length; i++) {
            let sortStr = this.options.sortBy[i];
            if (sortStr === 'name') {
                sortStr = 'username'; // TODO how to get around this... should we change the field on User to be username?
            }
            if (this.options.sortDesc[i]) {
                sortStr = '-' + sortStr;
            }
            sort.push(sortStr);
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
