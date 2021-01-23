<template>
    <NuxtLink :to="url">
        <img :title="username" :src="src" :alt="username" :class="'user-avatar ' + clazz" />
    </NuxtLink>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Prop } from 'vue-property-decorator';

@Component
export default class UserAvatar extends Vue {
    @Prop()
    username!: String;

    @Prop()
    avatarUrl!: String;

    @Prop()
    imgSrc!: String;

    @Prop()
    clazz!: String;

    // attribute map // TODO implement
    @Prop()
    attr!: Object;

    @Prop()
    href!: String;

    get src(): String {
        if (this.imgSrc) {
            return this.imgSrc;
        } else if (this.avatarUrl) {
            return this.avatarUrl;
        } else {
            return '';
        }
    }

    get url(): String {
        if (this.href) {
            return this.href;
        } else if (this.username) {
            return '/' + this.username;
        } else {
            return '#';
        }
    }
}
</script>

<style scoped>
.user-avatar-md {
    width: 100px;
    height: 100px;
}
.user-avatar-sm {
    width: 50px;
    height: 50px;
}
</style>
