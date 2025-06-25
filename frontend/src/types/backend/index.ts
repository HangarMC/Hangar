// noinspection JSValidateJSDoc,JSUnusedGlobalSymbols

export enum NotificationType {
  Neutral = "neutral",
  Success = "success",
  Info = "info",
  Warning = "warning",
  Error = "error",
}

export enum InviteType {
  Project = "project",
  Organization = "organization",
}

export enum Type {
  CHANNEL = "CHANNEL",
  VERSION = "VERSION",
}

export enum Context {
  PROJECT = "PROJECT",
  VERSION = "VERSION",
  PAGE = "PAGE",
  USER = "USER",
  ORGANIZATION = "ORGANIZATION",
}

export enum ProjectRole {
  PROJECT_SUPPORT = "PROJECT_SUPPORT",
  PROJECT_EDITOR = "PROJECT_EDITOR",
  PROJECT_DEVELOPER = "PROJECT_DEVELOPER",
  PROJECT_MAINTAINER = "PROJECT_MAINTAINER",
  PROJECT_ADMIN = "PROJECT_ADMIN",
  PROJECT_OWNER = "PROJECT_OWNER",
}

export enum OrganizationRole {
  ORGANIZATION_SUPPORT = "ORGANIZATION_SUPPORT",
  ORGANIZATION_EDITOR = "ORGANIZATION_EDITOR",
  ORGANIZATION_DEVELOPER = "ORGANIZATION_DEVELOPER",
  ORGANIZATION_MAINTAINER = "ORGANIZATION_MAINTAINER",
  ORGANIZATION_ADMIN = "ORGANIZATION_ADMIN",
  ORGANIZATION_OWNER = "ORGANIZATION_OWNER",
}

/**
 * The visibility of a project or version
 * @example "PUBLIC"
 */
export enum Visibility {
  Public = "public",
  New = "new",
  NeedsChanges = "needsChanges",
  NeedsApproval = "needsApproval",
  SoftDelete = "softDelete",
}

export enum ReviewState {
  Unreviewed = "unreviewed",
  Reviewed = "reviewed",
  UnderReview = "under_review",
  PartiallyReviewed = "partially_reviewed",
}

export enum FlagReason {
  inappropriateContent = "project.flag.flags.inappropriateContent",
  impersonation = "project.flag.flags.impersonation",
  spam = "project.flag.flags.spam",
  malIntent = "project.flag.flags.malIntent",
  other = "project.flag.flags.other",
}

export enum Category {
  AdminTools = "admin_tools",
  Chat = "chat",
  DevTools = "dev_tools",
  Economy = "economy",
  Gameplay = "gameplay",
  Games = "games",
  Protection = "protection",
  RolePlaying = "role_playing",
  WorldManagement = "world_management",
  Misc = "misc",
  Undefined = "undefined",
}

export enum ReviewAction {
  START = "START",
  MESSAGE = "MESSAGE",
  STOP = "STOP",
  REOPEN = "REOPEN",
  APPROVE = "APPROVE",
  PARTIALLY_APPROVE = "PARTIALLY_APPROVE",
  UNDO_APPROVAL = "UNDO_APPROVAL",
}

export enum Prompt {
  CHANGE_AVATAR = "CHANGE_AVATAR",
}

export enum Category {
  Server = "Server",
  Proxy = "Proxy",
}

/**
 * Server platform
 * @example "PAPER"
 */
export enum Platform {
  PAPER = "PAPER",
  WATERFALL = "WATERFALL",
  VELOCITY = "VELOCITY",
}

export enum PermissionType {
  Global = "global",
  Project = "project",
  Organization = "organization",
}

export enum NamedPermission {
  ViewPublicInfo = "view_public_info",
  EditOwnUserSettings = "edit_own_user_settings",
  EditApiKeys = "edit_api_keys",
  EditSubjectSettings = "edit_subject_settings",
  ManageSubjectMembers = "manage_subject_members",
  IsSubjectOwner = "is_subject_owner",
  IsSubjectMember = "is_subject_member",
  CreateProject = "create_project",
  EditPage = "edit_page",
  DeleteProject = "delete_project",
  CreateVersion = "create_version",
  EditVersion = "edit_version",
  DeleteVersion = "delete_version",
  EditChannels = "edit_channels",
  CreateOrganization = "create_organization",
  DeleteOrganization = "delete_organization",
  PostAsOrganization = "post_as_organization",
  ModNotesAndFlags = "mod_notes_and_flags",
  SeeHidden = "see_hidden",
  IsStaff = "is_staff",
  Reviewer = "reviewer",
  ViewHealth = "view_health",
  ViewIp = "view_ip",
  ViewStats = "view_stats",
  ViewLogs = "view_logs",
  ManualValueChanges = "manual_value_changes",
  RestoreVersion = "restore_version",
  RestoreProject = "restore_project",
  HardDeleteProject = "hard_delete_project",
  HardDeleteVersion = "hard_delete_version",
  EditAllUserSettings = "edit_all_user_settings",
}

export enum Color {
  ValueD946Ef = "#d946ef",
  ValueA855F7 = "#a855f7",
  Value8B5Cf6 = "#8b5cf6",
  Value6366F1 = "#6366f1",
  Value3B82F6 = "#3b82f6",
  Value0Ea5E9 = "#0ea5e9",
  Value06B6D4 = "#06b6d4",
  Value14B8A6 = "#14b8a6",
  Value34D399 = "#34d399",
  Value22C55E = "#22c55e",
  Value84Cc16 = "#84cc16",
  ValueEab308 = "#eab308",
  ValueF59E0B = "#f59e0b",
  ValueF97316 = "#f97316",
  ValueEf4444 = "#ef4444",
  Value78716C = "#78716c",
  ValueA9A9A9 = "#A9A9A9",
  Transparent = "transparent",
}

export enum ChannelFlag {
  FROZEN = "FROZEN",
  UNSTABLE = "UNSTABLE",
  PINNED = "PINNED",
  SENDS_NOTIFICATIONS = "SENDS_NOTIFICATIONS",
  HIDE_BY_DEFAULT = "HIDE_BY_DEFAULT",
}

export enum PinnedStatus {
  NONE = "NONE",
  VERSION = "VERSION",
  CHANNEL = "CHANNEL",
}

export enum Tag {
  ADDON = "ADDON",
  LIBRARY = "LIBRARY",
  SUPPORTS_FOLIA = "SUPPORTS_FOLIA",
}

export enum InviteStatus {
  ACCEPT = "ACCEPT",
  DECLINE = "DECLINE",
}

export enum JobType {
  SEND_EMAIL = "SEND_EMAIL",
  SEND_WEBHOOK = "SEND_WEBHOOK",
  SCHEDULED_TASK = "SCHEDULED_TASK",
}

export enum OAuthMode {
  LOGIN = "LOGIN",
  SIGNUP = "SIGNUP",
  SETTINGS = "SETTINGS",
}

