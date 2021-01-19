<template>
    <div class="input-group" style="position: relative">
        <input
            v-model.trim="input"
            type="text"
            class="form-control"
            :placeholder="$t('org.users.add') + '&hellip;'"
            aria-label="Username"
            @input="search"
            @focus="inputFocused = true"
            @blur="inputFocused = false"
            @keyup.enter="addUser"
        />
        <ul v-show="inputFocused" class="search-dropdown list-group">
            <li v-for="user in userResults" :key="user.id" class="list-group-item list-group-item-action" @mousedown.prevent @click="selectUser(user)">
                {{ user.name }}
            </li>
        </ul>
        <div class="input-group-append input-group-btn">
            <button :disabled="!selectedUser || loading" type="button" class="btn btn-default" title="Add User" @click.prevent="addUser">
                <i v-show="loading" class="fas fa-spinner fa-spin"></i>
                <i v-show="!loading" class="fas fa-plus"></i>
            </button>
        </div>
    </div>
</template>

<script>
import { API } from '@/api';

export default {
    name: 'UserSearch',
    emits: ['add-user'],
    data() {
        return {
            input: '',
            inputFocused: false,
            userResults: [],
            selectedUser: null,
            loading: false,
        };
    },
    methods: {
        search() {
            this.selectedUser = null;
            this.loading = true;
            API.request(`users?q=${this.input}`).then((response) => {
                let data = response.data;
                this.loading = false;
                if (data.result.length === 1 && data.result[0].name.toLowerCase() === this.input.toLowerCase()) {
                    this.selectUser(data.result[0]);
                } else {
                    this.userResults = data.result;
                }
            });
        },
        selectUser(user) {
            this.reset();
            this.input = user.name;
            this.selectedUser = user;
        },
        addUser() {
            if (this.userResults.length === 1) {
                this.selectedUser = this.userResults[0];
            }
            if (!this.selectedUser) {
                return;
            }
            this.loading = true;
            API.request(`users/${this.selectedUser.name}`)
                .then(() => {
                    this.$emit('add-user', this.selectedUser);
                    this.reset();
                })
                .catch(() => {
                    this.reset();
                })
                .finally(() => {
                    this.loading = false;
                });
        },
        reset() {
            this.selectedUser = null;
            this.input = '';
            this.inputFocused = false;
            this.userResults = [];
        },
    },
};
</script>
<style lang="scss" scoped>
.search-dropdown {
    position: absolute;
    top: 100%;
    max-height: 300px;
    overflow-y: hidden;
    z-index: 100;
    width: 100%;

    li {
        cursor: pointer;
    }
}
</style>
