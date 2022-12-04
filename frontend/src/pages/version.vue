<script lang="ts" setup>
import PageTitle from "~/lib/components/design/PageTitle.vue";
import { useBackendDataStore } from "~/store/backendData";
import Link from "~/lib/components/design/Link.vue";
import Alert from "~/lib/components/design/Alert.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useRoute } from "vue-router";

const version = useBackendDataStore().versionInfo;
const route = useRoute();
useHead(useSeo("Hangar Version", null, route, null));
</script>

<template>
  <PageTitle>Hangar Version</PageTitle>
  <p mb="2">This instance is running <Link href="https://github.com/HangarMC/Hangar">Hangar</Link> {{ version.version }}</p>
  <p mb="2">
    The last commit was <Link :href="'https://github.com/HangarMC/Hangar/commit/' + version.commit">{{ version.commitShort }}</Link> by
    <Link :href="'https://github.com/' + version.committer">{{ version.committer }}</Link> at {{ version.time }} with message: <br />
    {{ version.message }}
  </p>
  <p mb="4">Last Tag: {{ version.tag }} ({{ version.behind || 0 }} commits since tag)</p>
  <h2 class="text-xl mb-4 font-bold">Milestones</h2>
  <p mb="2">The next development steps of Hangar have been set to the following milestones</p>
  <ul class="list-disc list-inside">
    <li mb="2">
      <Link href="https://github.com/orgs/HangarMC/projects/1/views/10">Before Alpha</Link>: <br />
      Once this is done, we will look for Alpha Testers on our Staging Instance on <Link href="https://hangar.papermc.dev">hangar.papermc.dev</Link>.<br />
      At this point, DB resets are unlikely, but not impossible.
    </li>
    <li mb="2">
      <Link href="https://github.com/orgs/HangarMC/projects/1/views/12">Before Beta</Link>: <br />
      Once this is done, we will look for Beta Testers on our Prod Instance on <Link href="https://hangar.papermc.io">hangar.papermc.io</Link>.<br />
      At this point we promise to no longer reset the DB, so you can upload all your plugins! Stuff might still be rough around the edges, so expect breakage,
      please report any findings to our <Link href="https://github.com/HangarMC/Hangar/issues/new/choose">issue tracker</Link>!
    </li>
    <li mb="2">
      <Link href="https://github.com/orgs/HangarMC/projects/1/views/14">During Beta</Link> we aim to fix all bugs and introduce a few new exciting features.
      Hangar might be in this period for a while.
    </li>
    <li mb="2">
      The <Link href="https://github.com/orgs/HangarMC/projects/1/views/16">Future</Link> is looking bright for Hangar, so this milestone captures all tasks
      that we are interested in, but might be out of reach for a while for various reasons. But that should not stop you from suggesting new stuff!
    </li>
  </ul>
  <p mt="2">You can see a graphical display of those milestones <Link href="https://github.com/orgs/HangarMC/projects/1/insights/2">here</Link>.</p>
  <Alert mt="4">
    Please remember, these are estimates, not promises, they can change at any time as the team shifts focus. Although you can always speed up a particular task by
    contributing!
  </Alert>
</template>
