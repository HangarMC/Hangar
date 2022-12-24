import { ref } from "vue";
import { NamedPermission, Platform, ProjectCategory, Prompt } from "~/types/enums";
import { Color, FlagReason, IPlatform, IProjectCategory, IPrompt, IVisibility } from "hangar-internal";
import { IPermission, Role } from "hangar-api";

declare module "hangar-api" {
  import type { Visibility } from "~/types/enums";

  interface Named {
    name: string;
  }

  interface Announcement {
    text: string;
    color: string;
  }

  interface Sponsor {
    image: string;
    name: string;
    link: string;
  }

  interface Visible {
    visibility: Visibility;
  }

  interface Validation {
    regex?: string;
    max?: number;
    min?: number;
  }

  interface VersionInfo {
    time: string;
    commit: string;
    commitShort: string;
    version: string;
    committer: string;
    message: string;
    tag: string;
    behind: string;
  }

  interface Validations {
    project: {
      name: Validation;
      desc: Validation;
      keywords: Validation;
      channels: Validation;
      pageName: Validation;
      pageContent: Validation;
      maxPageCount: number;
      maxChannelCount: number;
    };
    org: Validation;
    userTagline: Validation;
    version: Validation;
    maxOrgCount: number;
    urlRegex: string;
  }

  interface BackendData {
    meta: {
      lastGenerated: string;
      apiUrl: string;
      version: number;
    };
    projectCategories: Map<ProjectCategory, IProjectCategory>;
    permissions: Map<NamedPermission, IPermission>;
    platforms: Map<Platform, IPlatform>;
    validations: Validations;
    prompts: Map<Prompt, IPrompt>;
    announcements: Announcement[];
    visibilities: IVisibility[];
    licenses: string[];
    orgRoles: Role[];
    projectRoles: Role[];
    globalRoles: Role[];
    channelColors: Color[];
    flagReasons: FlagReason[];
    loggedActions: string[];
  }
}