export enum CredentialType {
  PASSWORD = "PASSWORD",
  BACKUP_CODES = "BACKUP_CODES",
  TOTP = "TOTP",
  WEBAUTHN = "WEBAUTHN",
  OAUTH = "OAUTH",
}

export type JsonNode = any;

export interface RenameRequest {
  displayName: string;
  id: string;
}

export interface BackupCode {
  code: string;
  /** @format date-time */
  used_at: string;
}

export interface OAuthConnection {
  id: string;
  name: string;
  provider: string;
}

export interface AccountForm {
  /** @minLength 1 */
  currentPassword: string;
  /** @minLength 1 */
  email: string;
  newPassword: string;
  /** @minLength 1 */
  username: string;
}

export interface OAuthSignupForm {
  email: string;
  jwt: string;
  tos: boolean;
  username: string;
}

export interface OAuthSignupResponse {
  emailVerificationNeeded: boolean;
}

export interface ResetForm {
  code: string;
  /** @minLength 1 */
  email: string;
  password: string;
}

export interface SettingsResponse {
  authenticators: Authenticator[];
  emailConfirmed: boolean;
  emailPending: boolean;
  hasBackupCodes: boolean;
  hasPassword: boolean;
  hasTotp: boolean;
  oauthConnections: OAuthConnection[];
}

export interface Authenticator {
  addedAt: string;
  displayName: string;
  id: string;
}

export interface SignupForm {
  captcha: string;
  email: string;
  password: string;
  tos: boolean;
  username: string;
}

export interface TotpForm {
  code: string;
  secret: string;
}

export interface TotpSetupResponse {
  qrCode: string;
  secret: string;
}

export interface LoginBackupForm {
  backupCode: string;
  password: string;
  usernameOrEmail: string;
}

export interface LoginPasswordForm {
  password: string;
  usernameOrEmail: string;
}

export interface LoginResponse {
  /** @format int32 */
  aal: number;
  types: CredentialType[];
  user: HangarUser;
}

export interface LoginTotpForm {
  password: string;
  totpCode: string;
  usernameOrEmail: string;
}

export interface LoginWebAuthNForm {
  password: string;
  publicKeyCredentialJson: string;
  usernameOrEmail: string;
}

export interface Webhook {
  canceledBy: string;
  details: Details;
  duration: string;
  enqueuedAt: string;
  error: Error;
  /** @format date-time */
  finishedAt: string;
  indexUid: string;
  /** @format date-time */
  startedAt: string;
  status: string;
  type: string;
  uid: string;
}

export interface Details {
  /** @format int32 */
  indexedDocuments: number;
  /** @format int32 */
  receivedDocuments: number;
}

export interface Error {
  code: string;
  link: string;
  message: string;
  type: string;
}

export interface JobTable {
  /** @format date-time */
  createdAt: string;
  /** @format int64 */
  id: number;
  jobProperties: JsonNode;
  jobType: JobType;
  lastError: string;
  lastErrorDescriptor: string;
  /** @format date-time */
  lastUpdated: string;
  /** @format date-time */
  retryAt: string;
  state: JobState;
}

export interface CategoryData {
  apiName: string;
  icon: string;
  title: string;
  visible: boolean;
}

export interface ColorData {
  hex: string;
  name: string;
}

export interface FlagReasonData {
  title: string;
  type: string;
}

export interface GlobalData {
  globalNotifications: Record<string, string>;
}

export interface PermissionData {
  frontendName: string;
  permission: bigint;
  value: string;
}

export interface PlatformData {
  category: Category;
  enumName: Platform;
  name: string;
  platformVersions: PlatformVersion[];
  url: string;
  visible: boolean;
}

export interface PromptData {
  messageKey: string;
  name: string;
  titleKey: string;
}

export interface VersionInfo {
  behind: string;
  commit: string;
  commitShort: string;
  committer: string;
  message: string;
  tag: string;
  time: string;
  version: string;
}

export interface VisibilityData {
  canChangeTo: boolean;
  cssClass: string;
  name: string;
  showModal: boolean;
  title: string;
}

export interface Invites {
  organization: HangarOrganizationInvite[];
  project: HangarProjectInvite[];
}

export interface CreateUserRequest {
  admin: boolean;
  email: string;
  password: string;
  username: string;
}

export interface ProjectApprovals {
  needsApproval: HangarProjectApproval[];
  waitingProjects: HangarProjectApproval[];
}

export interface ReviewQueue {
  notStarted: HangarReviewQueueEntry[];
  underReview: HangarReviewQueueEntry[];
}

export interface JobState {
  null: boolean;
  type?: string;
  value?: string;
}

