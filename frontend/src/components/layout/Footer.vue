<script setup lang="ts">
import { useI18n } from "vue-i18n";

const i18n = useI18n();
</script>

<template>
  <footer class="relative flex items-end mt-10 background-default px-8 pb-2 text-light-10 min-h-10">
    <div class="footerContent w-screen">
      <div class="footerBar flex flex-col gap-4 md:(flex-row-reverse gap-0) items-center justify-around mt-4 max-w-1200px m-auto text-sm">
        <div class="footerBarLinks flex flex-col md:flex-row">
          <router-link :to="{ name: 'guidelines' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0"
            >{{ i18n.t("hangar.footer.resourceGuidelines") }}
          </router-link>
          <router-link :to="{ name: 'api' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0">{{
            i18n.t("hangar.footer.api")
          }}</router-link>
          <a href="https://github.com/HangarMC" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0">{{
            i18n.t("hangar.footer.org")
          }}</a>
          <router-link :to="{ name: 'tos' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0"
            >{{ i18n.t("hangar.footer.tos") }}
          </router-link>
        </div>
        <p class="font-bold">© 2022 <a href="https://papermc.io/">PaperMC</a></p>
      </div>
    </div>
  </footer>
</template>
