<script setup lang="ts">
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>

<template>
  <footer class="relative flex mt-10 background-default px-8 text-light-10">
    <div class="mt-3 mb-3 flex flex-column flex-grow-1 justify-center">
      <div class="flex flex-wrap flex-row-reverse flex-grow-1 gap-4 justify-around items-center max-w-1200px text-sm">
        <div class="flex flex-row flex-wrap justify-center">
          <a
            href="https://github.com/HangarMC"
            class="flex items-center rounded-md px-6 py-2"
            hover="text-primary-400 bg-primary-0"
            target="_blank"
            rel="noreferrer noopener"
          >
            {{ t("hangar.footer.org") }}
          </a>
          <a
            href="https://status.papermc.io"
            class="flex items-center rounded-md px-6 py-2"
            hover="text-primary-400 bg-primary-0"
            target="_blank"
            rel="noreferrer noopener"
          >
            {{ t("hangar.footer.status") }}
          </a>
          <NuxtLink :to="{ name: 'api-docs' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0">
            {{ t("hangar.footer.api") }}
          </NuxtLink>
          <NuxtLink :to="{ name: 'terms' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0">
            {{ t("hangar.footer.terms") }}
          </NuxtLink>
          <NuxtLink :to="{ name: 'privacy' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0">
            {{ t("hangar.footer.privacypolicy") }}
          </NuxtLink>
          <NuxtLink href="https://forums.papermc.io/help/legal-notice/" class="flex items-center rounded-md px-6 py-2" hover="text-primary-400 bg-primary-0">
            {{ t("hangar.footer.legalNotice") }}
          </NuxtLink>
        </div>
        <p class="font-bold">© {{ new Date().getFullYear() }} <a href="https://papermc.io/">PaperMC</a></p>
      </div>
    </div>
  </footer>
</template>
