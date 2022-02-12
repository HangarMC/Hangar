<script setup lang="ts">
import type { Announcement as AnnouncementObject } from 'hangar-api';
import { useInitialState } from '~/composables/useInitialState';
import { useInternalApi } from '~/composables/useApi';

// not sure if they need to be part of the initial state, since we directly render them, would only save a request on page switch at most, but I guess its a good demonstration
const announcements = await useInitialState<AnnouncementObject[]>("announcements", async () => await useInternalApi<AnnouncementObject[]>("data/announcements", false));
</script>

<template>
    <template v-if="announcements">
        <Announcement v-for="(announcement, idx) in announcements" :key="idx" :announcement="announcement" />
    </template>
</template>