export interface HangarApiException {
  args?: Record<string, any>[];
  body?: ProblemDetail;
  cause?: {
    localizedMessage?: string;
    message?: string;
    stackTrace?: {
      classLoaderName?: string;
      className?: string;
      fileName?: string;
      /** @format int32 */
      lineNumber?: number;
      methodName?: string;
      moduleName?: string;
      moduleVersion?: string;
      nativeMethod?: boolean;
    }[];
  };
  detail?: string;
  detailMessageArguments?: Record<string, any>[];
  detailMessageCode?: string;
  headers?: {
    accept?: MediaType[];
    acceptCharset?: {
      registered?: boolean;
    }[];
    acceptLanguage?: {
      range?: string;
      /** @format double */
      weight?: number;
    }[];
    acceptLanguageAsLocales?: string[];
    acceptPatch?: MediaType[];
    accessControlAllowCredentials?: boolean;
    accessControlAllowHeaders?: string[];
    accessControlAllowMethods?: HttpMethod[];
    accessControlAllowOrigin?: string;
    accessControlExposeHeaders?: string[];
    /** @format int64 */
    accessControlMaxAge?: number;
    accessControlRequestHeaders?: string[];
    accessControlRequestMethod?: HttpMethod;
    all?: Record<string, string>;
    /** @uniqueItems true */
    allow?: HttpMethod[];
    basicAuth?: string;
    bearerAuth?: string;
    cacheControl?: string;
    connection?: string[];
    contentDisposition?: ContentDisposition;
    contentLanguage?: string;
    /** @format int64 */
    contentLength?: number;
    contentType?: MediaType;
    /** @format int64 */
    date?: number;
    empty?: boolean;
    etag?: string;
    /** @format int64 */
    expires?: number;
    host?: {
      address?: {
        /** @format byte */
        address?: string;
        anyLocalAddress?: boolean;
        canonicalHostName?: string;
        hostAddress?: string;
        hostName?: string;
        linkLocalAddress?: boolean;
        loopbackAddress?: boolean;
        mcglobal?: boolean;
        mclinkLocal?: boolean;
        mcnodeLocal?: boolean;
        mcorgLocal?: boolean;
        mcsiteLocal?: boolean;
        multicastAddress?: boolean;
        siteLocalAddress?: boolean;
      };
      hostName?: string;
      hostString?: string;
      /** @format int32 */
      port?: number;
      unresolved?: boolean;
    };
    ifMatch?: string[];
    /** @format int64 */
    ifModifiedSince?: number;
    ifNoneMatch?: string[];
    /** @format int64 */
    ifUnmodifiedSince?: number;
    /** @format int64 */
    lastModified?: number;
    /** @format uri */
    location?: string;
    origin?: string;
    pragma?: string;
    range?: HttpRange[];
    upgrade?: string;
    vary?: string[];
    [key: string]: any;
  };
  /** @format uri */
  instance?: string;
  localizedMessage?: string;
  message?: string;
  mostSpecificCause?: {
    localizedMessage?: string;
    message?: string;
    stackTrace?: {
      classLoaderName?: string;
      className?: string;
      fileName?: string;
      /** @format int32 */
      lineNumber?: number;
      methodName?: string;
      moduleName?: string;
      moduleVersion?: string;
      nativeMethod?: boolean;
    }[];
  };
  reason?: string;
  responseHeaders?: {
    accept?: MediaType[];
    acceptCharset?: {
      registered?: boolean;
    }[];
    acceptLanguage?: {
      range?: string;
      /** @format double */
      weight?: number;
    }[];
    acceptLanguageAsLocales?: string[];
    acceptPatch?: MediaType[];
    accessControlAllowCredentials?: boolean;
    accessControlAllowHeaders?: string[];
    accessControlAllowMethods?: HttpMethod[];
    accessControlAllowOrigin?: string;
    accessControlExposeHeaders?: string[];
    /** @format int64 */
    accessControlMaxAge?: number;
    accessControlRequestHeaders?: string[];
    accessControlRequestMethod?: HttpMethod;
    all?: Record<string, string>;
    /** @uniqueItems true */
    allow?: HttpMethod[];
    basicAuth?: string;
    bearerAuth?: string;
    cacheControl?: string;
    connection?: string[];
    contentDisposition?: ContentDisposition;
    contentLanguage?: string;
    /** @format int64 */
    contentLength?: number;
    contentType?: MediaType;
    /** @format int64 */
    date?: number;
    empty?: boolean;
    etag?: string;
    /** @format int64 */
    expires?: number;
    host?: {
      address?: {
        /** @format byte */
        address?: string;
        anyLocalAddress?: boolean;
        canonicalHostName?: string;
        hostAddress?: string;
        hostName?: string;
        linkLocalAddress?: boolean;
        loopbackAddress?: boolean;
        mcglobal?: boolean;
        mclinkLocal?: boolean;
        mcnodeLocal?: boolean;
        mcorgLocal?: boolean;
        mcsiteLocal?: boolean;
        multicastAddress?: boolean;
        siteLocalAddress?: boolean;
      };
      hostName?: string;
      hostString?: string;
      /** @format int32 */
      port?: number;
      unresolved?: boolean;
    };
    ifMatch?: string[];
    /** @format int64 */
    ifModifiedSince?: number;
    ifNoneMatch?: string[];
    /** @format int64 */
    ifUnmodifiedSince?: number;
    /** @format int64 */
    lastModified?: number;
    /** @format uri */
    location?: string;
    origin?: string;
    pragma?: string;
    range?: HttpRange[];
    upgrade?: string;
    vary?: string[];
    [key: string]: any;
  };
  rootCause?: {
    cause?: {
      localizedMessage?: string;
      message?: string;
      stackTrace?: {
        classLoaderName?: string;
        className?: string;
        fileName?: string;
        /** @format int32 */
        lineNumber?: number;
        methodName?: string;
        moduleName?: string;
        moduleVersion?: string;
        nativeMethod?: boolean;
      }[];
    };
    localizedMessage?: string;
    message?: string;
    stackTrace?: {
      classLoaderName?: string;
      className?: string;
      fileName?: string;
      /** @format int32 */
      lineNumber?: number;
      methodName?: string;
      moduleName?: string;
      moduleVersion?: string;
      nativeMethod?: boolean;
    }[];
    suppressed?: {
      localizedMessage?: string;
      message?: string;
      stackTrace?: {
        classLoaderName?: string;
        className?: string;
        fileName?: string;
        /** @format int32 */
        lineNumber?: number;
        methodName?: string;
        moduleName?: string;
        moduleVersion?: string;
        nativeMethod?: boolean;
      }[];
    }[];
  };
  stackTrace?: {
    classLoaderName?: string;
    className?: string;
    fileName?: string;
    /** @format int32 */
    lineNumber?: number;
    methodName?: string;
    moduleName?: string;
    moduleVersion?: string;
    nativeMethod?: boolean;
  }[];
  statusCode?: HttpStatusCode;
  suppressed?: {
    localizedMessage?: string;
    message?: string;
    stackTrace?: {
      classLoaderName?: string;
      className?: string;
      fileName?: string;
      /** @format int32 */
      lineNumber?: number;
      methodName?: string;
      moduleName?: string;
      moduleVersion?: string;
      nativeMethod?: boolean;
    }[];
  }[];
  title?: string;
  titleMessageCode?: string;
  /** @format uri */
  type?: string;
  typeMessageCode?: string;
}

export interface HangarValidationException {
  fieldErrors?: FieldError[];
  globalErrors?: GlobalError[];
  httpError?: HttpError;
  isHangarValidationException?: boolean;
  message?: string;
  object?: string;
}

export interface FieldError {
  code?: string;
  errorMsg?: string;
  fieldName?: string;
  rejectedValue?: string;
}

export interface GlobalError {
  code?: string;
  errorMsg?: string;
  objectName?: string;
}

export interface HttpError {
  /** @format int32 */
  statusCode?: number;
  statusPhrase?: string;
}

