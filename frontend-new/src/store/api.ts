import { defineStore } from "pinia";
import { ref } from "vue";
import { PaginatedResult, Project } from "hangar-api";
import { fetchIfNeeded, useApi } from "~/composables/useApi";

export const useApiStore = defineStore("api", () => {
  const projects = ref<PaginatedResult<Project> | null>(null);
  async function loadProjects(pagination = { limit: 25, offset: 0 }) {
    return await fetchIfNeeded(() => useApi<PaginatedResult<Project>>("projects", true, "get", pagination), projects);
  }

  return { projects, loadProjects };
});
