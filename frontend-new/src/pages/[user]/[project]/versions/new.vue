<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { HangarProject } from "hangar-internal";
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import Steps, { Step } from "~/components/design/Steps.vue";
import { ref } from "vue";
import Alert from "~/components/design/Alert.vue";
import InputFile from "~/components/ui/InputFile.vue";
import InputText from "~/components/ui/InputText.vue";
import InputSelect from "~/components/ui/InputSelect.vue";
import Button from "~/components/design/Button.vue";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import MarkdownEditor from "~/components/MarkdownEditor.vue";

const route = useRoute();
const i18n = useI18n();
const props = defineProps<{
  project: HangarProject;
}>();

const selectedStep = ref("artifact");
// TODO i18n
const steps: Step[] = [
  { value: "artifact", header: "Artifact" },
  { value: "basic", header: "Basic Info" },
  { value: "dependencies", header: "Dependencies" },
  { value: "changelog", header: "Changelog" },
];

useHead(
  useSeo(
    i18n.t("version.new.title") + " | " + props.project.name,
    props.project.description,
    route,
    projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)
  )
);
</script>

<!-- todo functionality, design, i18n, all the things -->
<template>
  <Steps v-model="selectedStep" :steps="steps" button-lang-key="dum">
    <template #artifact>
      <p>Please specify the artifact. You can either upload a jar or a zip file, or you can link to an external site.</p>
      <Alert>An external link needs to be a direct download link!</Alert>
      <div class="flex flex-wrap">
        <InputFile />
        <span class="basis-full">or</span>
        <InputText label="External url" />
      </div>
    </template>
    <template #basic>
      <p>We detected the following settings based on the artifact you provided. Please fill out the remaining fields.</p>
      <div class="flex flex-wrap">
        <div class="basis-full md:basis-4/12"><InputText label="Version" /></div>
        <div class="basis-full md:basis-4/12"><InputText label="File name" /></div>
        <div class="basis-full md:basis-4/12"><InputText label="File size" /></div>

        <div class="basis-8/12"><InputSelect label="Channel" /></div>
        <Button class="basis-4/12">Add channel</Button>

        <div class="basis-4/12"><InputCheckbox label="Unstable" /></div>
        <div class="basis-4/12"><InputCheckbox label="Recommended" /></div>
        <div class="basis-4/12"><InputCheckbox label="Forum Post" /></div>
      </div>
    </template>
    <template #dependencies>
      <p>We detected the following dependencies based on the artifact you provided. Please fill out the remaining fields.</p>
      <div class="flex flex-wrap">
        Platform dependency stuff<br />
        Plugin dependency stuff
      </div>
    </template>
    <template #changelog>
      <p>Whats new?</p>
      <MarkdownEditor raw="# dum" :editing="true" />
    </template>
  </Steps>
</template>

<route lang="yaml">
meta:
  requireProjectPerm: ["CREATE_VERSION"]
</route>
