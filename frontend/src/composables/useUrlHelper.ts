import { useConfig } from "~/lib/composables/useConfig";

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
  return "/linkout?remoteUrl=" + encodeURIComponent(url);
}
