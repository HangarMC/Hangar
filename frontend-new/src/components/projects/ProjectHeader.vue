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

// TODO: use a permission check? idk which one to use
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
    <div class="flex">
      <UserAvatar :username="project.namespace.owner" :to="'/' + project.namespace.owner + '/' + project.name" :img-src="imageUrl"></UserAvatar>
      <div class="flex-grow mx-4">
        <p class="text-2xl pb-1">
          <router-link :to="'/' + project.namespace.owner">{{ project.namespace.owner }}</router-link>
          <span class="text-gray-500 dark:text-gray-400"> / </span>
          <span class="font-semibold">{{ project.name }}</span>
        </p>
        <p>{{ project.description }}</p>
      </div>
      <div class="flex flex-col items-end justify-between">
        <Button size="large">Download latest</Button>
        <div class="flex">
          <Tooltip>
            <template #content>
              <span v-if="canStarOrWatch">{{ i18n.t("project.info.stars", 0) }}</span>
              <span v-else-if="starred">{{ i18n.t("project.actions.unstar") }}</span>
              <span v-else>{{ i18n.t("project.actions.star") }}</span>
            </template>
            <Button size="small" class="pb-0" :disabled="canStarOrWatch" @click="toggleStar">
              <span class="inline-flex items-center mx-1">
                <IconMdiStar v-if="starred" />
                <IconMdiStarOutline v-else />
                <span class="ml-2">{{ project.stats.stars }}</span>
              </span>
            </Button>
          </Tooltip>
          <Tooltip>
            <template #content>
              <span v-if="canStarOrWatch">{{ i18n.t("project.info.watchers", 0) }}</span>
              <span v-else-if="starred">{{ i18n.t("project.actions.unwatch") }}</span>
              <span v-else>{{ i18n.t("project.actions.watch") }}</span>
            </template>
            <Button size="small" class="pb-0" :disabled="canStarOrWatch" @click="toggleWatch">
              <span class="inline-flex items-center mx-1">
                <IconMdiBell v-if="watching" />
                <IconMdiBellOutline v-else />
                <span class="ml-2">{{ project.stats.watchers }}</span>
              </span>
            </Button>
          </Tooltip>
        </div>
      </div>
    </div>
  </Card>
</template>
