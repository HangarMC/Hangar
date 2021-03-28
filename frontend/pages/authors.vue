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
import { Context } from '@nuxt/types';
import { PaginatedResult, User } from 'hangar-api';
import { DataTableHeader } from 'vuetify';
import { UserAvatar } from '~/components/users';
import { UserListPage } from '~/components/mixins';

@Component({
    components: { UserAvatar },
})
export default class AuthorsPage extends UserListPage {
    // TODO i18n for headers
    headers: DataTableHeader[] = [
        { text: '', value: 'pic', sortable: false },
        { text: 'Username', value: 'username' },
        { text: 'Roles', value: 'roles', sortable: false },
        { text: 'Joined', value: 'joinDate' },
        { text: 'Projects', value: 'projectCount' },
    ];

    head() {
        return {
            title: this.$t('pages.authors'),
        };
    }

    get url(): string {
        return 'authors';
    }

    async asyncData({ $api, $util }: Context) {
        const users = await $api.request<PaginatedResult<User>>('authors', false).catch<any>($util.handlePageRequestError);
        return { users };
    }
}
</script>

<style lang="scss" scoped></style>
