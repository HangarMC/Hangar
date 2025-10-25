<script lang="ts" setup>
import { onClickOutside } from '@vueuse/core';

const i18n = useI18n();
const router = useRouter();

const query = ref('');
const showModal = ref(false);
const searchContainer = ref<HTMLElement>();
const searchInput = ref<HTMLInputElement>();

const debouncedQuery = refDebounced(query, 300);

const displayedProjects = ref();
const displayedUsers = ref();

const hasProjectResults = computed(() => (displayedProjects.value?.result.length ?? 0) > 0);
const hasUserResults = computed(() => (displayedUsers.value?.result.length ?? 0) > 0);

const projectLimit = computed(() => {
  if (!hasUserResults.value && hasProjectResults.value) return 20;
  return 7;
});

const userLimit = computed(() => {
  if (!hasProjectResults.value && hasUserResults.value) return 20;
  return 7;
});

const projectParams = computed(() => ({
  limit: projectLimit.value,
  offset: 0,
  query: debouncedQuery.value,
}));

const userParams = computed(() => ({
  limit: userLimit.value,
  offset: 0,
  query: debouncedQuery.value,
}));

const { projects, refreshProjects } = useProjects(() => projectParams.value, router);
const { users } = useUsers(() => userParams.value);

watch(projects, (newProjects) => {
  if (newProjects) {
    displayedProjects.value = newProjects;
  }
});

watch(users, (newUsers) => {
  if (newUsers) {
    displayedUsers.value = newUsers;
  }
});

watch(debouncedQuery, () => {
  if (debouncedQuery.value.length > 0) {
    refreshProjects();
  } else {
    displayedProjects.value = undefined;
    displayedUsers.value = undefined;
  }
});

onClickOutside(searchContainer, () => {
  closeModal();
});

function openModal() {
  showModal.value = true;
  nextTick(() => {
    searchInput.value?.focus();
  });
}

function closeModal() {
  showModal.value = false;
  query.value = '';
  displayedProjects.value = undefined;
  displayedUsers.value = undefined;
}

function navigateToProject(slug: string) {
  navigateTo(`/projects/${slug}`);
  closeModal();
}

function navigateToUser(username: string) {
  navigateTo(`/${username}`);
  closeModal();
}

const hasResults = computed(() => {
  return (displayedProjects.value?.result.length ?? 0) > 0 || (displayedUsers.value?.result.length ?? 0) > 0;
});
</script>

