<template>
    <NuxtLink :to="url">
        <img
            :title="username"
            :src="src"
            :alt="username"
            :class="'user-avatar ' + clazz"
            @error="$event.target.src = 'https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png'"
        />
    </NuxtLink>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { PropType } from 'vue';

@Component
export default class UserAvatar extends Vue {
    @Prop()
    username!: String;

    @Prop()
    avatarUrl!: String;

    @Prop()
    imgSrc!: String;

    @Prop()
    clazz!: PropType<'user-avatar-md' | 'user-avatar-sm' | 'user-avatar-xs'>;

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
.user-avatar-xs {
    width: 32px;
    height: 32px;
}
</style>