export interface MultiHangarApiException {
  body?: ProblemDetail;
  cause?: {
    localizedMessage?: string;
    message?: string;
    stackTrace?: {
      classLoaderName?: string;
      className?: string;
      fileName?: string;
      /** @format int32 */
      lineNumber?: number;
      methodName?: string;
      moduleName?: string;
      moduleVersion?: string;
      nativeMethod?: boolean;
    }[];
  };
  detail?: string;
  detailMessageArguments?: Record<string, any>[];
  detailMessageCode?: string;
  exceptions?: HangarApiException[];
  headers?: {
    accept?: MediaType[];
    acceptCharset?: {
      registered?: boolean;
    }[];
    acceptLanguage?: {
      range?: string;
      /** @format double */
      weight?: number;
    }[];
    acceptLanguageAsLocales?: string[];
    acceptPatch?: MediaType[];
    accessControlAllowCredentials?: boolean;
    accessControlAllowHeaders?: string[];
    accessControlAllowMethods?: HttpMethod[];
    accessControlAllowOrigin?: string;
    accessControlExposeHeaders?: string[];
    /** @format int64 */
    accessControlMaxAge?: number;
    accessControlRequestHeaders?: string[];
    accessControlRequestMethod?: HttpMethod;
    all?: Record<string, string>;
    /** @uniqueItems true */
    allow?: HttpMethod[];
    basicAuth?: string;
    bearerAuth?: string;
    cacheControl?: string;
    connection?: string[];
    contentDisposition?: ContentDisposition;
    contentLanguage?: string;
    /** @format int64 */
    contentLength?: number;
    contentType?: MediaType;
    /** @format int64 */
    date?: number;
    empty?: boolean;
    etag?: string;
    /** @format int64 */
    expires?: number;
    host?: {
      address?: {
        /** @format byte */
        address?: string;
        anyLocalAddress?: boolean;
        canonicalHostName?: string;
        hostAddress?: string;
        hostName?: string;
        linkLocalAddress?: boolean;
        loopbackAddress?: boolean;
        mcglobal?: boolean;
        mclinkLocal?: boolean;
        mcnodeLocal?: boolean;
        mcorgLocal?: boolean;
        mcsiteLocal?: boolean;
        multicastAddress?: boolean;
        siteLocalAddress?: boolean;
      };
      hostName?: string;
      hostString?: string;
      /** @format int32 */
      port?: number;
      unresolved?: boolean;
    };
    ifMatch?: string[];
    /** @format int64 */
    ifModifiedSince?: number;
    ifNoneMatch?: string[];
    /** @format int64 */
    ifUnmodifiedSince?: number;
    /** @format int64 */
    lastModified?: number;
    /** @format uri */
    location?: string;
    origin?: string;
    pragma?: string;
    range?: HttpRange[];
    upgrade?: string;
    vary?: string[];
    [key: string]: any;
  };
  /** @format uri */
  instance?: string;
  localizedMessage?: string;
  message?: string;
  mostSpecificCause?: {
    localizedMessage?: string;
    message?: string;
    stackTrace?: {
      classLoaderName?: string;
      className?: string;
      fileName?: string;
      /** @format int32 */
      lineNumber?: number;
      methodName?: string;
      moduleName?: string;
      moduleVersion?: string;
      nativeMethod?: boolean;
    }[];
  };
  reason?: string;
  responseHeaders?: {
    accept?: MediaType[];
    acceptCharset?: {
      registered?: boolean;
    }[];
    acceptLanguage?: {
      range?: string;
      /** @format double */
      weight?: number;
    }[];
    acceptLanguageAsLocales?: string[];
    acceptPatch?: MediaType[];
    accessControlAllowCredentials?: boolean;
    accessControlAllowHeaders?: string[];
    accessControlAllowMethods?: HttpMethod[];
    accessControlAllowOrigin?: string;
    accessControlExposeHeaders?: string[];
    /** @format int64 */
    accessControlMaxAge?: number;
    accessControlRequestHeaders?: string[];
    accessControlRequestMethod?: HttpMethod;
    all?: Record<string, string>;
    /** @uniqueItems true */
    allow?: HttpMethod[];
    basicAuth?: string;
    bearerAuth?: string;
    cacheControl?: string;
    connection?: string[];
    contentDisposition?: ContentDisposition;
    contentLanguage?: string;
    /** @format int64 */
    contentLength?: number;
    contentType?: MediaType;
    /** @format int64 */
    date?: number;
    empty?: boolean;
    etag?: string;
    /** @format int64 */
    expires?: number;
    host?: {
      address?: {
        /** @format byte */
        address?: string;
        anyLocalAddress?: boolean;
        canonicalHostName?: string;
        hostAddress?: string;
        hostName?: string;
        linkLocalAddress?: boolean;
        loopbackAddress?: boolean;
        mcglobal?: boolean;
        mclinkLocal?: boolean;
        mcnodeLocal?: boolean;
        mcorgLocal?: boolean;
        mcsiteLocal?: boolean;
        multicastAddress?: boolean;
        siteLocalAddress?: boolean;
      };
      hostName?: string;
      hostString?: string;
      /** @format int32 */
      port?: number;
      unresolved?: boolean;
    };
    ifMatch?: string[];
    /** @format int64 */
    ifModifiedSince?: number;
    ifNoneMatch?: string[];
    /** @format int64 */
    ifUnmodifiedSince?: number;
    /** @format int64 */
    lastModified?: number;
    /** @format uri */
    location?: string;
    origin?: string;
    pragma?: string;
    range?: HttpRange[];
    upgrade?: string;
    vary?: string[];
    [key: string]: any;
  };
  rootCause?: {
    cause?: {
      localizedMessage?: string;
      message?: string;
      stackTrace?: {
        classLoaderName?: string;
        className?: string;
        fileName?: string;
        /** @format int32 */
        lineNumber?: number;
        methodName?: string;
        moduleName?: string;
        moduleVersion?: string;
        nativeMethod?: boolean;
      }[];
    };
    localizedMessage?: string;
    message?: string;
    stackTrace?: {
      classLoaderName?: string;
      className?: string;
      fileName?: string;
      /** @format int32 */
      lineNumber?: number;
      methodName?: string;
      moduleName?: string;
      moduleVersion?: string;
      nativeMethod?: boolean;
    }[];
    suppressed?: {
      localizedMessage?: string;
      message?: string;
      stackTrace?: {
        classLoaderName?: string;
        className?: string;
        fileName?: string;
        /** @format int32 */
        lineNumber?: number;
        methodName?: string;
        moduleName?: string;
        moduleVersion?: string;
        nativeMethod?: boolean;
      }[];
    }[];
  };
  stackTrace?: {
    classLoaderName?: string;
    className?: string;
    fileName?: string;
    /** @format int32 */
    lineNumber?: number;
    methodName?: string;
    moduleName?: string;
    moduleVersion?: string;
    nativeMethod?: boolean;
  }[];
  statusCode?: HttpStatusCode;
  suppressed?: {
    localizedMessage?: string;
    message?: string;
    stackTrace?: {
      classLoaderName?: string;
      className?: string;
      fileName?: string;
      /** @format int32 */
      lineNumber?: number;
      methodName?: string;
      moduleName?: string;
      moduleVersion?: string;
      nativeMethod?: boolean;
    }[];
  }[];
  title?: string;
  titleMessageCode?: string;
  /** @format uri */
  type?: string;
  typeMessageCode?: string;
}

export interface Announcement {
  color: string;
  text: string;
}

export interface ApiKey {
  /** @format date-time */
  createdAt: string;
  /** @format date-time */
  lastUsed?: string;
  name: string;
  permissions: NamedPermission[];
  tokenIdentifier: string;
}

export interface PaginatedResultUser {
  pagination: Pagination;
  result: User[];
}

export interface PaginatedResultProject {
  pagination: Pagination;
  result: Project[];
}

export interface PaginatedResultProjectCompact {
  pagination: Pagination;
  result: ProjectCompact[];
}

export interface PaginatedResultProjectMember {
  pagination: Pagination;
  result: ProjectMember[];
}

export interface PaginatedResultVersion {
  pagination: Pagination;
  result: Version[];
}

export interface PaginatedResultHangarLoggedAction {
  pagination: Pagination;
  result: HangarLoggedAction[];
}

