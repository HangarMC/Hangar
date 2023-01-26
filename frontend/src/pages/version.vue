<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute } from "vue-router";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import Link from "~/lib/components/design/Link.vue";
import Alert from "~/lib/components/design/Alert.vue";
import { useSeo } from "~/composables/useSeo";
import { useVersionInfo } from "~/composables/useApiHelper";

const version = await useVersionInfo();
const route = useRoute();
useHead(useSeo("Hangar Version", null, route, null));
</script>

<template>
  <div>
    <PageTitle>Hangar Version</PageTitle>
    <template v-if="version">
      <p mb="2">This instance is running <Link href="https://github.com/HangarMC/Hangar">Hangar</Link> {{ version.version }}</p>
      <p mb="2">
        The most recent commit is <Link :href="'https://github.com/HangarMC/Hangar/commit/' + version.commit">{{ version.commitShort }}</Link> by
        <span class="font-bold">{{ version.committer }}</span> at {{ version.time }} with message: <br />
        {{ version.message }}
      </p>
      <p v-if="version.tag" mb="2">Last Tag: {{ version.tag }} ({{ version.behind || 0 }} commits since tag)</p>
      <h2 class="text-xl mt-4 mb-2 font-bold">Milestones</h2>
      <p mb="2">The next development steps of Hangar have been set to the following milestones:</p>
      <ul class="list-disc list-inside">
        <li mb="2">
          <Link href="https://github.com/orgs/HangarMC/projects/1/views/12">Before Beta</Link>: <br />
          Having completed the most important work, we will look for beta testers on our production instance on <Link href="https://hangar.papermc.io">hangar.papermc.io</Link>.
          At this point we promise to no longer reset the database, so you can start uploading your plugins! <br><br>
          Certain things might still be rough around the edges, so expect breakage. Please report any findings to our <Link href="https://github.com/HangarMC/Hangar/issues/new/choose">issue tracker</Link>.
        </li>
        <li mb="2">
          <Link href="https://github.com/orgs/HangarMC/projects/1/views/14">During Beta</Link> we aim to fix more bugs and introduce a few new exciting features.
          Hangar might stay in this phase for a while.
        </li>
        <li mb="2">
          The <Link href="https://github.com/orgs/HangarMC/projects/1/views/16">Future</Link> is looking bright for Hangar, so this milestone captures all tasks
          that we are interested in, but might be out of reach for a while for various reasons. We're open to more extravagant suggestions!
        </li>
      </ul>
      <p mt="2">You can see an overview graph of these milestones <Link href="https://github.com/orgs/HangarMC/projects/1/insights/2">here</Link>.</p>
    </template>
    <Alert v-else mt="4" type="danger"> Version info couldn't be loaded! </Alert>
  </div>
</template>
