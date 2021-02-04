<template>
    <v-data-table
        :headers="headers"
        :items="staff.result"
        :options.sync="options"
        :server-items-length="staff.pagination.count"
        :loading="loading"
        class="elevation-1"
    >
        <template #item.roles="{ item }">
            {{ item.roles.map((r) => r.title).join(', ') }}
        </template>
        <template #item.joinDate="{ item }">
            {{ $util.prettyDate(new Date(item.joinDate)) }}
        </template>
    </v-data-table>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'nuxt-property-decorator';
import { PaginatedResult, User } from 'hangar-api';
import { DataOptions, DataTableHeader } from 'vuetify';
import { Context } from '@nuxt/types';

@Component({
    head: {
        title: 'Staff',
    },
})
export default class StaffPage extends Vue {
    headers: DataTableHeader[] = [
        { text: 'Username', value: 'name' },
        { text: 'Roles', value: 'roles' },
        { text: 'Joined', value: 'joinDate' },
    ];

    staff?: PaginatedResult<User>;
    loading = false;
    options = { page: 1, itemsPerPage: 10 } as DataOptions;
    initialLoad = true;

    @Watch('options', { deep: true })
    onOptionsChanged() {
        if (this.initialLoad) {
            this.initialLoad = false;
            return;
        }
        this.loading = true;

        this.$api.request<PaginatedResult<User>>('staff', false, 'get', this.requestOptions).then((staff) => {
            this.staff = staff;
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

    async asyncData({ $api }: Context): Promise<{ staff: PaginatedResult<User> }> {
        return { staff: await $api.request<PaginatedResult<User>>('staff', false, 'get', { limit: 10, offset: 0 }) };
    }
}
</script>

<style lang="scss" scoped></style>