export interface PaginatedResultHangarProjectFlag {
  pagination: Pagination;
  result: HangarProjectFlag[];
}

export interface PaginatedResultHangarNotification {
  pagination: Pagination;
  result: HangarNotification[];
}

export interface Pagination {
  /** @format int64 */
  count: number;
  /**
   * The maximum amount of items to return
   * @format int64
   * @min 1
   * @max 25
   * @example 1
   */
  limit: number;
  /**
   * Where to start searching
   * @format int64
   * @min 0
   * @example 0
   */
  offset: number;
}

export interface User {
  avatarUrl: string;
  /** @format date-time */
  createdAt: string;
  /** @format int64 */
  id: number;
  isOrganization: boolean;
  locked: boolean;
  name: string;
  nameHistory: UserNameChange[];
  /** @format int64 */
  projectCount: number;
  roles: number[];
  socials: JsonNode;
  tagline: string;
}

export interface UserNameChange {
  /** @format date-time */
  date: string;
  newName: string;
  oldName: string;
}

export interface ApiSession {
  /**
   * Milliseconds this JWT expires in
   * @format int64
   */
  expiresIn: number;
  /** JWT used for authentication */
  token: string;
}

export interface PermissionCheck {
  result: boolean;
  type: PermissionType;
}

export interface UserPermissions {
  permissionBinString: string;
  permissions: NamedPermission[];
  type: PermissionType;
}

export interface DayProjectStats {
  /** @format int64 */
  downloads: number;
  /** @format int64 */
  views: number;
}

/** The path and new contents of the page */
export interface PageEditForm {
  content: string;
  path: string;
}

export interface Project {
  /** The url to the project's icon */
  avatarUrl: string;
  /** The category of the project */
  category: Category;
  /** @format date-time */
  createdAt: string;
  /** The short description of the project */
  description: string;
  /**
   * The internal id of the project
   * @format int64
   */
  id: number;
  /**
   * The last time the project was updated
   * @format date-time
   */
  lastUpdated: string;
  /** The content of the main page */
  mainPageContent: string;
  /** The names of the members of the project */
  memberNames: string[];
  /** The unique name of the project */
  name: string;
  /** The namespace of the project */
  namespace: ProjectNamespace;
  /** The settings of the project */
  settings: ProjectSettings;
  /** Stats of the project */
  stats: ProjectStats;
  /** The platforms and versions the project supports */
  supportedPlatforms: Record<string, string[]>;
  /** Information about your interactions with the project */
  userActions: UserActions;
  /** The visibility of the project */
  visibility: Visibility;
}

export interface ProjectChannel {
  color: Color;
  /** @format date-time */
  createdAt: string;
  description: string;
  /** @uniqueItems true */
  flags: ChannelFlag[];
  name: string;
}

export interface ProjectCompact {
  /** The url to the project's icon */
  avatarUrl: string;
  /** The category of the project */
  category: Category;
  /** @format date-time */
  createdAt: string;
  /** The short description of the project */
  description: string;
  /**
   * The internal id of the project
   * @format int64
   */
  id: number;
  /**
   * The last time the project was updated
   * @format date-time
   */
  lastUpdated: string;
  /** The unique name of the project */
  name: string;
  /** The namespace of the project */
  namespace: ProjectNamespace;
  /** Stats of the project */
  stats: ProjectStats;
  /** The visibility of the project */
  visibility: Visibility;
}

export interface ProjectDonationSettings {
  enable: boolean;
  subject: string;
}

export interface ProjectLicense {
  name: string;
  type: string;
  url?: string;
}

export interface ProjectMember {
  roles: CompactRole[];
  user: string;
  /** @format int64 */
  userId: number;
}

export interface ProjectNamespace {
  owner: string;
  /**
   * The unique name of a project
   * @example "Maintenance"
   */
  slug: string;
}

export interface ProjectStats {
  /** @format int64 */
  downloads: number;
  /** @format int64 */
  recentDownloads: number;
  /** @format int64 */
  recentViews: number;
  /** @format int64 */
  stars: number;
  /** @format int64 */
  views: number;
  /** @format int64 */
  watchers: number;
}

export interface UserActions {
  flagged: boolean;
  starred: boolean;
  watching: boolean;
}

export interface Link {
  /** @format int64 */
  id: number;
  name: string;
  url: string;
}

export interface LinkSection {
  /** @format int64 */
  id: number;
  links: Link[];
  title: string;
  /**
   * Type of the link. Either SIDEBAR or TOP
   * @example "TOP"
   */
  type: string;
}

export interface ProjectSettings {
  /** @deprecated */
  donation: ProjectDonationSettings;
  keywords: string[];
  license: ProjectLicense;
  links: LinkSection[];
  sponsors: string;
  tags: Tag[];
}

export interface FileInfo {
  name: string;
  sha256Hash: string;
  /** @format int64 */
  sizeBytes: number;
}

export interface PlatformVersionDownload {
  /** Hangar download url if not an external download */
  downloadUrl?: string;
  /** External download url if not directly uploaded to Hangar */
  externalUrl?: string;
  fileInfo?: FileInfo;
}

export interface PluginDependency {
  /**
   * External url to download the dependency from if not a Hangar project, else null
   * @example "https://papermc.io/downloads"
   */
  externalUrl?: string;
  /**
   * Name of the plugin dependency. For non-external dependencies, this should be the Hangar project name
   * @example "Maintenance"
   */
  name: string;
  /** Platform the dependency runs on */
  platform: Platform;
  /**
   * Project ID of the dependency. Only for non-external dependencies
   * @format int64
   * @example 1
   */
  projectId: number;
  /** Whether the dependency is required for the plugin to function */
  required: boolean;
}

/** A version that has been uploaded */
export interface UploadedVersion {
  /**
   * URL of the uploaded version
   * @example "https://hangar.papermc.io/PaperMC/Debuggery/versions/1.0.0"
   */
  url: string;
}

export interface Version {
  author: string;
  channel: ProjectChannel;
  /** @format date-time */
  createdAt: string;
  description: string;
  downloads: Record<string, PlatformVersionDownload>;
  /** @format int64 */
  id: number;
  memberNames: string[];
  name: string;
  pinnedStatus: PinnedStatus;
  platformDependencies: Record<string, string[]>;
  platformDependenciesFormatted: Record<string, string[]>;
  pluginDependencies: Record<string, PluginDependency[]>;
  /** @format int64 */
  projectId: number;
  reviewState: ReviewState;
  stats: VersionStats;
  /** The visibility of a project or version */
  visibility: Visibility;
}

export interface VersionStats {
  platformDownloads: Record<string, number>;
  /** @format int64 */
  totalDownloads: number;
}

export interface FlagForm {
  /** @minLength 1 */
  comment: string;
  /** @format int64 */
  projectId: number;
  reason: FlagReason;
}

export interface RequestPagination {
  /**
   * The maximum amount of items to return
   * @format int64
   * @min 1
   * @max 25
   * @example 1
   */
  limit: number;
  /**
   * Where to start searching
   * @format int64
   * @min 0
   * @example 0
   */
  offset: number;
}

