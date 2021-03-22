<template>
    <v-row>
        <v-col cols="12" md="6">
            <h2>{{ $t('apiKeys.createNew') }}</h2>
            <v-text-field v-model="name" :label="$t('apiKeys.name')">
                <template #append-outer>
                    <v-btn @click="create">{{ $t('apiKeys.createKey') }}</v-btn>
                </template>
            </v-text-field>
            <v-row>
                <v-col v-for="perm in perms" :key="perm.frontendName" cols="6">
                    <v-checkbox v-model="selectedPerms" :label="perm.frontendName" :value="perm.value" dense hide-details> </v-checkbox>
                </v-col>
            </v-row>
        </v-col>
        <v-col cols="12" md="6">
            <h2>{{ $t('apiKeys.existing') }}</h2>
            <v-simple-table>
                <thead>
                    <tr>
                        <th class="text-left">{{ $t('apiKeys.name') }}</th>
                        <th class="text-left">{{ $t('apiKeys.key') }}</th>
                        <th class="text-left">{{ $t('apiKeys.keyIdentifier') }}</th>
                        <th class="text-left">{{ $t('apiKeys.permissions') }}</th>
                        <th class="text-left">{{ $t('apiKeys.delete') }}</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="key in apiKeys" :key="key.name">
                        <td>{{ key.name }}</td>
                        <td>{{ key.key }}</td>
                        <td>{{ key.identifier }}</td>
                        <td>{{ key.permissions }}</td>
                        <td>
                            <v-btn color="red" @click="deleteKey(key.key)">{{ $t('apiKeys.deleteKey') }}</v-btn>
                        </td>
                    </tr>
                </tbody>
            </v-simple-table>
            <v-alert v-if="apiKeys.length === 0" type="info">{{ $t('apiKeys.noKeys') }}</v-alert>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { ApiKey, IPermission } from 'hangar-api';
import { RootState } from '~/store';
import { NamedPermission } from '~/types/enums';
import { GlobalPermission } from '~/utils/perms';

@Component
@GlobalPermission(NamedPermission.EDIT_API_KEYS)
export default class AuthorSettingsApiKeysPage extends Vue {
    selectedPerms: NamedPermission[] = [];
    name: string = '';

    // TODO load from server
    apiKeys: ApiKey[] = [];

    get perms(): IPermission[] {
        return Array.from((this.$store.state as RootState).permissions.values());
    }

    create() {
        // TODO send to server
        this.apiKeys.push({ key: 'blah', identifier: 'blub', permissions: this.selectedPerms, name: this.name, createdAt: new Date().toString() });
        this.name = '';
        this.selectedPerms = [];
    }

    deleteKey(key: string) {
        // todo send to server
        this.apiKeys = this.apiKeys.filter((e) => e.key !== key);
    }
}
</script>
<style lang="scss" scoped>
.v-input--checkbox {
    margin-top: 0;
}
.col {
    padding-top: 0;
    padding-bottom: 0;
}
</style>
