import { useConfig } from "~/lib/composables/useConfig";

export function projectIconUrl(owner: string, projectName: string, proxy = true) {
  const url = `/api/internal/projects/project/${owner}/${projectName}/icon`;
  const config = useConfig();
  return proxy ? config.authHost + "/image/" + config.publicHost + url : url;
}

export function avatarUrl(name: string) {
  return `${useConfig().authHost}/avatar/user/${name}`;
}

export function authUrl(user: string) {
  return useConfig().authHost + "/" + user;
}

export function forumUrl(topicId: number) {
  return `https://forums.papermc.io/threads/` + topicId;
}

export function forumUserUrl(name: number | string) {
  // TODO fixme?
  return `https://forums.papermc.io/members/` + name;
}

export function linkout(url: string) {
  return "/linkout?remoteUrl=" + url;
}