export interface PlatformVersion {
  subVersions: string[];
  version: string;
}

export interface UnreadCount {
  /** @format int64 */
  invites: number;
  /** @format int64 */
  notifications: number;
}

export interface CompactRole {
  category: string;
  color: Color;
  /** @format int32 */
  rank?: number;
  title: string;
}

export interface RoleData {
  assignable: boolean;
  color: string;
  permissions: string;
  /** @format int32 */
  rank?: number;
  roleCategory: string;
  /** @format int64 */
  roleId: number;
  title: string;
  value: string;
}

export interface UserTable {
  avatarUrl: string;
  /** @format date-time */
  createdAt: string;
  emailVerified: boolean;
  /** @format int64 */
  id: number;
  language: string;
  locked: boolean;
  name: string;
  organization: boolean;
  readPrompts: number[];
  socials: JsonNode;
  tagline?: string;
  theme: string;
  /** @format int64 */
  userId: number;
  /** @format uuid */
  uuid: string;
}

export interface ProjectOwner {
  /** @format int64 */
  id: number;
  name: string;
  organization: boolean;
  /** @format int64 */
  userId: number;
}

export interface ProjectPageTable {
  contents: string;
  /** @format date-time */
  createdAt: string;
  deletable: boolean;
  homepage: boolean;
  /** @format int64 */
  id: number;
  name: string;
  slug: string;
}

export interface OrganizationRoleTable {
  accepted: boolean;
  avatarUrl: string;
  /** @format date-time */
  createdAt: string;
  /** @format int64 */
  id: number;
  /** @format int64 */
  ownerId: number;
  ownerName: string;
  /** @format int64 */
  principalId: number;
  role: OrganizationRole;
  /** @format int64 */
  roleId: number;
  /** @format int64 */
  userId: number;
  /** @format uuid */
  uuid: string;
}

export interface ProjectRoleTable {
  accepted: boolean;
  /** @format date-time */
  createdAt: string;
  /** @format int64 */
  id: number;
  /** @format int64 */
  principalId: number;
  role: ProjectRole;
  /** @format int64 */
  roleId: number;
  /** @format int64 */
  userId: number;
}

export interface HangarOrganization {
  /** @format int64 */
  id: number;
  members: JoinableMemberOrganizationRoleTable[];
  owner: ProjectOwner;
  owner2: ProjectOwner;
  roleCategory: string;
}

export interface DayStats {
  /** @format date */
  day: string;
  /** @format int64 */
  flagsClosed: number;
  /** @format int64 */
  flagsOpened: number;
  /** @format int64 */
  reviews: number;
  /** @format int64 */
  totalDownloads: number;
  /** @format int64 */
  uploads: number;
}

export interface FlagActivity {
  namespace: ProjectNamespace;
  /** @format date-time */
  resolvedAt: string;
}

export interface ReviewActivity {
  /** @format date-time */
  endedAt: string;
  namespace: ProjectNamespace;
  platforms: Platform[];
  versionString: string;
}

export interface MissingFileCheck {
  fileNames: string[];
  namespace: ProjectNamespace;
  platforms: Platform[];
  versionString: string;
}

export interface UnhealthyProject {
  /** @format date-time */
  lastUpdated: string;
  namespace: ProjectNamespace;
  /** The visibility of a project or version */
  visibility: Visibility;
}

/** Data about the key to create */
export interface CreateAPIKeyForm {
  /**
   * @min 5
   * @max 36
   * @minLength 5
   * @maxLength 36
   */
  name: string;
  /** @uniqueItems true */
  permissions: NamedPermission[];
}

export interface CreateOrganizationForm {
  members: OrgMember[];
  name: string;
}

export interface OrgMember {
  /** @minLength 1 */
  name: string;
  role: OrganizationRole;
}

export interface ProjectMember {
  /** @minLength 1 */
  name: string;
  role: ProjectRole;
}

/** The path and new contents of the page */
export interface StringContent {
  /**
   * A non-null, non-empty string
   * @minLength 1
   */
  content: string;
}

export interface UserProfileSettings {
  socials: Record<string, string>;
  tagline: string;
}

export interface UserSettings {
  language: string;
  theme: string;
}

export interface ChangePlatformVersionsForm {
  empty: boolean;
  [key: string]: any;
}

export interface ChangeRoleForm {
  /** @minLength 1 */
  color: string;
  /** @format int32 */
  rank?: number;
  /** @format int64 */
  roleId: number;
  /** @minLength 1 */
  title: string;
}

export interface ReportNotificationForm {
  /**
   * @minLength 0
   * @maxLength 500
   */
  content: string;
  toReporter: boolean;
  warning: boolean;
}

export interface ChannelForm {
  color: Color;
  description: string;
  /** @uniqueItems true */
  flags: ChannelFlag[];
  /** @minLength 1 */
  name: string;
}

export interface EditChannelForm {
  color: Color;
  description: string;
  /** @uniqueItems true */
  flags: ChannelFlag[];
  /** @format int64 */
  id: number;
  /** @minLength 1 */
  name: string;
}

export interface NewProjectForm {
  avatarUrl?: string;
  category: Category;
  description: string;
  name: string;
  /** @format int64 */
  ownerId: number;
  pageContent: string;
  settings: ProjectSettings;
}

export interface NewProjectPage {
  /** @minLength 1 */
  name: string;
  /** @format int64 */
  parentId: number;
}

export interface ProjectSettingsForm {
  category: Category;
  description: string;
  settings: ProjectSettings;
}

export interface VisibilityChangeForm {
  /**
   * @minLength 0
   * @maxLength 500
   */
  comment: string;
  /** The visibility of a project or version */
  visibility: Visibility;
}

export interface ReviewMessage {
  args: Record<string, any>;
  message: string;
}

export interface UpdatePlatformVersions {
  /** Server platform */
  platform: Platform;
  /**
   * @maxItems 2147483647
   * @minItems 1
   * @uniqueItems true
   */
  versions: string[];
}

export interface UpdatePluginDependencies {
  /** Server platform */
  platform: Platform;
  pluginDependencies: Record<string, PluginDependency>;
}

export interface HealthReport {
  erroredJobs: JobTable[];
  missingFiles: MissingFileCheck[];
  nonPublicProjects: UnhealthyProject[];
  staleProjects: UnhealthyProject[];
}

export interface PossibleProjectOwner {
  /** @format int64 */
  id: number;
  name: string;
  organization: boolean;
  /** @format int64 */
  userId: number;
}

export interface Security {
  /** @uniqueItems true */
  oauthProviders: string[];
  safeDownloadHosts: string[];
}

export interface Validation {
  /** @format int32 */
  max?: number;
  /** @format int32 */
  min?: number;
  regex?: string;
}

export interface Validations {
  /** @format int32 */
  maxOrgCount: number;
  org: Validation;
  project: ProjectValidations;
  urlRegex: string;
  userTagline: Validation;
  version: Validation;
}

