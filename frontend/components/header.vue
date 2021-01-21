<template>
    <v-app-bar fixed app>
        <v-menu offset-y open-on-hover transition="slide-y-transition">
            <template #activator="{ on, attrs }">
                <NuxtLink to="/">
                    <img src="https://papermc.io/images/logo-marker.svg" alt="Paper logo" v-bind="attrs" v-on="on" />
                </NuxtLink>
            </template>
            <v-list>
                <v-list-item>
                    <v-list-item-icon><v-icon color="white">mdi-home</v-icon></v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>Home</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
            </v-list>
        </v-menu>
    </v-app-bar>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

@Component
export default class NewPage extends Vue {}
</script>

<style lang="scss" scoped>
img {
    width: 100%;
}
</style>
