<script lang="ts" setup>
import type { HangarProject } from "#shared/types/backend";

definePageMeta({
  projectPermsRequired: ["ModNotesAndFlags"],
});

const props = defineProps<{
  project?: HangarProject;
}>();
const i18n = useI18n();
const route = useRoute("user-project-notes");
const { notes, refreshNotes } = useProjectNotes(() => route.params.project);
const text = ref("");
const loading = ref(false);

useSeo(
  computed(() => ({
    title: i18n.t("notes.title") + " | " + props.project?.name,
    route,
    description: props.project?.description,
    image: props.project?.avatarUrl,
  }))
);

async function addNote() {
  if (!text.value) {
    return;
  }
  loading.value = true;
  await useInternalApi(`projects/notes/${props.project?.id}`, "post", {
    content: text.value,
  }).catch((err) => handleRequestError(err));
  text.value = "";
  await refreshNotes();
  loading.value = false;
}
</script>

<template>
  <div>
    <div class="mb-5">
      <h1 class="text-3xl font-bold">{{ i18n.t("notes.title") }}</h1>
      <p class="mt-1 text-gray-secondary">{{ i18n.t("notes.subtitle") }}</p>
    </div>

    <Card flat padding="none" class="mb-4">
      <div class="flex flex-col gap-3 p-4 sm:flex-row sm:items-center">
        <InputText v-model="text" class="flex-grow" :placeholder="i18n.t('notes.placeholder')" @keyup.enter="addNote" />
        <Button :disabled="!text" :loading="loading" class="flex-shrink-0" @click="addNote">
          <IconMdiPlus />
          {{ i18n.t("notes.addNote") }}
        </Button>
      </div>
    </Card>

    <Card flat padding="none">
      <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <h2 class="flex-grow text-lg font-bold">{{ i18n.t("notes.notes") }}</h2>
        <span class="text-sm text-gray-secondary tabular-nums">{{ notes?.length ?? 0 }}</span>
      </div>

      <ul v-if="notes && notes.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
        <Pagination :items="notes" :items-per-page="15">
          <template #default="{ item: note }">
            <li class="flex flex-col gap-3 px-4 py-3 sm:flex-row sm:items-start">
              <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-sky-500/15 text-lg text-sky-500">
                <IconMdiNoteTextOutline />
              </div>
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center gap-2">
                  <Link :to="'/' + note.userName" class="font-semibold">{{ note.userName }}</Link>
                  <span class="text-xs text-gray-secondary">{{ i18n.d(note.createdAt, "time") }}</span>
                </div>
                <p class="mt-1 whitespace-pre-wrap break-words text-sm">{{ note.message }}</p>
              </div>
            </li>
          </template>
          <template #pagination="{ page, pages, updatePage }">
            <li class="p-3">
              <PaginationButtons :page="page" :pages="pages" @update:page="updatePage" />
            </li>
          </template>
        </Pagination>
      </ul>
      <div v-else-if="notes" class="flex flex-col items-center px-4 py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiNoteTextOutline />
        </div>
        <p class="text-gray-secondary">{{ i18n.t("notes.noNotes") }}</p>
      </div>
      <div v-else class="p-4">
        <Skeleton />
      </div>
    </Card>
  </div>
</template>