export interface ProjectValidations {
  channels: Validation;
  desc: Validation;
  keywordName: Validation;
  keywords: Validation;
  license: Validation;
  /** @format int32 */
  maxChannelCount: number;
  /** @format int32 */
  maxFileSize: number;
  /** @format int32 */
  maxPageCount: number;
  name: Validation;
  pageContent: Validation;
  pageName: Validation;
  sponsorsContent: Validation;
}

export interface HangarLoggedAction {
  action: Object;
  address?: {
    /** @format byte */
    address?: string;
    anyLocalAddress?: boolean;
    canonicalHostName?: string;
    hostAddress?: string;
    hostName?: string;
    linkLocalAddress?: boolean;
    loopbackAddress?: boolean;
    mcglobal?: boolean;
    mclinkLocal?: boolean;
    mcnodeLocal?: boolean;
    mcorgLocal?: boolean;
    mcsiteLocal?: boolean;
    multicastAddress?: boolean;
    siteLocalAddress?: boolean;
  };
  contextType: Context;
  /** @format date-time */
  createdAt: string;
  newState: string;
  oldState: string;
  page: LogPage;
  project: LogProject;
  subject: LogSubject;
  /** @format int64 */
  userId: number;
  userName: string;
  version: LogVersion;
}

export interface Object {
  description: string;
  name: string;
  pgLoggedAction: string;
}

export interface LogPage {
  /** @format int64 */
  id: number;
  name: string;
  slug: string;
}

export interface LogProject {
  /** @format int64 */
  id: number;
  owner: string;
  slug: string;
}

export interface LogSubject {
  /** @format int64 */
  id: number;
  name: string;
}

export interface LogVersion {
  /** @format int64 */
  id: number;
  platforms: Platform[];
  versionString: string;
}

export interface HangarChannel {
  color: Color;
  /** @format date-time */
  createdAt: string;
  description: string;
  /** @uniqueItems true */
  flags: ChannelFlag[];
  /** @format int64 */
  id: number;
  name: string;
  /** @format int64 */
  projectId: number;
  /** @format int32 */
  versionCount: number;
}

export interface HangarProject {
  /** The url to the project's icon */
  avatarUrl: string;
  /** The category of the project */
  category: Category;
  /** @format date-time */
  createdAt: string;
  /** The short description of the project */
  description: string;
  /**
   * The internal id of the project
   * @format int64
   */
  id: number;
  info: HangarProjectInfo;
  /**
   * The last time the project was updated
   * @format date-time
   */
  lastUpdated: string;
  lastVisibilityChangeComment: string;
  lastVisibilityChangeUserName: string;
  mainChannelVersions: Record<string, Version>;
  mainPage: ProjectPageTable;
  /** The content of the main page */
  mainPageContent: string;
  /** The names of the members of the project */
  memberNames: string[];
  members: JoinableMemberProjectRoleTable[];
  /** The unique name of the project */
  name: string;
  /** The namespace of the project */
  namespace: ProjectNamespace;
  owner2: ProjectOwner;
  pages: HangarProjectPage[];
  pinnedVersions: PinnedVersion[];
  /** @format int64 */
  projectId: number;
  roleCategory: string;
  /** The settings of the project */
  settings: ProjectSettings;
  /** Stats of the project */
  stats: ProjectStats;
  /** The platforms and versions the project supports */
  supportedPlatforms: Record<string, string[]>;
  /** Information about your interactions with the project */
  userActions: UserActions;
  /** The visibility of the project */
  visibility: Visibility;
}

export interface HangarProjectInfo {
  /** @format int32 */
  flagCount: number;
  /** @format int32 */
  noteCount: number;
  /** @format int32 */
  publicVersions: number;
  /** @format int64 */
  starCount: number;
  /** @format int64 */
  watcherCount: number;
}

export interface PinnedVersion {
  channel: ProjectChannel;
  downloads: Record<string, PlatformVersionDownload>;
  name: string;
  platformDependencies: Record<string, string[]>;
  platformDependenciesFormatted: Record<string, string[]>;
  type: Type;
  /** @format int64 */
  versionId: number;
}

export interface HangarProjectApproval {
  changeRequester: string;
  comment: string;
  namespace: ProjectNamespace;
  /** @format int64 */
  projectId: number;
  /** The visibility of a project or version */
  visibility: Visibility;
}

export interface HangarProjectFlag {
  comment: string;
  /** @format date-time */
  createdAt: string;
  /** @format int64 */
  id: number;
  /** @format int64 */
  projectId: number;
  projectNamespace: ProjectNamespace;
  /** The visibility of a project or version */
  projectVisibility: Visibility;
  reason: FlagReason;
  reportedByName: string;
  resolved: boolean;
  /** @format date-time */
  resolvedAt: string;
  /** @format int64 */
  resolvedBy: number;
  resolvedByName: string;
  /** @format int64 */
  userId: number;
}

export interface HangarProjectFlagNotification {
  /** @format int64 */
  id: number;
  message: string[];
  originUserName: string;
  type: NotificationType;
  /** @format int64 */
  userId: number;
}

export interface HangarProjectNote {
  /** @format date-time */
  createdAt: string;
  /** @format int64 */
  id: number;
  message: string;
  /** @format int64 */
  projectId: number;
  /** @format int64 */
  userId: number;
  userName: string;
}

export interface HangarProjectPage {
  children: HangarProjectPage[];
  home: boolean;
  /** @format int64 */
  id: number;
  name: string;
  slug: string;
}

export interface HangarUser {
  /** @format int32 */
  aal: number;
  accessToken: string;
  avatarUrl: string;
  /** @format date-time */
  createdAt: string;
  email: string;
  headerData: HeaderData;
  /** @format int64 */
  id: number;
  isOrganization: boolean;
  language: string;
  locked: boolean;
  name: string;
  nameHistory: UserNameChange[];
  /** @format int64 */
  projectCount: number;
  readPrompts: number[];
  roles: number[];
  socials: JsonNode;
  tagline: string;
  theme: string;
  /** @format uuid */
  uuid: string;
}

export interface HeaderData {
  globalPermission: string;
  /** @format int64 */
  organizationCount: number;
  /** @format int64 */
  projectApprovals: number;
  /** @format int64 */
  reviewQueueCount: number;
  unreadCount: UnreadCount;
  /** @format int64 */
  unresolvedFlags: number;
}

export interface JoinableMemberOrganizationRoleTable {
  hidden: boolean;
  role: OrganizationRoleTable;
  user: UserTable;
}

export interface JoinableMemberProjectRoleTable {
  hidden: boolean;
  role: ProjectRoleTable;
  user: UserTable;
}

export interface HangarOrganizationInvite {
  name: string;
  role: string;
  /** @format int64 */
  roleId: number;
  type: InviteType;
  url: string;
}

