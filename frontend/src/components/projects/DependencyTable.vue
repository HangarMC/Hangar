<script lang="ts" setup>
import { DependencyVersion, PaginatedResult, PluginDependency, Project, ProjectNamespace } from "hangar-api";
import { Platform } from "~/types/enums";
import Table from "~/lib/components/design/Table.vue";
import { useI18n } from "vue-i18n";
import Button from "~/lib/components/design/Button.vue";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import InputText from "~/lib/components/ui/InputText.vue";
import { required } from "~/lib/composables/useValidationHelpers";
import { computed, ref } from "vue";
import InputAutocomplete from "~/lib/components/ui/InputAutocomplete.vue";
import { useApi } from "~/composables/useApi";
import { useRoute } from "vue-router";
import Tabs, { Tab } from "~/lib/components/design/Tabs.vue";

const route = useRoute();
const i18n = useI18n();
const t = i18n.t;

const props = withDefaults(
  defineProps<{
    version: DependencyVersion;
    platform: Platform;
    noEditing?: boolean;
    isNew?: boolean;
  }>(),
  {
    noEditing: false,
    isNew: false,
  }
);

const results = ref<Record<string, ProjectNamespace[]>>({});
const newDepResults = ref<ProjectNamespace[][]>([]);
const newDeps = ref<PluginDependency[]>([]);
const deletedDeps = ref<string[]>([]);

function addNewDep() {
  newDeps.value.push({
    name: "",
    required: false,
    namespace: null,
    externalUrl: null,
  });
  newDepResults.value.push([]);
}

function getNamespace(namespace: ProjectNamespace) {
  return `${namespace.owner}/${namespace.slug}`;
}

function deleteDep(index: number) {
  deletedDeps.value.push(props.version.pluginDependencies[props.platform][index].name);
  newDepResults.value.splice(index, 1);
}

function deleteNewDep(index: number) {
  newDepResults.value.splice(index, 1);
  newDeps.value.splice(index, 1);
}

function reset() {
  newDeps.value.splice(0);
  newDepResults.value.splice(0);
  deletedDeps.value.splice(0);
  results.value = {};
  if (props.version.pluginDependencies[props.platform]) {
    for (const dep of props.version.pluginDependencies[props.platform]) {
      if (dep.namespace) {
        results.value[dep.name] = [dep.namespace];
      } else {
        results.value[dep.name] = [];
      }
    }
  }
}

async function onSearch(val: string, name: string) {
  if (val) {
    const projects = await useApi<PaginatedResult<Project>>(`projects?relevance=true&limit=25&offset=0&q=${val ? val.replace("/", " ") : ""}`);
    results.value[name] = projects.result
      .filter((p) => p.namespace.owner !== route.params.user || p.namespace.slug !== route.params.project)
      .map((p) => p.namespace);
  }
}

async function onNewDepSearch(val: string, index: number) {
  if (val) {
    const projects = await useApi<PaginatedResult<Project>>(`projects?relevance=true&limit=25&offset=0&q=${val.replace("/", " ")}`);
    newDepResults.value[index] = projects.result
      .filter((p) => p.namespace.owner !== route.params.user || p.namespace.slug !== route.params.project)
      .map((p) => p.namespace);
  }
}

const filteredDeps = computed(() => {
  return props.version.pluginDependencies[props.platform]?.filter((d) => !deletedDeps.value.includes(d.name)) || [];
});

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

const selectedUploadTab = ref("file");
const selectedUploadTabs: Tab[] = [
  { value: "file", header: "Hangar" },
  { value: "url", header: "URL" },
];

defineExpose({ results, newDepResults, newDeps, deletedDeps, reset: reset });
</script>

