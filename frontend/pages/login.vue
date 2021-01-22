<template>
    <div>Logging in...</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

@Component
export default class Login extends Vue {
    created() {
        this.$auth
            .processLogin()
            .then(() => {
                this.$router.push(this.$cookies.get('returnRoute') || '/');
                this.$cookies.remove('returnRoute');
            })
            .catch(() => {
                this.$router.push('/');
                // TODO error popup
            });
    }
}
</script>
