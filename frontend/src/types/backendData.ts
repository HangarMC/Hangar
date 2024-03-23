import type {
  NamedPermission,
  Category,
  Platform,
  type Prompt,
  type Announcement,
  type RoleData,
  type Security,
  type Validations,
  type CategoryData,
  type PermissionData,
  type PlatformData,
  type VisibilityData,
  type ColorData,
  type FlagReasonData,
  type PromptData,
} from "~/types/backend";

export interface BackendData {
  projectCategories: Map<Category, CategoryData>;
  permissions: Map<NamedPermission, PermissionData>;
  platforms: Map<Platform, PlatformData>;
  validations: Validations;
  prompts: Map<Prompt, PromptData>;
  announcements: Announcement[];
  visibilities: VisibilityData[];
  licenses: string[];
  orgRoles: RoleData[];
  projectRoles: RoleData[];
  globalRoles: RoleData[];
  channelColors: ColorData[];
  flagReasons: FlagReasonData[];
  loggedActions: string[];
  security: Security;
}

export interface ServerBackendData {
  projectCategories: CategoryData[];
  permissions: PermissionData[];
  platforms: PlatformData[];
  validations: Validations;
  prompts: PromptData[];
  announcements: Announcement[];
  visibilities: VisibilityData[];
  licenses: string[];
  orgRoles: RoleData[];
  projectRoles: RoleData[];
  globalRoles: RoleData[];
  channelColors: ColorData[];
  flagReasons: FlagReasonData[];
  loggedActions: string[];
  security: Security;

  meta: {
    lastGenerated: string;
    apiUrl: string;
    version: number;
  };
}
