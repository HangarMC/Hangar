export function projectIconUrl(owner: string, projectName: string) {
  return `/api/internal/projects/project/${owner}/${projectName}/icon`;
}

export function avatarUrl(name: string) {
  return `/avatar/user/${name}`;
}

export function forumUrl(topicId: number) {
  return `https://forums.papermc.io/threads/` + topicId;
}