<template>
  <div>
    <!-- Search Button -->
    <button
      @click="openModal"
      class="flex items-center gap-2 min-w-80 rounded-lg px-4 py-2 dark:bg-gray-800 border border-transparent
             hover:border-gray-700 transition-all duration-200 hover:scale-[1.005] hover:text-white cursor-text"
    >
      <IconMdiMagnify class="text-gray-500" />
      <span class="text-gray-500">{{ i18n.t('hangar.globalSearch.query') }}</span>
    </button>

    <!-- Modal Overlay -->
    <Transition name="fade">
      <div
        v-if="showModal"
        class="fixed inset-0 z-50 flex items-center justify-center bg-[#000000] bg-opacity-70"
        @click.self="closeModal"
      >
        <div
          ref="searchContainer"
          class="w-full max-w-2xl h-[600px] mx-4 bg-white dark:bg-gray-900 rounded-xl shadow-lg border border-gray-200 dark:border-gray-800 overflow-hidden flex flex-col"
        >
          <!-- Search Input -->
          <div class="relative dark:border-gray-700 flex-shrink-0 bg-charcoal-900 px-3 pt-3">
            <input
              ref="searchInput"
              v-model="query"
              type="text"
              :placeholder="i18n.t('hangar.globalSearch.query')"
              class="w-full rounded-lg pl-9 px-4 py-2 dark:bg-gray-800 border border-transparent focus:border-gray-700 hover:border-gray-700"
              @keydown.esc="closeModal"
            />
            <IconMdiMagnify class="absolute top-6 left-6 text-gray-500" />
            <button v-if="query.length > 0" class="transition-all duration-250" @click="query = ''">
              <IconMdiClose class="absolute top-6 right-6 text-gray-500 hover:text-white" />
            </button>
          </div>

          <!-- Results -->
          <div class="relative flex-1 overflow-y-auto bg-charcoal-900">
            <!-- Top shadow -->
            <div class="absolute top-0 left-0 right-0 h-8 bg-gradient-to-b from-charcoal-900 to-transparent z-10 pointer-events-none"></div>
            <!-- Bottom shadow -->
            <div class="absolute bottom-0 left-0 right-0 h-4 bg-gradient-to-t from-charcoal-900 to-transparent z-10 pointer-events-none"></div>

            <!-- Results Container -->
            <div class="h-full overflow-y-auto mb-2">
              <!-- No Results / Start Typing Messages -->
              <div v-if="query.length === 0" class="h-full flex items-center justify-center text-lg text-center text-gray-500 dark:text-gray-400">
                {{ i18n.t('hangar.globalSearch.startTyping') }}
              </div>

              <!-- No Results Message -->
              <div v-else-if="!hasResults" class="h-full flex items-center justify-center text-lg text-center text-gray-500 dark:text-gray-400">
                {{ i18n.t('hangar.globalSearch.noResults') }}
              </div>

              <!-- Results Sections -->
              <div v-else>
                <!-- Projects Section -->
                <div class="mx-3.5" v-if="displayedProjects && displayedProjects.result.length > 0">
                  <h1 class="mb-2 mt-4 font-bold text-xl">
                    {{ i18n.t('hangar.globalSearch.projects') }}
                  </h1>
                  <button
                    v-for="project in displayedProjects.result"
                    :key="project.namespace.slug"
                    class="w-full px-3 py-1 mb-1 hover:scale-[1.005] hover:bg-gray-800 border border-transparent hover:border-gray-700 rounded-xl transition-all duration-200 flex items-center gap-3 text-left"
                    @click="navigateToProject(project.namespace.slug)"
                  >
                    <img
                      :src="`${project.avatarUrl}?size=32`"
                      :alt="project.name"
                      class="w-8 h-8 rounded flex-shrink-0"
                    />
                    <div class="flex-1 min-w-0">
                      <div class="font-medium text-gray-900 dark:text-gray-100 truncate">{{ project.name }}</div>
                      <div class="text-sm text-gray-500 dark:text-gray-400 truncate">{{ project.description }}</div>
                    </div>
                    <div class="flex items-center gap-1.5 text-sm text-gray-500 dark:text-gray-400 flex-shrink-0">
                      <IconMdiStar class="ml-1 -mr-1 text-primary-300" />
                      <span>{{ project.stats.stars }}</span>
                    </div>
                  </button>
                </div>

                <!-- Users Section -->
                <div class="mx-3.5" v-if="displayedUsers && displayedUsers.result.length > 0">
                  <h1 class="mb-2 mt-4 font-bold text-xl">
                    {{ i18n.t('hangar.globalSearch.users') }}
                  </h1>
                  <button
                    v-for="user in displayedUsers.result"
                    :key="user.name"
                    class="w-full px-3 py-1 mb-1 hover:scale-[1.005] hover:bg-gray-800 border border-transparent hover:border-gray-700 rounded-xl transition-all duration-200 flex items-center gap-3 text-left"
                    @click="navigateToUser(user.name)"
                  >
                    <UserAvatar
                      :username="user.name"
                      :avatar-url="user.avatarUrl"
                      size="xs"
                      :disable-link="true"
                      class="flex-shrink-0"
                    />
                    <div class="flex-1 my-2.5 min-w-0">
                      <div class="font-medium text-gray-900 dark:text-gray-100 truncate">{{ user.name }}</div>
                    </div>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>



<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
