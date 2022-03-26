<script setup lang="ts">
import { HangarProject } from "hangar-internal";
import Card from "~/components/design/Card.vue";
import { useI18n } from "vue-i18n";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import NewPageModal from "~/components/modals/NewPageModal.vue";
import TreeView from "~/components/TreeView.vue";
import Link from "~/components/design/Link.vue";
import { useRoute } from "vue-router";

const props = defineProps<{
  project: HangarProject;
}>();

const i18n = useI18n();
const route = useRoute();
</script>

<template>
  <Card>
    <template #header>
      <NewPageModal v-if="hasPerms(NamedPermission.EDIT_PAGE)" :pages="project.pages" :project-id="project.id" activator-class="mr-2" />
      {{ i18n.t("page.plural") }}
    </template>

    <TreeView :items="project.pages" item-key="slug">
      <template #item="{ item }">
        <Link v-if="item.home" :to="`/${route.params.user}/${route.params.project}`" exact><IconMdiHome /> {{ item.name }}</Link>
        <Link v-else :to="`/${route.params.user}/${route.params.project}/pages/${item.slug}`" exact> {{ item.name }}</Link>
      </template>
    </TreeView>
  </Card>
</template>
