<script lang="ts" setup>
import type { User } from "hangar-api";
import { useI18n } from "vue-i18n";
import type { HangarProject, Note } from "hangar-internal";
import { ref } from "vue";
import { useHead } from "@unhead/vue";
import { useRoute } from "vue-router";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import SortableTable from "~/components/SortableTable.vue";
import Alert from "~/components/design/Alert.vue";
import { useProjectNotes } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useInternalApi } from "~/composables/useApi";
import InputText from "~/components/ui/InputText.vue";
import Button from "~/components/design/Button.vue";
import { useSeo } from "~/composables/useSeo";
import { definePageMeta } from "#imports";
import type { Header } from "~/types/components/SortableTable";

definePageMeta({
  projectPermsRequired: ["MOD_NOTES_AND_FLAGS"],
});

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();
const i18n = useI18n();
const route = useRoute();
const notes = await useProjectNotes(props.project.id);
const text = ref("");
const loading = ref(false);

const headers: Header[] = [
  { title: "Date", name: "createdAt", width: "10%" },
  { title: "User", name: "userName", width: "10%" },
  { title: "Message", name: "message", width: "80%" },
];

useHead(useSeo("Notes | " + props.project.name, props.project.description, route, props.project.avatarUrl));

async function addNote() {
  if (!text.value) {
    return;
  }
  loading.value = true;
  await useInternalApi(`projects/notes/${props.project.id}`, "post", {
    content: text.value,
  }).catch((e) => handleRequestError(e));
  text.value = "";
  const newNotes = await useInternalApi<Note[]>("projects/notes/" + props.project.id).catch((e) => handleRequestError(e));
  if (notes?.value && newNotes) {
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
