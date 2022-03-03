<script setup lang="ts">
import Header from '~/components/Header.vue';
import Footer from '~/components/Footer.vue';
</script>

<template>
    <main>
        <Header/>
        <div class="min-h-[60vh]">
            <router-view v-bind="$attrs"/>
        </div>
        <Footer/>
    </main>
</template>
