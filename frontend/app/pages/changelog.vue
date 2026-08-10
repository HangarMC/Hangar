<script lang="ts" setup>
import { changelog, unseenChangelog } from "#shared/changelog";
import type { ChangelogKind } from "#shared/changelog";

const { t } = useI18n();
const route = useRoute("changelog");
const authStore = useAuthStore();

useSeo(computed(() => ({ title: t("changelog.title"), description: t("changelog.description"), route })));

// captured once, so marking the page seen doesn't move the divider while it's being read
const unseenIds = new Set(unseenChangelog(authStore.user?.lastSeenChangelogAt).map((entry) => entry.id));
const unseenCount = unseenIds.size;

onMounted(async () => {
  if (!authStore.user || unseenCount === 0) return;
  try {
    await useInternalApi("users/settings/changelogSeen", "post");
    authStore.user.lastSeenChangelogAt = new Date().toISOString();
  } catch {
    // cosmetic
  }
});

const kinds: Record<ChangelogKind, { tone: "primary" | "green" | "neutral" | "amber"; label: string }> = {
  feature: { tone: "primary", label: "changelog.kind.feature" },
  improvement: { tone: "green", label: "changelog.kind.improvement" },
  fix: { tone: "neutral", label: "changelog.kind.fix" },
  api: { tone: "amber", label: "changelog.kind.api" },
};

const entries = computed(() => changelog.map((entry) => ({ ...entry, unseen: unseenIds.has(entry.id), html: parseMarkdown(entry.body).html })));

const firstSeenIndex = computed(() => (unseenCount === 0 || unseenCount === entries.value.length ? -1 : unseenCount));
</script>

<template>
  <div>
    <div class="mb-5">
      <h1 class="text-3xl font-bold">{{ t("changelog.title") }}</h1>
      <p class="mt-1 text-gray-secondary">
        {{ t("changelog.description") }}
        <Link href="/changelog.atom" class="feed-link ml-1"><IconMdiRss class="mr-1" />{{ t("changelog.feed") }}</Link>
      </p>
    </div>

    <Card flat padding="none">
      <ol class="m-0 list-none divide-y divide-gray-300 p-0 dark:divide-gray-700">
        <template v-for="(entry, index) in entries" :key="entry.id">
          <li v-if="index === firstSeenIndex" class="flex items-center gap-3 px-4 py-2 text-xs text-gray-secondary" aria-hidden="true">
            <hr class="min-w-4 flex-1 border-gray-300 dark:border-gray-700" />
            {{ t("changelog.seenDivider") }}
            <hr class="min-w-4 flex-1 border-gray-300 dark:border-gray-700" />
          </li>
          <li :id="entry.id" class="scroll-mt-20 px-4 py-4" :class="{ 'entry-unseen': entry.unseen }">
            <div class="mb-1 flex flex-wrap items-center gap-2">
              <Chip :tone="kinds[entry.kind].tone">{{ t(kinds[entry.kind].label) }}</Chip>
              <time class="text-sm text-gray-secondary tabular-nums" :datetime="entry.date">{{ entry.date }}</time>
            </div>
            <h2 class="text-lg font-bold">
              <a :href="'#' + entry.id" class="hover:color-primary">{{ entry.title }}</a>
            </h2>
            <!-- eslint-disable-next-line vue/no-v-html -->
            <div class="prose dark:prose-invert mt-2 max-w-none text-sm" v-html="useDomPurify(entry.html)" />
          </li>
        </template>
      </ol>
    </Card>
  </div>
</template>

<style scoped>
.entry-unseen {
  box-shadow: inset 3px 0 0 var(--primary-500);
}

/* keep the link on the paragraph's baseline; an inline-flex box would sit off it */
.feed-link :deep(svg) {
  display: inline-block;
  vertical-align: -0.15em;
}
</style>
