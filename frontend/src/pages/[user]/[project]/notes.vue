<script lang="ts" setup>
import Card from "~/lib/components/design/Card.vue";
import Link from "~/lib/components/design/Link.vue";
import { User } from "hangar-api";
import { useI18n } from "vue-i18n";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import Alert from "~/lib/components/design/Alert.vue";
import { useContext } from "vite-ssr/vue";
import { useProjectNotes } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { HangarProject, Note } from "hangar-internal";
import { ref } from "vue";
import { useInternalApi } from "~/composables/useApi";
import InputText from "~/lib/components/ui/InputText.vue";
import Button from "~/lib/components/design/Button.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { useRoute } from "vue-router";

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();
const i18n = useI18n();
const ctx = useContext();
const route = useRoute();
const notes = await useProjectNotes(props.project.id).catch((e) => handleRequestError(e, ctx, i18n));
const text = ref("");
const loading = ref(false);

const headers = [
  { title: "Date", name: "createdAt", width: "10%" },
  { title: "User", name: "userName", width: "10%" },
  { title: "Message", name: "message", width: "80%" },
] as Header[];

useHead(useSeo("Notes | " + props.project.name, props.project.description, route, projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)));

async function addNote() {
  if (!text.value) {
    return;
  }
  loading.value = true;
  await useInternalApi(`projects/notes/${props.project.id}`, true, "post", {
    content: text.value,
  }).catch((e) => handleRequestError(e, ctx, i18n));
  text.value = "";
  const newNotes = await useInternalApi<Note[]>("projects/notes/" + props.project.id, false).catch((e) => handleRequestError(e, ctx, i18n));
  if (notes && newNotes) {
    notes.value = newNotes;
  }
  loading.value = false;
}
</script>

<template>
  <Card>
    <template #header>
      {{ i18n.t("notes.header") }}
      <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
        {{ project.namespace.owner + "/" + project.namespace.slug }}
      </Link>
    </template>

    <div class="flex">
      <div class="flex-grow"><InputText v-model="text" :placeholder="i18n.t('notes.placeholder')"></InputText></div>
      <Button :disabled="!text || loading" size="medium" class="ml-4 w-max" @click="addNote">{{ i18n.t("notes.addNote") }}</Button>
    </div>

    <h2 class="text-lg font-bold mb-1 mt-2">
      {{ i18n.t("notes.notes") }}
    </h2>
    <SortableTable v-if="notes" :items="notes" :headers="headers">
      <template #empty>
        <Alert type="warning">{{ i18n.t("notes.noNotes") }}</Alert>
      </template>
      <template #item_createdAt="{ item }">
        {{ i18n.d(item.createdAt, "time") }}
      </template>
    </SortableTable>
  </Card>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["MOD_NOTES_AND_FLAGS"]
</route>