<template>
  <Table>
    <thead>
      <tr>
        <th>{{ t("general.name") }}</th>
        <th>{{ t("general.required") }}</th>
        <th>{{ t("general.link") }}</th>
        <th v-if="!noEditing">
          {{ t("general.delete") }}
        </th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(dep, index) in filteredDeps" :key="`${platform}-${dep.name}`">
        <td>{{ dep.name }}</td>
        <td><InputCheckbox v-model="dep.required" /></td>
        <td class="flex flex-wrap gap-2">
          <Tabs v-model="selectedUploadTab" :tabs="selectedUploadTabs" class="items-center" compact>
            <template #file>
              <InputAutocomplete
                :id="dep.name"
                :model-value="toString(dep.namespace)"
                :placeholder="t('version.new.form.hangarProject')"
                :values="results[dep.name]"
                :item-text="getNamespace"
                :item-value="getNamespace"
                :disabled="!!dep.externalUrl"
                @search="onSearch($event, dep.name)"
                @change="dep.externalUrl = null"
                @update:model-value="dep.namespace = fromString($event)"
              />
            </template>
            <template #url>
              <InputText
                v-model.trim="dep.externalUrl"
                :placeholder="t('version.new.form.externalUrl')"
                :disabled="dep.namespace !== null && Object.keys(dep.namespace).length !== 0"
                clearable
                @change="dep.namespace = null"
              />
            </template>
          </Tabs>
          <!-- :rules="dep.namespace !== null && Object.keys(dep.namespace).length !== 0 ? [] : [required(t('version.new.form.externalUrl'))]" -->
          <!-- todo fix validation of dependency table -->
          <!-- :rules="!!dep.externalUrl ? [] : [required(t('version.new.form.hangarProject'))]" -->
        </td>
        <td v-if="!noEditing">
          <Button icon color="error" @click="deleteDep(index)">
            <IconMdiDelete />
          </Button>
        </td>
      </tr>

      <template v-if="!noEditing">
        <tr v-for="(newDep, index) in newDeps" :key="`newDep-${newDep.name}`">
          <td>
            <InputText
              v-model.trim="newDep.name"
              dense
              hide-details
              flat
              :label="t('general.name')"
              :rules="[required(t('general.name'))]"
              :disabled="noEditing"
            />
          </td>
          <td><InputCheckbox v-model="newDep.required" :ripple="false" /></td>
          <td class="flex flex-wrap gap-2 items-center">
            <Tabs v-model="selectedUploadTab" :tabs="selectedUploadTabs" class="items-center" compact>
              <template #file>
                <InputAutocomplete
                  :id="newDep.name"
                  :model-value="toString(newDep.namespace)"
                  :placeholder="t('version.new.form.hangarProject')"
                  :values="newDepResults[index]"
                  :item-text="getNamespace"
                  :item-value="getNamespace"
                  :disabled="!!newDep.externalUrl"
                  :rules="!!newDep.externalUrl ? [] : [required(t('version.new.form.hangarProject'))]"
                  @search="onNewDepSearch($event, index)"
                  @change="newDep.externalUrl = null"
                  @update:model-value="newDep.namespace = fromString($event)"
                />
              </template>
              <template #url>
                <InputText
                  v-model.trim="newDep.externalUrl"
                  :placeholder="t('version.new.form.externalUrl')"
                  :disabled="newDep.namespace !== null && Object.keys(newDep.namespace).length !== 0"
                  :rules="newDep.namespace !== null && Object.keys(newDep.namespace).length !== 0 ? [] : [required(t('version.new.form.externalUrl'))]"
                  clearable
                  @change="newDep.namespace = null"
                />
              </template>
            </Tabs>
          </td>
          <td v-if="!noEditing">
            <Button @click="deleteNewDep(index)">
              <IconMdiDelete />
            </Button>
          </td>
        </tr>
      </template>
    </tbody>
  </Table>
  <div v-if="!noEditing" class="m-2">
    <Button block @click="addNewDep">
      <IconMdiPlus />
      {{ t("general.add") }}
    </Button>
  </div>
</template>
