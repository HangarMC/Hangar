<script setup lang="ts">
const { t } = useI18n();
</script>

<template>
  <footer class="relative mt-10 py-1 background-default text-light-10">
    <div class="mt-3 mb-3 max-w-screen-xl mx-auto px-4">
      <div class="flex flex-wrap flex-col gap-4 justify-around items-center text-sm">
        <div class="flex flex-row flex-wrap justify-center">
          <a
            href="https://github.com/HangarMC"
            class="flex items-center rounded-md px-6 py-2"
            hover="text-primary-500 bg-primary-0"
            target="_blank"
            rel="noreferrer noopener"
          >
            {{ t("hangar.footer.org") }}
          </a>
          <a
            href="https://status.papermc.io"
            class="flex items-center rounded-md px-6 py-2"
            hover="text-primary-500 bg-primary-0"
            target="_blank"
            rel="noreferrer noopener"
          >
            {{ t("hangar.footer.status") }}
          </a>
          <NuxtLink :to="{ name: 'api-docs' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-500 bg-primary-0">
            {{ t("hangar.footer.api") }}
          </NuxtLink>
          <NuxtLink :to="{ name: 'terms' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-500 bg-primary-0">
            {{ t("hangar.footer.terms") }}
          </NuxtLink>
          <NuxtLink :to="{ name: 'privacy' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-500 bg-primary-0">
            {{ t("hangar.footer.privacypolicy") }}
          </NuxtLink>
          <NuxtLink :to="{ name: 'guidelines' }" class="flex items-center rounded-md px-6 py-2" hover="text-primary-500 bg-primary-0">
            {{ t("hangar.footer.guidelines") }}
          </NuxtLink>
          <NuxtLink href="https://forums.papermc.io/help/legal-notice/" class="flex items-center rounded-md px-6 py-2" hover="text-primary-500 bg-primary-0">
            {{ t("hangar.footer.legalNotice") }}
          </NuxtLink>
        </div>
        <div class="flex flex-row flex-wrap justify-center gap-4">
          <Link to="/paper">Download Paper Plugins</Link>
          <Link to="/velocity">Download Velocity Plugins</Link>
          <Link to="/waterfall">Download Waterfall Plugins</Link>
        </div>
        <div class="flex flex-grow-1 lt-md:justify-center">
          <p class="font-bold">© {{ new Date().getFullYear() }} <a href="https://papermc.io/">PaperMC</a></p>
        </div>
        <div class="flex flex-grow-1 text-opacity-80 font-size-3 font-light text-center">
          <p>
            This website is not an official Minecraft website and is not associated with Mojang Studios or Microsoft. All product and company names are
            trademarks or registered trademarks of their respective holders. Use of these names does not imply any affiliation or endorsement by them.
          </p>
        </div>
      </div>
    </div>
  </footer>
</template>
