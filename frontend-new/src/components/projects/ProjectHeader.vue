<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import UserAvatar from "~/components/UserAvatar.vue";
import Button from "~/components/design/Button.vue";
import Card from "~/components/design/Card.vue";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { HangarProject } from "hangar-internal";
import { UserActions } from "hangar-api";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import Tooltip from "~/components/design/Tooltip.vue";
import { useAuthStore } from "~/store/auth";

const ctx = useContext();
const i18n = useI18n();
const props = defineProps<{
  project: HangarProject;
}>();

const user = useAuthStore().user;
const slug = computed(() => props.project.namespace.owner + "/" + props.project.name);
const imageUrl = computed(() => {
  return projectIconUrl(props.project.namespace.owner, props.project.name);
});

const starred = ref(props.project.userActions.starred);
const watching = ref(props.project.userActions.watching);

const canStarOrWatch = computed(() => !user || user.name === props.project.namespace.owner);

function toggleState(stateType: keyof UserActions, route: string, i18nName: string = route) {
  useInternalApi(`projects/project/${props.project.id}/${route}/${!props.project.userActions[stateType]}`, true, "post").catch((err) =>
    handleRequestError(err, ctx, i18n, i18n.t(`project.error.${i18nName}`))
  );
}

function toggleStar() {
  toggleState("starred", "star");
  starred.value = !starred.value;
}

function toggleWatch() {
  toggleState("watching", "watch");
  watching.value = !watching.value;
}
</script>

<template>
  <Card accent>
    <div class="flex <sm:flex-col">
      <UserAvatar
        class="flex-shrink-0 mr-4 <sm:hidden"
        :username="project.namespace.owner"
        :to="'/' + project.namespace.owner + '/' + project.name"
        :img-src="imageUrl"
      />
      <div class="flex-grow sm:mr-4 <sm:mb-4 overflow-clip">
        <p class="text-2xl <sm:text-lg pb-1 inline-flex space-x-1.2 items-center">
          <UserAvatar
            class="!w-8 !h-8 sm:hidden"
            :username="project.namespace.owner"
            :to="'/' + project.namespace.owner + '/' + project.name"
            :img-src="imageUrl"
          />
          <router-link class="!sm:ml-0" :to="'/' + project.namespace.owner">{{ project.namespace.owner }}</router-link>
          <span class="text-gray-500 dark:text-gray-400"> / </span>
          <span class="font-semibold">{{ project.name }}</span>
        </p>
        <p>{{ project.description }}</p>
      </div>
      <div class="flex sm:flex-col space-y-2 items-end justify-between sm:justify-around flex-shrink-0">
        <!-- TODO: download button component with functionality -->
        <Button size="large">Download latest</Button>
        <div class="flex">
          <Tooltip>
            <template #content>
              <span v-if="canStarOrWatch">{{ i18n.t("project.info.stars", 0) }}</span>
              <span v-else-if="starred">{{ i18n.t("project.actions.unstar") }}</span>
              <span v-else>{{ i18n.t("project.actions.star") }}</span>
            </template>
            <Button button-type="gray" size="small" :disabled="canStarOrWatch" @click="toggleStar">
              <IconMdiStar v-if="starred" />
              <IconMdiStarOutline v-else />
              <span class="ml-2">{{ project.stats.stars }}</span>
            </Button>
          </Tooltip>
          <!-- Tooltips mess with normal margins so this is a workaround -->
          <div class="px-1"></div>
          <Tooltip>
            <template #content>
              <span v-if="canStarOrWatch">{{ i18n.t("project.info.watchers", 0) }}</span>
              <span v-else-if="starred">{{ i18n.t("project.actions.unwatch") }}</span>
              <span v-else>{{ i18n.t("project.actions.watch") }}</span>
            </template>
            <Button button-type="gray" size="small" :disabled="canStarOrWatch" @click="toggleWatch">
              <IconMdiBell v-if="watching" />
              <IconMdiBellOutline v-else />
              <span class="ml-2">{{ project.stats.watchers }}</span>
            </Button>
          </Tooltip>
        </div>
      </div>
    </div>
  </Card>
</template>
