<script lang="ts" setup>
import type { DependencyVersion, PaginatedResult, PluginDependency, Project, ProjectNamespace } from "hangar-api";
import { useI18n } from "vue-i18n";
import type { Platform } from "~/types/enums";
import Table from "~/components/design/Table.vue";
import Button from "~/components/design/Button.vue";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import InputText from "~/components/ui/InputText.vue";
import { required } from "~/composables/useValidationHelpers";
import InputAutocomplete from "~/components/ui/InputAutocomplete.vue";
import { useApi } from "~/composables/useApi";
import Tabs from "~/components/design/Tabs.vue";
import { ref, useRoute } from "#imports";
import type { Tab } from "~/types/components/design/Tabs";

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
  if (dependency.externalUrl) {
    selectedTab.value.push("url");
  } else if (dependency.namespace) {
    selectedTab.value.push("file");
  } else {
    selectedTab.value.push("url");
  }
  completionResults.value.push([]);
}

function addDep() {
  selectedTab.value.push("url");
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
    const projects = await useApi<PaginatedResult<Project>>(`projects?limit=25&offset=0&q=${val.replace("/", " ")}`);
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
        <th>{{ t("version.new.form.linkOrProject") }}</th>
        <th>{{ t("general.name") }}</th>
        <th>{{ t("general.required") }}</th>
        <th v-if="!noEditing" />
      </tr>
    </thead>
    <tbody>
      <tr v-for="(dep, index) in dependencies" :key="`${platform}-${index}`">
        <td class="flex flex-wrap gap-2">
          <Tabs v-model="selectedTab[index]" :tabs="selectedUploadTabs" class="items-center -ml-2" compact @update:model-value="changeTabs($event, index)">
            <template #file>
              <InputAutocomplete
                :id="dep.name || 'new'"
                :model-value="toString(dep.namespace)"
                :placeholder="t('version.new.form.hangarProject')"
                :values="completionResults[index]"
                :item-text="getNamespace"
                :item-value="getNamespace"
                :disabled="!!dep.externalUrl || noEditing"
                :rules="!!dep.externalUrl ? [] : [required(t('version.new.form.hangarProject'))]"
                @search="onSearch($event, index)"
                @change="dep.externalUrl = null"
                @update:model-value="
                  dep.namespace = fromString($event);
                  dep.name = dep.namespace?.slug;
                "
              />
            </template>
            <template #url>
              <InputText
                v-model.trim="dep.externalUrl"
                :placeholder="t('version.new.form.externalUrl')"
                :disabled="(dep.namespace && Object.keys(dep.namespace).length !== 0) || noEditing"
                :rules="dep.namespace && Object.keys(dep.namespace).length !== 0 ? [] : [required(t('version.new.form.externalUrl'))]"
                clearable
                @change="dep.namespace = null"
              />
            </template>
          </Tabs>
        </td>
        <td v-if="selectedTab[index] === 'url'">
          <InputText v-model.trim="dep.name" dense hide-details flat :label="t('general.name')" :rules="[required(t('general.name'))]" :disabled="noEditing" />
        </td>
        <td v-else />
        <td><InputCheckbox v-model="dep.required" :ripple="false" :disabled="noEditing" /></td>
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
