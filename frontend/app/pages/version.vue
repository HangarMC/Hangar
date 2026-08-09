<script lang="ts" setup>
const { version } = useVersionInfo();
const route = useRoute("version");
useSeo(computed(() => ({ title: "Hangar Version", route })));
</script>

<template>
  <div>
    <div class="mb-5">
      <h1 class="text-3xl font-bold">Hangar Version</h1>
      <p class="mt-1 text-gray-secondary">Build information for this instance of <Link href="https://github.com/HangarMC/Hangar">Hangar</Link>.</p>
    </div>

    <template v-if="version">
      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">Build info</h2>
        </div>
        <dl class="m-0 divide-y divide-gray-300 dark:divide-gray-700">
          <div class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-center sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Version</dt>
            <dd class="min-w-0 flex flex-1 items-center">
              <code class="inline-flex items-center rounded background-card px-2 py-0.5 text-sm tabular-nums">{{ version.version }}</code>
            </dd>
          </div>
          <div class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-center sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Commit</dt>
            <dd class="min-w-0 flex flex-1 items-center">
              <Link :href="'https://github.com/HangarMC/Hangar/commit/' + version.commit" class="inline-flex">
                <code class="inline-flex items-center rounded background-card px-2 py-0.5 text-sm tabular-nums">{{ version.commitShort }}</code>
              </Link>
            </dd>
          </div>
          <div class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-center sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Committer</dt>
            <dd class="min-w-0 flex-1 font-semibold">{{ version.committer }}</dd>
          </div>
          <div class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-center sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Committed</dt>
            <dd class="min-w-0 flex-1 tabular-nums">{{ version.time }}</dd>
          </div>
          <div class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-center sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Message</dt>
            <dd class="min-w-0 flex-1 whitespace-pre-line">{{ version.message }}</dd>
          </div>
          <div v-if="version.tag" class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-center sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Last tag</dt>
            <dd class="min-w-0 flex flex-1 flex-wrap items-center gap-2">
              <code class="inline-flex items-center rounded background-card px-2 py-0.5 text-sm tabular-nums">{{ version.tag }}</code>
              <span class="text-sm text-gray-secondary tabular-nums">{{ version.behind || 0 }} commits since tag</span>
            </dd>
          </div>
        </dl>
      </Card>

      <p class="mt-5 text-sm text-gray-secondary">
        Please report bugs and other problems you encounter to our <Link href="https://github.com/HangarMC/Hangar/issues/new/choose">issue tracker</Link>.
      </p>
    </template>

    <Alert v-else class="mt-4" type="danger">Version info couldn't be loaded!</Alert>
  </div>
</template>
