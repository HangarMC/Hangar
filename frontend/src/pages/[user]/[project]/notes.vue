<script lang="ts" setup>
import type { Header } from "~/types/components/SortableTable";
import type { HangarProject, HangarProjectNote, User } from "~/types/backend";

definePageMeta({
  projectPermsRequired: ["ModNotesAndFlags"],
});

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();
const i18n = useI18n();
const route = useRoute("user-project-notes");
const notes = await useProjectNotes(props.project.id);
const text = ref("");
const loading = ref(false);

const headers = [
  { title: "Date", name: "createdAt", width: "10%" },
  { title: "User", name: "userName", width: "10%" },
  { title: "Message", name: "message", width: "80%" },
] as const satisfies Header<string>[];

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
  const newNotes = await useInternalApi<HangarProjectNote[]>("projects/notes/" + props.project.id).catch((e) => handleRequestError(e));
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
      <template #createdAt="{ item }">
        {{ i18n.d(item.createdAt, "time") }}
      </template>
    </SortableTable>
  </Card>
</template>
