<script setup lang="ts">
import type { ProjectCompact } from "#shared/types/backend";

const i18n = useI18n();

const SPEED = 20;
const GAP = 8;
const STOP_EASE = 0.28;
const RESUME_EASE = 0.22;

const track = useTemplateRef<HTMLElement>("track");
const paused = ref(false);
let frame = 0;

onMounted(() => {
  if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;

  let offset = 0;
  let velocity = 0;
  let last = performance.now();

  function step(now: number) {
    const delta = Math.min((now - last) / 1000, 0.05);
    last = now;

    const element = track.value;
    if (element) {
      // exponential approach rather than a hard stop, so hovering slows it down instead of snapping
      const ease = paused.value ? STOP_EASE : RESUME_EASE;
      velocity += ((paused.value ? 0 : SPEED) - velocity) * (1 - Math.exp(-delta / ease));

      const period = (element.scrollWidth + GAP) / 2;
      if (period > 0) {
        offset = (offset + velocity * delta) % period;
        element.style.transform = `translate3d(${-offset}px, 0, 0)`;
      }
    }
    frame = requestAnimationFrame(step);
  }

  frame = requestAnimationFrame(step);
});

onBeforeUnmount(() => cancelAnimationFrame(frame));

const { data: projects } = await useAsyncData("discovery-daily", () => useInternalApi<ProjectCompact[]>("discovery/daily", "get").catch(() => []), {
  default: () => [] as ProjectCompact[],
});

// the row is rendered twice so the transform can wrap without a visible seam
const loop = computed(() => [...projects.value, ...projects.value]);
</script>

<template>
  <section v-if="projects.length > 0" aria-labelledby="discover-heading">
    <div class="mb-2 flex items-baseline gap-2">
      <h2 id="discover-heading" class="text-lg font-semibold">{{ i18n.t("hangar.discover.title") }}</h2>
      <p class="text-sm text-gray-secondary">{{ i18n.t("hangar.discover.sub") }}</p>
    </div>

    <div
      class="discover-viewport overflow-hidden"
      @pointerenter="paused = true"
      @pointerleave="paused = false"
      @focusin="paused = true"
      @focusout="paused = false"
    >
      <ul ref="track" class="w-max flex gap-2 will-change-transform">
        <li v-for="(project, index) in loop" :key="index" :aria-hidden="index >= projects.length" class="w-260px flex-shrink-0">
          <Card class="group relative h-full !p-3 border-1px !border-gray-300 hover:background-card !dark:border-gray-700">
            <div class="flex items-center gap-2.5">
              <div class="h-40px w-40px flex-shrink-0">
                <UserAvatar :username="project.namespace.owner" :monogram-name="project.name" :img-src="project.avatarUrl" size="fill" disable-link />
              </div>

              <div class="min-w-0 flex-1">
                <!-- name gets a line to itself: beside the author both end up truncated -->
                <p class="truncate font-bold leading-tight">
                  <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug" class="after:(absolute inset-0 content-empty)">
                    {{ project.name }}
                  </NuxtLink>
                </p>
                <p class="min-w-0 truncate text-xs text-gray-secondary">
                  {{ i18n.t("general.by") }}
                  <NuxtLink :to="'/' + project.namespace.owner" class="relative z-1 font-bold color-primary hover:underline">
                    {{ project.namespace.owner }}
                  </NuxtLink>
                </p>
              </div>
            </div>

            <p class="mt-2 line-clamp-2 min-h-2.5rem text-sm leading-snug">{{ project.description }}</p>

            <div class="mt-1 flex items-center gap-x-3 text-xs text-gray-secondary">
              <span class="inline-flex items-center gap-1">
                <CategoryLogo :category="project.category" :size="14" class="flex-shrink-0" />
                {{ i18n.t("project.category." + project.category) }}
              </span>
              <span class="inline-flex items-center gap-1 tabular-nums">
                <IconMdiDownload class="h-3.5 w-3.5 flex-shrink-0" />
                {{ project.stats.downloads.toLocaleString("en-US") }}
              </span>
            </div>
          </Card>
        </li>
      </ul>
    </div>
  </section>
</template>

<style scoped>
.discover-viewport {
  mask-image: linear-gradient(to right, transparent, #000 32px, #000 calc(100% - 32px), transparent);
}
</style>
