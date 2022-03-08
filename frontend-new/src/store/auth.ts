import type { HangarUser } from "hangar-internal";
import { defineStore } from "pinia";
import { ref } from "vue";
import { User } from "hangar-api";

export const useAuthStore = defineStore("auth", () => {
  const authenticated = ref<boolean>(false);
  const user = ref<HangarUser | null>(null);
  const token = ref<string | null>(null);
  const routePermissions = ref<string | null>(null);

  function setUser(newUser: User) {
    //let hangarUser: HangarUser = new Hangar();
    user.value = {
      createdAt: newUser.createdAt,
      headerData: undefined,
      id: 0,
      isOrganization: newUser.isOrganization,
      joinDate: newUser.joinDate,
      language: "",
      locked: newUser.locked,
      name: newUser.name,
      projectCount: newUser.projectCount,
      readPrompts: [],
      roles: newUser.roles,
      tagline: newUser.tagline,
    };
  }

  return { authenticated, user, token, routePermissions, setUser };
});
