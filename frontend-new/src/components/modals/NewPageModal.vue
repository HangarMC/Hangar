<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { useBackendDataStore } from "~/store/backendData";
import { useContext } from "vite-ssr/vue";
import { HangarProjectPage } from "hangar-internal";
import { computed, ref, watch } from "vue";

const props = defineProps<{
  projectId: number;
  pages: HangarProjectPage[];
}>();

const i18n = useI18n();
const ctx = useContext();
const backendData = useBackendDataStore();

const pageRoots = computed(() => flatDeep(props.pages));
const name = ref("");
const parent = ref<number | null>(null);

watch(name, () => {
  // todo validate name
});

// TODO these should be sorted into somewhat of an order
function flatDeep(pages: HangarProjectPage[]): HangarProjectPage[] {
  let ps: HangarProjectPage[] = [];
  for (const page of pages) {
    if (page.children.length > 0) {
      ps = [...ps, ...flatDeep(page.children)];
    }
    ps.push(page);
  }
  return ps;
}

function createPage() {
  // todo create page
}
</script>

<template>
  <Modal :title="i18n.t('page.new.title')">
    <template #default="{ on }">
      <!-- todo new page modal -->

      <Button class="mt-2" v-on="on">{{ i18n.t("general.close") }}</Button>
      <Button class="mt-2 ml-2" @click="createPage">{{ i18n.t("general.create") }}</Button>
    </template>
    <template #activator="{ on }">
      <Button v-bind="$attrs" class="mr-1" v-on="on">
        <IconMdiPlus />
      </Button>
    </template>
  </Modal>
</template>
