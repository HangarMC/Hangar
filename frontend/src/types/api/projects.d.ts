declare module "hangar-api" {
  import type { Model, Named, Visible } from "hangar-api";
  import type { ProjectCategory } from "~/types/enums";

  interface ProjectNamespace {
    owner: string;
    slug: string;
  }

  interface ProjectStats {
    views: number;
    downloads: number;
    recentViews: number;
    recentDownloads: number;
    stars: number;
    watchers: number;
  }

  interface UserActions {
    starred: boolean;
    watching: boolean;
    flagged: boolean;
  }

  interface License {
    name?: string | null;
    url?: string | null;
    type?: string | null;
  }

  interface ProjectSettings {
    homepage: string | null;
    issues: string | null;
    source: string | null;
    support: string | null;
    wiki: string | null;
    license: License;
    keywords: string[];
    forumSync: boolean;
    sponsors: string | null;
    donation: {
      enable: false;
      subject: string;
    };
  }

  interface ProjectCompact extends Model, Named, Visible {
    namespace: ProjectNamespace;
    stats: ProjectStats;
    category: ProjectCategory;
    lastUpdated: Date;
    avatarUrl: string;
  }

  interface Project extends ProjectCompact {
    description: string;
    userActions: UserActions;
    settings: ProjectSettings;
    postId: number;
    topicId: number;
  }
}
