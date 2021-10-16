<template>
    <v-row>
        <v-col cols="12" md="5">
            <div class="text-h4 mb-4">
                {{ $t('apiKeys.createNew') }}
            </div>
            <v-form ref="modalForm" v-model="validForm">
                <v-text-field
                    v-model="name"
                    :label="$t('apiKeys.name')"
                    dense
                    filled
                    counter="255"
                    :loading="validateLoading"
                    :error-messages="nameErrorMessages"
                    :rules="[$util.$vc.required($t('apiKeys.name')), $util.$vc.maxLength(255), $util.$vc.minLength(5)]"
                >
                    <template #append-outer>
                        <v-btn color="success" class="input-append-btn" :disabled="!validForm" :loading="loading" @click="create">
                            {{ $t('apiKeys.createKey') }}
                        </v-btn>
                    </template>
                </v-text-field>
                <v-row no-gutters>
                    <v-col cols="6">
                        <v-checkbox
                            v-for="perm in chunkedPerms[0]"
                            :key="perm.frontendName"
                            v-model="selectedPerms"
                            :label="perm.frontendName"
                            :value="perm.value"
                            :rules="[$util.$vc.requireNonEmptyArray()]"
                            dense
                            hide-details
                        />
                    </v-col>
                    <v-col cols="6">
                        <v-checkbox
                            v-for="perm in chunkedPerms[1]"
                            :key="perm.frontendName"
                            v-model="selectedPerms"
                            :label="perm.frontendName"
                            :value="perm.value"
                            :rules="[$util.$vc.requireNonEmptyArray()]"
                            dense
                            hide-details
                        />
                    </v-col>
                </v-row>
            </v-form>
        </v-col>
        <v-col cols="12" md="7">
            <div class="text-h4 mb-4">{{ $t('apiKeys.existing') }}asdfasdfa</div>
            <v-simple-table>
                <thead>
                    <tr>
                        <th class="text-left">
                            {{ $t('apiKeys.name') }}
                        </th>
                        <th class="text-left">
                            {{ $t('apiKeys.key') }}
                        </th>
                        <th class="text-left">
                            {{ $t('apiKeys.keyIdentifier') }}
                        </th>
                        <th class="text-left">
                            {{ $t('apiKeys.permissions') }}
                        </th>
                        <th class="text-left">
                            {{ $t('apiKeys.delete') }}
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="key in apiKeys" :key="key.name">
                        <td>{{ key.name }}</td>
                        <td>{{ key.token }}</td>
                        <td>{{ key.tokenIdentifier }}</td>
                        <td>{{ key.permissions.map((p) => $perms.convert(p).frontendName).join(', ') }}</td>
                        <td>
                            <v-tooltip bottom>
                                <template #activator="{ on }">
                                    <v-btn icon small color="red" @click="deleteKey(key)" v-on="on">
                                        <v-icon>mdi-delete</v-icon>
                                    </v-btn>
                                </template>
                                <span>{{ $t('apiKeys.deleteKey') }}</span>
                            </v-tooltip>
                        </td>
                    </tr>
                    <tr v-if="apiKeys.length === 0">
                        <td colspan="5">
                            <v-alert type="info" width="100%" class="mt-4">
                                {{ $t('apiKeys.noKeys') }}
                            </v-alert>
                        </td>
                    </tr>
                </tbody>
            </v-simple-table>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component, Watch } from 'nuxt-property-decorator';
import { ApiKey, IPermission } from 'hangar-api';
import { chunk } from 'lodash-es';
import { Context } from '@nuxt/types';
import { TranslateResult } from 'vue-i18n';
import { AxiosError } from 'axios';
import { RootState } from '~/store';
import { NamedPermission } from '~/types/enums';
import { CurrentUser, GlobalPermission } from '~/utils/perms';
import { HangarForm } from '~/components/mixins';

@Component
@GlobalPermission(NamedPermission.EDIT_API_KEYS)
@CurrentUser
export default class AuthorSettingsApiKeysPage extends HangarForm {
    selectedPerms: NamedPermission[] = [];
    name: string = '';
    validateLoading = false;
    nameErrorMessages: TranslateResult[] = [];
    possiblePerms!: NamedPermission[];
    apiKeys!: ApiKey[];

    head() {
        return this.$seo.head(this.$t('apiKeys.title'), null, this.$route, this.$util.avatarUrl(this.$route.params.user));
    }

    get chunkedPerms(): IPermission[][] {
        const permArr = this.possiblePerms.map((perm) => (this.$store.state as RootState).permissions.get(perm)!);
        return chunk(permArr, permArr.length / 2);
    }

    $refs!: {
        modalForm: any;
    };

    create() {
        this.loading = true;
        this.$api
            .requestInternal<string>(`api-keys/create-key/${this.$route.params.user}`, true, 'post', {
                name: this.name,
                permissions: this.selectedPerms,
            })
            .then((key) => {
                this.apiKeys.unshift({ token: key, name: this.name, permissions: this.selectedPerms, createdAt: new Date().toISOString() });
                this.$util.success(this.$t('apiKeys.success.create', [this.name]));
                this.$refs.modalForm.reset();
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading = false;
            });
    }

    deleteKey(key: ApiKey) {
        this.$api
            .requestInternal(`api-keys/delete-key/${this.$route.params.user}`, true, 'post', {
                content: key.name,
            })
            .then(() => {
                this.$delete(this.apiKeys, this.apiKeys.indexOf(key));
                this.$util.success(this.$t('apiKeys.success.delete', [key.name]));
            })
            .catch(this.$util.handleRequestError);
    }

    @Watch('name')
    checkName(val: string) {
        if (!val) return;
        this.validateLoading = true;
        this.nameErrorMessages = [];
        this.$api
            .requestInternal(`api-keys/check-key/${this.$route.params.user}`, true, 'get', {
                name: this.name,
            })
            .catch((err: AxiosError) => {
                if (!err.response?.data.isHangarApiException) {
                    return this.$util.handleRequestError(err);
                }
                this.nameErrorMessages.push(this.$t(err.response.data.message));
            })
            .finally(() => {
                this.validateLoading = false;
            });
    }

    async asyncData({ $api, $util, params }: Context) {
        const data = await Promise.all([
            $api.requestInternal<IPermission[]>(`api-keys/possible-perms/${params.user}`),
            $api.requestInternal<ApiKey[]>(`api-keys/existing-keys/${params.user}`),
        ]).catch<any>($util.handlePageRequestError);
        if (typeof data === 'undefined') return;
        return { possiblePerms: data[0], apiKeys: data[1] };
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
