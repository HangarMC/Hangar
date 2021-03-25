<template>
    <div>
        <h1>
            {{ $t('userAdmin.title') }}
            <NuxtLink :to="'/' + $route.params.user">{{ $route.params.user }}</NuxtLink>
        </h1>
        <v-row>
            <v-col md="8" cols="12">
                <v-card>
                    <v-card-title>{{ $t('userAdmin.organizations') }}</v-card-title>
                    <v-card-text>
                        <v-data-table :items="orgList" :headers="orgConfig">
                            <template #item.name="{ item }">
                                <NuxtLink :to="'/' + item.name">{{ item.name }}</NuxtLink>
                            </template>
                            <template #item.owner="{ item }">
                                <!-- todo owner -->
                                <NuxtLink :to="'/' + item.name.owner">{{ item.name.owner }}</NuxtLink>
                            </template>
                            <template #item.role="{ item }"> {{ organizations[item.name].role.title }} </template>
                            <template #item.accepted="{ item }">
                                <!--suppress EqualityComparisonWithCoercionJS dont ask me why this doesnt work without this-->
                                <v-checkbox :value="organizations[item.name].accepted == true" readonly></v-checkbox>
                            </template>
                        </v-data-table>
                    </v-card-text>
                </v-card>
                <v-card class="mt-3">
                    <v-card-title>{{ $t('userAdmin.projects') }}</v-card-title>
                    <v-card-text>
                        <v-data-table :items="projects.result" :headers="projectsConfig">
                            <template #item.name="{ item }">
                                <NuxtLink :to="'/' + item.namespace.owner + '/' + item.name">{{ item.name }}</NuxtLink>
                            </template>
                            <template #item.owner="{ item }">
                                <NuxtLink :to="'/' + item.namespace.owner">{{ item.namespace.owner }}</NuxtLink>
                            </template>
                            <template #item.role="{ item }">
                                <!-- todo role -->
                                Role {{ item.name }}
                            </template>
                            <template #item.accepted="{ item }">
                                <v-checkbox :value="item.visibility === 'public'" readonly></v-checkbox>
                            </template>
                        </v-data-table>
                    </v-card-text>
                </v-card>
            </v-col>
            <v-col md="4" cols="12">
                <v-card>
                    <v-card-title>{{ $t('userAdmin.sidebar') }}</v-card-title>
                    <v-list>
                        <!-- todo links -->
                        <v-list-item>
                            <a href="">{{ $t('userAdmin.hangarAuth') }}</a>
                        </v-list-item>
                        <v-list-item>
                            <a href="">{{ $t('userAdmin.forum') }}</a>
                        </v-list-item>
                    </v-list>
                </v-card>
            </v-col>
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { PaginatedResult, Project } from 'hangar-api';
import { RoleTable } from 'hangar-internal';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';
import { HangarComponent } from '~/components/mixins';

@Component
@GlobalPermission(NamedPermission.EDIT_ALL_USER_SETTINGS)
export default class AdminUserPage extends HangarComponent {
    projects!: PaginatedResult<Project>;
    organizations!: { [key: string]: RoleTable };

    get projectsConfig() {
        return [
            { text: this.$t('userAdmin.project'), value: 'name' },
            { text: this.$t('userAdmin.owner'), value: 'owner' },
            { text: this.$t('userAdmin.role'), value: 'role' },
            { text: this.$t('userAdmin.accepted'), value: 'accepted' },
        ];
    }

    get orgConfig() {
        return [
            { text: this.$t('userAdmin.organization'), value: 'name' },
            { text: this.$t('userAdmin.owner'), value: 'owner' },
            { text: this.$t('userAdmin.role'), value: 'role' },
            { text: this.$t('userAdmin.accepted'), value: 'accepted' },
        ];
    }

    get orgList() {
        return Object.keys(this.organizations).map((name) => {
            return { name };
        });
    }

    async asyncData({ params, $api, $util }: Context) {
        const data = await Promise.all([
            $api.request<PaginatedResult<Project>>(`projects`, false, 'get', {
                owner: params.user,
            }),
            $api.requestInternal<{ [key: string]: RoleTable }>(`organizations/${params.user}/userOrganizations`, false),
        ]).catch<any>($util.handlePageRequestError);
        return { projects: data[0], organizations: data[1] };
    }
}
</script>

<style lang="scss" scoped></style>
