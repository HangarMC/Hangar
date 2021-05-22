<template>
    <v-data-table
        :headers="headers"
        :items="users.result"
        :options.sync="options"
        :server-items-length="users.pagination.count"
        :items-per-page="25"
        :footer-props="{ itemsPerPageOptions: [5, 15, 25] }"
        multi-sort
        :loading="loading"
        class="elevation-1"
    >
        <template #item.username="{ item }">
            <NuxtLink :to="'/' + item.name">{{ item.name }}</NuxtLink>
        </template>
        <template #item.pic="{ item }">
            <UserAvatar :username="item.name" :avatar-url="$util.avatarUrl(item.name)" clazz="user-avatar-xs" />
        </template>
        <template #item.roles="{ item }">
            <span v-for="role in item.roles" :key="role.roleId" :style="{ backgroundColor: role.color }" class="user-role-badge">{{ role.title }}</span>
        </template>
        <template #item.joinDate="{ item }">
            {{ $util.prettyDate(item.joinDate) }}
        </template>
    </v-data-table>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { PaginatedResult, User } from 'hangar-api';
import { DataTableHeader } from 'vuetify';
import { Context } from '@nuxt/types';
import { UserAvatar } from '~/components/users';
import { UserListPage } from '~/components/mixins';

@Component({
    components: { UserAvatar },
})
export default class StaffPage extends UserListPage {
    headers: DataTableHeader[] = [
        { text: '', value: 'pic', sortable: false },
        { text: this.$t('pages.headers.username') as string, value: 'username' },
        { text: this.$t('pages.headers.roles') as string, value: 'roles', sortable: false },
        { text: this.$t('pages.headers.joined') as string, value: 'joinDate' },
    ];

    head() {
        return this.$seo.head(this.$t('pages.staffTitle'), null, this.$route, null);
    }

    get url(): string {
        return 'staff';
    }

    async asyncData({ $api, $util }: Context) {
        const users = await $api.request<PaginatedResult<User>>('staff', false).catch<any>($util.handlePageRequestError);
        return { users };
    }
}
</script>

<style lang="scss" scoped></style>
