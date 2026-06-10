<script setup lang="ts">
import { NuxtLink } from "#components";

const { t } = useI18n();

const hangarLinks = computed(() => [
  { label: t("hangar.footer.org"), href: "https://github.com/HangarMC", external: true },
  { label: t("hangar.footer.status"), href: "https://status.papermc.io", external: true },
  { label: t("hangar.footer.api"), to: { name: "api-docs" } },
  { label: t("hangar.footer.guidelines"), to: { name: "guidelines" } },
]);

const legalLinks = computed(() => [
  { label: t("hangar.footer.terms"), to: { name: "terms" } },
  { label: t("hangar.footer.privacypolicy"), to: { name: "privacy" } },
  { label: t("hangar.footer.legalNotice"), href: "https://forums.papermc.io/help/legal-notice/", external: true },
]);

const platformLinks = [
  { label: "Download Paper Plugins", to: "/paper" },
  { label: "Download Velocity Plugins", to: "/velocity" },
  { label: "Download Waterfall Plugins", to: "/waterfall" },
];
</script>

<template>
  <footer class="relative mt-12 bg-transparent">
    <div class="mx-auto max-w-screen-2xl px-6">
      <div class="grid md:grid-cols-2 lg:grid-cols-[1.1fr_1fr_1.25fr_1fr]">
        <section class="py-8 md:pr-8 lg:py-10" aria-labelledby="footer-brand">
          <h2 id="footer-brand" class="text-xl font-bold">PaperMC</h2>
          <p class="mt-2 max-w-72 text-sm leading-relaxed text-gray">The community-powered home for Paper, Velocity, and Waterfall plugins.</p>
          <p class="mt-5 text-sm text-gray">© {{ new Date().getFullYear() }} PaperMC</p>
        </section>

        <section class="py-8 md:pl-8 lg:py-10 lg:pr-8" aria-labelledby="footer-hangar">
          <h2 id="footer-hangar" class="mb-3 px-2 text-sm font-bold uppercase tracking-wide text-gray">Hangar</h2>
          <nav class="flex flex-col items-start gap-1" aria-label="Hangar">
            <component
              :is="link.href ? 'a' : NuxtLink"
              v-for="link in hangarLinks"
              :key="link.label"
              :to="link.to"
              :href="link.href"
              class="inline-flex items-center px-2 py-2 font-medium decoration-none hover:color-primary hover:underline"
              :target="link.external ? '_blank' : undefined"
              :rel="link.external ? 'noreferrer noopener' : undefined"
            >
              {{ link.label }}
              <IconMdiOpenInNew v-if="link.external" class="ml-1 text-xs text-gray" />
            </component>
          </nav>
        </section>

        <section class="py-8 md:pr-8 lg:px-8 lg:py-10" aria-labelledby="footer-platforms">
          <h2 id="footer-platforms" class="mb-3 px-2 text-sm font-bold uppercase tracking-wide text-gray">Platforms</h2>
          <nav class="flex flex-col items-start gap-1" aria-label="Plugin downloads">
            <NuxtLink
              v-for="link in platformLinks"
              :key="link.to"
              :to="link.to"
              class="inline-flex items-center px-2 py-2 font-medium decoration-none hover:color-primary hover:underline"
            >
              <IconMdiDownloadOutline class="mr-1 text-sm" />
              {{ link.label }}
            </NuxtLink>
          </nav>
        </section>

        <section class="py-8 md:pl-8 lg:py-10" aria-labelledby="footer-legal">
          <h2 id="footer-legal" class="mb-3 px-2 text-sm font-bold uppercase tracking-wide text-gray">Legal</h2>
          <nav class="flex flex-col items-start gap-1" aria-label="Legal">
            <component
              :is="link.href ? 'a' : NuxtLink"
              v-for="link in legalLinks"
              :key="link.label"
              :to="link.to"
              :href="link.href"
              class="inline-flex items-center px-2 py-2 font-medium decoration-none hover:color-primary hover:underline"
              :target="link.external ? '_blank' : undefined"
              :rel="link.external ? 'noreferrer noopener' : undefined"
            >
              {{ link.label }}
              <IconMdiOpenInNew v-if="link.external" class="ml-1 text-xs text-gray" />
            </component>
          </nav>
        </section>
      </div>

      <p class="py-5 text-center text-xs font-light leading-relaxed text-gray">
        This website is not an official Minecraft website and is not associated with Mojang Studios or Microsoft. All product and company names are trademarks
        or registered trademarks of their respective holders. Use of these names does not imply any affiliation or endorsement by them.
      </p>
    </div>
  </footer>
</template>
