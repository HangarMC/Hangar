<script setup lang="ts">
import { useHead } from '@vueuse/head';
import { useSeo } from '~/composables/useSeo';

const title = 'Hangar New Test';
const description = 'IDK WTF am doing';

useHead(useSeo(title, description, useRoute(), null));
</script>

<template>
    <router-view v-slot="{ Component, route }">
        <transition name="slide">
            <component :is="Component" :key="route" />
        </transition>
    </router-view>
</template>
