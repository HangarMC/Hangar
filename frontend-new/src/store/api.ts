import { defineStore } from "pinia";
import type { Ref} from "vue";
import { ref } from "vue";

import type {Announcement as AnnouncementObject} from "hangar-api";
import {useInternalApi} from '~/composables/useApi';

export const useAPI = defineStore("api", () => {
    const announcements: Ref<AnnouncementObject | undefined> = ref();



    async function getAnnouncements(): Promise<AnnouncementObject[]> {
        return await useInternalApi<AnnouncementObject[]>("data/announcements", false)
    }

    return { announcements, getAnnouncements };
});
