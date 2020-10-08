<template>
    <a :href="url">
        <img :class="`user-avatar ${clazz}`" :title="userName" :src="src" :alt="userName" />
    </a>
</template>

<script>
export default {
    name: 'UserAvatar',
    props: {
        userName: String,
        avatarUrl: String,
        clazz: String,
        imgSrc: String,
        href: String,
    },
    data() {
        return {
            ROUTES: window.ROUTES,
        };
    },
    computed: {
        src() {
            return this.imgSrc || this.userName ? this.avatarUrl : '';
        },
        url() {
            return this.href || this.userName ? this.ROUTES.parse('USERS_SHOW_PROJECTS', this.userName) : '#';
        },
    },
};
</script>
