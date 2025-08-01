import type {
  NamedPermission,
  Category,
  Prompt,
  RoleData,
  Security,
  Validations,
  CategoryData,
  PermissionData,
  VisibilityData,
  ColorData,
  FlagReasonData,
  PromptData,
} from "./backend";

export interface BackendData {
  projectCategories: Map<Category, CategoryData>;
  permissions: Map<NamedPermission, PermissionData>;
  validations: Validations;
  prompts: Map<Prompt, PromptData>;
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
  validations: Validations;
  prompts: PromptData[];
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
