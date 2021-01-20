<template>
    <div v-show="error" id="keyAlert" class="alert alert-danger" role="alert" v-text="error"></div>
    <div class="row">
        <div class="col-md-6">
            <h2 v-text="$t('user.apiKeys.createNew')"></h2>
            <div class="row">
                <div v-for="(chunk, index) in possiblePermissions" :key="`perm-chunk-${index}`" class="col-md-6">
                    <div v-for="perm in chunk" :key="perm.value" class="checkbox">
                        <label> <input v-model="form.perms" type="checkbox" :value="perm.value" /> {{ perm.name }} </label>
                    </div>
                </div>
            </div>
            <div class="input-group">
                <div class="input-group-prepend">
                    <label for="new-key-name-input" class="input-group-text" v-text="$t('user.apiKeys.keyName')"></label>
                </div>
                <input v-model.trim="form.name" type="text" class="form-control" id="new-key-name-input" />
                <div class="input-group-append">
                    <button
                        type="button"
                        class="btn input-group-btn btn-primary"
                        :disabled="!form.perms.length || !form.name"
                        v-text="$t('user.apiKeys.createKeyBtn')"
                        @click="createKey"
                    ></button>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <h2 v-text="$t('user.apiKeys.existingKeys')"></h2>
            <table class="table">
                <thead>
                    <tr>
                        <th v-text="$t('user.apiKeys.keyName')"></th>
                        <th v-text="$t('user.apiKeys.keyToken')"></th>
                        <th v-text="$t('user.apiKeys.keyIdentifier')"></th>
                        <th v-text="$t('user.apiKeys.keyPermissions')"></th>
                        <th v-text="$t('user.apiKeys.keyDeleteColumn')"></th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="key in existingKeys" :key="key.id">
                        <th v-text="key.name"></th>
                        <th v-text="key.token"></th>
                        <th v-text="key.tokenIdentifier"></th>
                        <th v-text="key.namedRawPermissions.join(', ')"></th>
                        <th><button class="btn btn-danger" v-text="$t('user.apiKeys.keyDeleteButton')" @click="deleteKey(key)"></button></th>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script>
import { chunk, remove } from 'lodash-es';
import { API } from '@/api';

export default {
    name: 'ApiKeyManagement',
    data() {
        return {
            form: {
                perms: [],
                name: '',
            },
            error: null,
            possiblePermissions: chunk(window.API_KEY_PERMISSIONS, window.API_KEY_PERMISSIONS.length / 2),
            existingKeys: window.EXISTING_KEYS,
        };
    },
    methods: {
        createKey() {
            this.error = null;
            if (this.form.perms.length === 0) {
                this.error = this.$t('user.apiKeys.error.noPermsSet');
                return;
            }
            if (!this.form.name || !this.form.name.trim()) {
                this.error = this.$t('user.apiKeys.error.noNameSet');
                return;
            }
            if (this.form.name.length > 255) {
                this.error = this.$t('user.apiKeys.error.tooLongName');
                return;
            }
            if (this.existingKeys.find((key) => key.name === this.form.name)) {
                this.error = this.$t('user.apiKeys.error.nameAlreadyUsed');
                return;
            }
            API.request('keys', 'POST', {
                permissions: this.form.perms,
                name: this.form.name,
            }).then(({ data }) => {
                this.error = null;
                this.existingKeys.push({
                    name: this.form.name,
                    token: data.key,
                    namedRawPermissions: data.perms,
                });
                this.form.name = '';
                this.form.perms = [];
            });
        },
        deleteKey(key) {
            API.request(`keys?name=${key.name}`, 'DELETE').then(() => {
                remove(this.existingKeys, (k) => k.name === key.name);
            });
        },
    },
};
</script>
