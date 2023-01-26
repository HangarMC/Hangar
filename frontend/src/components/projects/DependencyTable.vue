<script lang="ts" setup>
import { DependencyVersion, PaginatedResult, PluginDependency, Project, ProjectNamespace } from "hangar-api";
import { useI18n } from "vue-i18n";
import { Platform } from "~/types/enums";
import Table from "~/lib/components/design/Table.vue";
import Button from "~/lib/components/design/Button.vue";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import InputText from "~/lib/components/ui/InputText.vue";
import { required } from "~/lib/composables/useValidationHelpers";
import InputAutocomplete from "~/lib/components/ui/InputAutocomplete.vue";
import { useApi } from "~/composables/useApi";
import Tabs from "~/lib/components/design/Tabs.vue";
import { ref, useRoute } from "#imports";
import { Tab } from "~/lib/types/components/design/Tabs";

const route = useRoute();
const i18n = useI18n();
const t = i18n.t;

const props = withDefaults(
  defineProps<{
    version: DependencyVersion;
    platform: Platform;
    noEditing?: boolean;
  }>(),
  {
    noEditing: false,
  }
);

const completionResults = ref<ProjectNamespace[][]>([]);
const dependencies = ref<PluginDependency[]>(props.version.pluginDependencies[props.platform] ? [...props.version.pluginDependencies[props.platform]] : []);
const selectedTab = ref<string[]>([]);
for (let i = 0; i < dependencies.value.length; i++) {
  const dependency = dependencies.value[i];
  selectedTab.value.push(dependency.externalUrl !== null ? "url" : "file");
  completionResults.value.push([]);
}

function addDep() {
  selectedTab.value.push("file");
  dependencies.value.push({
    name: "",
    required: false,
    namespace: null,
    externalUrl: null,
  });
}

function deleteDep(index: number) {
  selectedTab.value.splice(index, 1);
  dependencies.value.splice(index, 1);
  completionResults.value.splice(index, 1);
}

function getNamespace(namespace: ProjectNamespace) {
  return `${namespace.owner}/${namespace.slug}`;
}

function reset() {
  completionResults.value.splice(0);
  dependencies.value.splice(0);
  selectedTab.value.splice(0);
}

async function onSearch(val: string, index: number) {
  if (val) {
    const projects = await useApi<PaginatedResult<Project>>(`projects?relevance=true&limit=25&offset=0&q=${val.replace("/", " ")}`);
    completionResults.value[index] = projects.result
      .filter((p) => p.namespace.owner !== route.params.user || p.namespace.slug !== route.params.project)
      .map((p) => p.namespace);
  }
}

function toString(namespace: ProjectNamespace | string) {
  if (!namespace) return "";
  if (typeof namespace === "string") return namespace;
  return namespace.owner + "/" + namespace.slug;
}

function fromString(string: string): ProjectNamespace | string | null {
  const split = string.split("/");
  return split.length !== 2
    ? string
    : {
        owner: split[0],
        slug: split[1],
      };
}

const selectedUploadTabs: Tab[] = [
  { value: "file", header: "Hangar" },
  { value: "url", header: "URL" },
];

function changeTabs(val: string, idx: number) {
  if (val === "file") {
    dependencies.value[idx].externalUrl = null;
  } else if (val === "url") {
    dependencies.value[idx].namespace = null;
  }
}

defineExpose({ dependencies, reset });
</script>

<template>
  <Table v-if="dependencies.length !== 0">
    <thead>
      <tr>
        <th>{{ t("general.name") }}</th>
        <th>{{ t("general.required") }}</th>
        <th>{{ t("general.link") }}</th>
        <th v-if="!noEditing" />
      </tr>
    </thead>
    <tbody>
      <tr v-for="(dep, index) in dependencies" :key="`${platform}-${index}`">
        <td>
          <InputText
            v-if="selectedTab[index] !== 'url'"
            v-model.trim="dep.namespace.slug"
            dense
            hide-details
            flat
            :label="t('general.name')"
            :rules="[required(t('general.name'))]"
            disabled
          />
          <InputText
            v-else
            v-model.trim="dep.name"
            dense
            hide-details
            flat
            :label="t('general.name')"
            :rules="[required(t('general.name'))]"
            :disabled="noEditing"
          />
        </td>
        <td><InputCheckbox v-model="dep.required" :ripple="false" :disabled="noEditing" /></td>
        <td class="flex flex-wrap gap-2">
          <Tabs v-model="selectedTab[index]" :tabs="selectedUploadTabs" class="items-center -ml-2" compact @update:model-value="changeTabs($event, index)">
            <template #file>
              <InputAutocomplete
                :id="dep.name"
                :model-value="toString(dep.namespace)"
                :placeholder="t('version.new.form.hangarProject')"
                :values="completionResults[index]"
                :item-text="getNamespace"
                :item-value="getNamespace"
                :disabled="!!dep.externalUrl || noEditing"
                :rules="!!dep.externalUrl ? [] : [required(t('version.new.form.hangarProject'))]"
                @search="onSearch($event, index)"
                @change="dep.externalUrl = null"
                @update:model-value="dep.namespace = fromString($event)"
              />
              <!-- todo rules good? -->
            </template>
            <template #url>
              <InputText
                v-model.trim="dep.externalUrl"
                :placeholder="t('version.new.form.externalUrl')"
                :disabled="(dep.namespace !== null && Object.keys(dep.namespace).length !== 0) || noEditing"
                :rules="dep.namespace !== null && Object.keys(dep.namespace).length !== 0 ? [] : [required(t('version.new.form.externalUrl'))]"
                clearable
                @change="dep.namespace = null"
              />
            </template>
          </Tabs>
        </td>
        <td v-if="!noEditing">
          <Button button-type="red" @click="deleteDep(index)"><IconMdiDelete /></Button>
        </td>
      </tr>
    </tbody>
  </Table>
  <div v-if="!noEditing" class="m-2" :class="dependencies.length !== 0 ? '-mt-2' : ''">
    <Button block @click="addDep">
      <IconMdiPlus />
      {{ t("general.add") }}
    </Button>
  </div>
</template>
