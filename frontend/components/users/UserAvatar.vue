<template>
    <div class="user-avatar-wrap">
        <NuxtLink :to="url">
            <img :title="username" :src="src" :alt="username" :class="'user-avatar ' + clazz" @error="errored = true" />
        </NuxtLink>
        <slot />
    </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { PropType } from 'vue';

@Component
export default class UserAvatar extends Vue {
    @Prop()
    username!: string;

    @Prop()
    avatarUrl!: string;

    @Prop()
    imgSrc!: string;

    @Prop()
    clazz!: PropType<'user-avatar-md' | 'user-avatar-sm' | 'user-avatar-xs'>;

    @Prop()
    href!: string;

    errored: boolean = false;

    get src(): string {
        if (this.errored) {
            return 'https://docs.papermc.io/img/paper.png';
        } else if (this.imgSrc) {
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
.user-avatar-xs {
    width: 32px;
    height: 32px;
}
</style>
