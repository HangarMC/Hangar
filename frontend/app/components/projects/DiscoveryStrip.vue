<script setup lang="ts">
import type { ProjectCompact } from "#shared/types/backend";

const i18n = useI18n();

const SPEED = 20;
const GAP = 8;
const COPIES = 2;
const STOP_EASE = 0.28;
const RESUME_EASE = 0.22;
const GLIDE_EASE = 0.12;

const viewport = useTemplateRef<HTMLElement>("viewport");
const track = useTemplateRef<HTMLElement>("track");
const paused = ref(false);
let frame = 0;
let reducedMotion = false;
let pos = 0;
let applied = 0;
let glide = 0;
let parked = false;

function period() {
  const element = track.value;
  return element ? (element.scrollWidth + GAP) / COPIES : 0;
}

function move(amount: number) {
  if (reducedMotion) {
    pos += amount;
  } else {
    glide += amount;
  }
}

function nudge(direction: number) {
  const element = viewport.value;
  move(direction * Math.max((element?.clientWidth ?? 0) * 0.3, 260));
}

function onWheel(event: WheelEvent) {
  if (!event.shiftKey && Math.abs(event.deltaX) <= Math.abs(event.deltaY)) return;

  const raw = Math.abs(event.deltaX) > Math.abs(event.deltaY) ? event.deltaX : event.deltaY;
  if (!raw) return;

  event.preventDefault();
  const scale = event.deltaMode === 1 ? 16 : (event.deltaMode === 2 ? (viewport.value?.clientWidth ?? 0) : 1);
  move(raw * scale);
}

onMounted(() => {
  reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  let velocity = reducedMotion ? 0 : SPEED;
  let last = performance.now();

  function step(now: number) {
    const delta = Math.min((now - last) / 1000, 0.05);
    last = now;

    const element = viewport.value;
    if (element) {
      const target = paused.value || reducedMotion ? 0 : SPEED;
      // exponential approach rather than a hard stop, so hovering slows it down instead of snapping
      velocity += (target - velocity) * (1 - Math.exp(-delta / (paused.value ? STOP_EASE : RESUME_EASE)));

      // anything that scrolled the element behind our back (keyboard focus, touch swipe) wins
      if (Math.abs(element.scrollLeft - applied) > 1) pos = element.scrollLeft;

      pos += velocity * delta;
      if (glide) {
        const move = glide * (1 - Math.exp(-delta / GLIDE_EASE));
        pos += move;
        glide -= move;
        if (Math.abs(glide) < 0.5) {
          pos += glide;
          glide = 0;
        }
      }

      const turn = period();
      // inset the wrap band from both ends, or a native swipe hits the browser's scroll clamp instead of looping
      const slack = (turn - GAP - element.clientWidth) / 2;
      if (slack > 0) {
        if (!parked) {
          pos = turn;
          parked = true;
        }
        pos = slack + ((((pos - slack) % turn) + turn) % turn);
        element.scrollLeft = pos;
        applied = element.scrollLeft;
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

// the row is repeated so the scroll can wrap without a visible seam
const loop = computed(() => Array.from({ length: COPIES }, () => projects.value).flat());
</script>

<template>
  <section v-if="projects.length > 0" class="group" aria-labelledby="discover-heading">
    <div class="mb-1.5 flex items-baseline gap-2">
      <h2 id="discover-heading" class="font-semibold">{{ i18n.t("hangar.discover.title") }}</h2>
      <p class="truncate text-sm text-gray-secondary">{{ i18n.t("hangar.discover.sub") }}</p>
    </div>

    <!-- the arrows sit outside the viewport, so the pause has to live on the wrapper or hovering one resumes the drift -->
    <div class="relative" @pointerenter="paused = true" @pointerleave="paused = false" @focusin="paused = true" @focusout="paused = false">
      <div ref="viewport" class="discover-viewport overflow-x-auto" @wheel="onWheel">
        <ul ref="track" class="w-max flex gap-2 will-change-scroll">
          <li v-for="(project, index) in loop" :key="index" :aria-hidden="index >= projects.length" class="w-315px flex-shrink-0">
            <Card class="relative h-full !p-3 border-1px !border-gray-300 hover:background-card !dark:border-gray-700">
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

      <button
        type="button"
        class="discover-arrow -left-2 opacity-0 transition-opacity duration-150 group-hover:opacity-100 focus-visible:opacity-100"
        :aria-label="i18n.t('hangar.discover.prev')"
        @click="nudge(-1)"
      >
        <IconMdiChevronLeft class="text-xl" />
      </button>
      <button
        type="button"
        class="discover-arrow -right-2 opacity-0 transition-opacity duration-150 group-hover:opacity-100 focus-visible:opacity-100"
        :aria-label="i18n.t('hangar.discover.next')"
        @click="nudge(1)"
      >
        <IconMdiChevronRight class="text-xl" />
      </button>
    </div>
  </section>
</template>

<style scoped>
.discover-viewport {
  mask-image: linear-gradient(to right, transparent, #000 32px, #000 calc(100% - 32px), transparent);
  scrollbar-width: none;
  overscroll-behavior-x: contain;
}

.discover-viewport::-webkit-scrollbar {
  display: none;
}

.discover-arrow {
  @apply absolute top-1/2 z-1 -translate-y-1/2 flex items-center justify-center rounded-full h-8 w-8 background-default shadow-lg border-1px border-gray-400 hover:background-card dark:border-gray-600;
}

@media (hover: none) {
  .discover-arrow {
    display: none;
  }
}
</style>
