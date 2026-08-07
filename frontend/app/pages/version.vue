<script lang="ts" setup>
const { version } = useVersionInfo();
const route = useRoute("version");
useSeo(computed(() => ({ title: "Hangar Version", route })));

type MilestoneState = "done" | "current" | "planned";

const milestones: { name: string; href: string; state: MilestoneState; description?: string }[] = [
  {
    name: "Before Alpha",
    href: "https://github.com/orgs/HangarMC/projects/1/views/6?filterQuery=+-no%3Apriority+release-target%3A%22before+Alpha%22",
    state: "done",
  },
  {
    name: "Before Beta",
    href: "https://github.com/orgs/HangarMC/projects/1/views/12",
    state: "done",
  },
  {
    name: "During Beta",
    href: "https://github.com/orgs/HangarMC/projects/1/views/14",
    state: "current",
    description: "We aim to fix more bugs and introduce a few new exciting features. Hangar might stay in this phase for a while.",
  },
  {
    name: "Future",
    href: "https://github.com/orgs/HangarMC/projects/1/views/16",
    state: "planned",
    description:
      "The future is looking bright for Hangar, so this milestone captures all tasks that we are interested in, but might be out of reach for a while for various reasons. We're open to more extravagant suggestions!",
  },
];

const stateLabels: Record<MilestoneState, string> = {
  done: "Completed",
  current: "In progress",
  planned: "Planned",
};

const stateTones: Record<MilestoneState, string> = {
  done: "bg-lime-500/15 text-lime-500",
  current: "bg-amber-500/15 text-amber-500",
  planned: "bg-sky-500/15 text-sky-500",
};

const stateChipTones: Record<MilestoneState, "green" | "amber" | "neutral"> = {
  done: "green",
  current: "amber",
  planned: "neutral",
};
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
          <Chip tone="primary" class="tabular-nums">{{ version.version }}</Chip>
        </div>
        <dl class="divide-y divide-gray-300 dark:divide-gray-700">
          <div class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-baseline sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Version</dt>
            <dd class="min-w-0 flex-1">
              <code class="rounded background-card px-2 py-1.5 text-sm tabular-nums">{{ version.version }}</code>
            </dd>
          </div>
          <div class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-baseline sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Commit</dt>
            <dd class="min-w-0 flex-1">
              <Link :href="'https://github.com/HangarMC/Hangar/commit/' + version.commit">
                <code class="rounded background-card px-2 py-1.5 text-sm tabular-nums">{{ version.commitShort }}</code>
              </Link>
            </dd>
          </div>
          <div class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-baseline sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Committer</dt>
            <dd class="min-w-0 flex-1 font-semibold">{{ version.committer }}</dd>
          </div>
          <div class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-baseline sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Committed</dt>
            <dd class="min-w-0 flex-1 tabular-nums">{{ version.time }}</dd>
          </div>
          <div class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-baseline sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Message</dt>
            <dd class="min-w-0 flex-1 whitespace-pre-line">{{ version.message }}</dd>
          </div>
          <div v-if="version.tag" class="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-baseline sm:gap-4">
            <dt class="text-sm text-gray-secondary sm:w-36 sm:flex-shrink-0">Last tag</dt>
            <dd class="min-w-0 flex flex-1 flex-wrap items-center gap-2">
              <code class="rounded background-card px-2 py-1.5 text-sm tabular-nums">{{ version.tag }}</code>
              <span class="text-sm text-gray-secondary tabular-nums">{{ version.behind || 0 }} commits since tag</span>
            </dd>
          </div>
        </dl>
      </Card>

      <Card flat padding="none" class="mt-5">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">Milestones</h2>
          <span class="text-sm text-gray-secondary tabular-nums">{{ milestones.length }}</span>
        </div>
        <p class="px-4 pt-3 text-sm text-gray-secondary">The next development steps of Hangar have been set to the following milestones:</p>
        <ul class="mt-3 divide-y divide-gray-300 dark:divide-gray-700">
          <li v-for="milestone in milestones" :key="milestone.name" class="flex items-start gap-3 px-4 py-3">
            <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg text-lg" :class="stateTones[milestone.state]">
              <IconMdiCheck v-if="milestone.state === 'done'" />
              <IconMdiProgressClock v-else-if="milestone.state === 'current'" />
              <IconMdiCalendarClock v-else />
            </div>
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-2">
                <Link :href="milestone.href">{{ milestone.name }}</Link>
                <Chip :tone="stateChipTones[milestone.state]">{{ stateLabels[milestone.state] }}</Chip>
              </div>
              <p v-if="milestone.description" class="mt-1 text-sm text-gray-secondary">{{ milestone.description }}</p>
            </div>
          </li>
        </ul>
      </Card>

      <p class="mt-5 text-sm text-gray-secondary">
        Please report bugs and other problems you encounter to our <Link href="https://github.com/HangarMC/Hangar/issues/new/choose">issue tracker</Link>.
      </p>
    </template>

    <Alert v-else class="mt-4" type="danger">Version info couldn't be loaded!</Alert>
  </div>
</template>
