declare module "hangar-api" {
  import type { Model, Named, Visible } from "hangar-api";
  import type { ProjectCategory } from "~/types/enums";
  import { Tag } from "~/types/enums";

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

  interface LinkSection {
    id: number;
    type: "top" | "sidebar";
    title: string;
    links: { id: number; name: string; url: string }[];
  }

  interface ProjectSettings {
    links: LinkSection[];
    license: License;
    tags: Tag[];
    keywords: string[];
    sponsors: string | null;
    /*donation: {
      enable: false;
      subject: string;
    };*/
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
  }
}
