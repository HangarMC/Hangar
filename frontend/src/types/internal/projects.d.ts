declare module "hangar-internal" {
  import type { Joinable, Table } from "hangar-internal";
  import type { Project, ProjectNamespace } from "hangar-api";
  import type { ProjectCategory, Visibility } from "~/types/enums";

  interface ProjectOwner {
    id: number;
    name: string;
    userId: number;
    isOrganization: boolean;
  }

  interface HangarProjectInfo {
    publicVersions: number;
    flagCount: number;
    noteCount: number;
    starCount: number;
    watcherCount: number;
  }

  interface HangarProjectPage {
    id: number;
    name: string;
    slug: string;
    home: boolean;
    children: HangarProjectPage[];
  }

  interface HangarProject extends Joinable, Project, Table {
    lastVisibilityChangeComment: string;
    lastVisibilityChangeUserName: string;
    info: HangarProjectInfo;
    pages: HangarProjectPage[];
    recommendedVersions: Record<string, string | null>;
  }

  interface ProjectPage extends Table {
    name: string;
    slug: string;
    contents: string;
    deletable: boolean;
    isHome: boolean;
  }

  interface Flag extends Table {
    userId: number; //
    reportedByName: string; //
    reason: string; //
    resolved: boolean; //
    comment: string; //
    resolvedAt: string | null; //
    resolvedBy: number | null; //
    resolvedByName: string | null; //
    projectId: number; //
    projectNamespace: ProjectNamespace;
    projectVisibility: Visibility;
  }

  interface Note extends Table {
    projectId: number;
    message: string;
    userId: number;
    userName: string | null;
  }

  interface ProjectSettingsForm {
    settings: {
      homepage: string | null;
      issues: string | null;
      source: string | null;
      support: string | null;
      keywords: string[];
      license: {
        type?: string | null;
        url: string | null;
        name: string | null;
      };
      donation: {
        enable: false;
        email: string | null;
        defaultAmount: number;
        oneTimeAmounts: Array<string>;
        monthlyAmounts: Array<string>;
      };
      forumSync: false;
    };
    category: ProjectCategory;
    description: string;
  }

  interface ProjectApproval {
    projectId: number;
    namespace: ProjectNamespace;
    visibility: Visibility;
    comment: string;
    changeRequester: string;
  }
}