export interface HangarProjectInvite {
  name: string;
  representingOrg?: string;
  role: string;
  /** @format int64 */
  roleId: number;
  type: InviteType;
  url: string;
}

export interface HangarNotification {
  action: string;
  /** @format date-time */
  createdAt: string;
  /** @format int64 */
  id: number;
  message: string[];
  originUserName: string;
  read: boolean;
  type: NotificationType;
}

export interface HangarReview {
  /** @format date-time */
  createdAt: string;
  /** @format date-time */
  endedAt?: string;
  messages: HangarReviewMessage[];
  /** @format int64 */
  userId: number;
  userName: string;
}

export interface HangarReviewMessage {
  action: ReviewAction;
  args: Record<string, any>;
  /** @format date-time */
  createdAt: string;
  message: string;
}

export interface HangarReviewQueueEntry {
  channelColor: Color;
  channelName: string;
  namespace: ProjectNamespace;
  platforms: Platform[];
  reviews: Review[];
  versionAuthor: string;
  /** @format date-time */
  versionCreatedAt: string;
  /** @format int64 */
  versionId: number;
  versionString: string;
}

export interface Review {
  lastAction: ReviewAction;
  /** @format date-time */
  reviewEnded: string;
  /** @format date-time */
  reviewStarted: string;
  reviewerName: string;
}

export interface JarScanResult {
  /** @format date-time */
  createdAt: string;
  entries: string[];
  highestSeverity: string;
  /** @format int64 */
  id: number;
  /** Server platform */
  platform: Platform;
}

/** List of different jars/external links that are part of the version */
export interface MultipartFileOrUrl {
  /**
   * External url to download the jar from if not provided via an attached jar, else null
   * @example "https://papermc.io/downloads"
   */
  externalUrl?: string;
  /**
   * List of platforms this jar runs on
   * @example "[PAPER, WATERFALL, VELOCITY]"
   */
  platforms: Platform[];
}

export interface PendingVersion {
  channelColor?: Color;
  channelDescription: string;
  /** @uniqueItems true */
  channelFlags: ChannelFlag[];
  /** @minLength 1 */
  channelName: string;
  /** @minLength 1 */
  description: string;
  /**
   * @maxItems 3
   * @minItems 1
   */
  files: PendingVersionFile[];
  platformDependencies: Record<string, string[]>;
  pluginDependencies: Record<string, PluginDependency[]>;
  /** @minLength 1 */
  versionString: string;
}

export interface PendingVersionFile {
  externalUrl?: string;
  fileInfo?: FileInfo;
  /**
   * @maxItems 3
   * @minItems 1
   */
  platforms: Platform[];
}

/** Version data. See the VersionUpload schema for more info */
export interface VersionUpload {
  /**
   * Channel of the version to be published under
   * @minLength 1
   * @example "Release"
   */
  channel: string;
  description: string;
  /**
   * @maxItems 3
   * @minItems 1
   */
  files: MultipartFileOrUrl[];
  /**
   * Map of platforms and their versions this version runs on
   * @example "{PAPER: ["1.12", "1.16-1.18.2", "1.20.x"]}"
   */
  platformDependencies: Record<string, string[]>;
  /** Map of each platform's plugin dependencies */
  pluginDependencies: Record<string, PluginDependency[]>;
  /**
   * Version string of the version to be published
   * @minLength 1
   * @example "1.0.0-SNAPSHOT+1"
   */
  version: string;
}

export interface ContentDisposition {
  attachment?: boolean;
  charset?: {
    registered?: boolean;
  };
  /** @format date-time */
  creationDate?: string;
  filename?: string;
  formData?: boolean;
  inline?: boolean;
  /** @format date-time */
  modificationDate?: string;
  name?: string;
  /** @format date-time */
  readDate?: string;
  /** @format int64 */
  size?: number;
  type?: string;
}

export interface HttpHeaders {
  accept?: MediaType[];
  acceptCharset?: {
    registered?: boolean;
  }[];
  acceptLanguage?: {
    range?: string;
    /** @format double */
    weight?: number;
  }[];
  acceptLanguageAsLocales?: string[];
  acceptPatch?: MediaType[];
  accessControlAllowCredentials?: boolean;
  accessControlAllowHeaders?: string[];
  accessControlAllowMethods?: HttpMethod[];
  accessControlAllowOrigin?: string;
  accessControlExposeHeaders?: string[];
  /** @format int64 */
  accessControlMaxAge?: number;
  accessControlRequestHeaders?: string[];
  accessControlRequestMethod?: HttpMethod;
  all?: Record<string, string>;
  /** @uniqueItems true */
  allow?: HttpMethod[];
  basicAuth?: string;
  bearerAuth?: string;
  cacheControl?: string;
  connection?: string[];
  contentDisposition?: ContentDisposition;
  contentLanguage?: string;
  /** @format int64 */
  contentLength?: number;
  contentType?: MediaType;
  /** @format int64 */
  date?: number;
  empty?: boolean;
  etag?: string;
  /** @format int64 */
  expires?: number;
  host?: {
    address?: {
      /** @format byte */
      address?: string;
      anyLocalAddress?: boolean;
      canonicalHostName?: string;
      hostAddress?: string;
      hostName?: string;
      linkLocalAddress?: boolean;
      loopbackAddress?: boolean;
      mcglobal?: boolean;
      mclinkLocal?: boolean;
      mcnodeLocal?: boolean;
      mcorgLocal?: boolean;
      mcsiteLocal?: boolean;
      multicastAddress?: boolean;
      siteLocalAddress?: boolean;
    };
    hostName?: string;
    hostString?: string;
    /** @format int32 */
    port?: number;
    unresolved?: boolean;
  };
  ifMatch?: string[];
  /** @format int64 */
  ifModifiedSince?: number;
  ifNoneMatch?: string[];
  /** @format int64 */
  ifUnmodifiedSince?: number;
  /** @format int64 */
  lastModified?: number;
  /** @format uri */
  location?: string;
  origin?: string;
  pragma?: string;
  range?: HttpRange[];
  upgrade?: string;
  vary?: string[];
  [key: string]: any;
}

export type HttpMethod = any;

export type HttpRange = any;

export interface HttpStatusCode {
  error?: boolean;
  is1xxInformational?: boolean;
  is2xxSuccessful?: boolean;
  is3xxRedirection?: boolean;
  is4xxClientError?: boolean;
  is5xxServerError?: boolean;
}

export interface MediaType {
  charset?: {
    registered?: boolean;
  };
  concrete?: boolean;
  parameters?: Record<string, string>;
  /** @format double */
  qualityValue?: number;
  subtype?: string;
  subtypeSuffix?: string;
  type?: string;
  wildcardSubtype?: boolean;
  wildcardType?: boolean;
}

export interface ProblemDetail {
  detail?: string;
  /** @format uri */
  instance?: string;
  properties?: Record<string, Record<string, any>>;
  /** @format int32 */
  status?: number;
  title?: string;
  /** @format uri */
  type?: string;
}

export type StreamingResponseBody = any;
