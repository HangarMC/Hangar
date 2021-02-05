<template>
    <v-snackbar v-model="enabled" app :timeout="timeout" :color="color" top @input="onClose">
        <div v-for="(message, index) in messages" :key="index" class="pb-1">
            {{ message }}
        </div>
    </v-snackbar>
</template>

<script lang="ts">
import { Component, namespace, Vue } from 'nuxt-property-decorator';

const snackbar = namespace('snackbar');
@Component
export default class HangarSnackbar extends Vue {
    @snackbar.State
    timeout!: number;

    get enabled() {
        return this.$store.state.snackbar.enabled;
    }

    set enabled(value) {
        this.$store.commit('snackbar/SHOW_SNACKBAR', value);
    }

    @snackbar.State
    messages!: string[];

    @snackbar.State
    color!: string;

    @snackbar.Mutation('CLEAR_MESSAGES')
    onClose!: () => void;
}
</script>

<style lang="scss" scoped></style>
