// noinspection JSValidateJSDoc,JSUnusedGlobalSymbols

export type JsonNode = Record<string, any>;

export interface RenameRequest {
  displayName: string;
  id: string;
}

export interface BackupCode {
  code: string;
  /** @format date-time */
  used_at: string;
}

export enum CredentialType {
  PASSWORD = "PASSWORD",
  BACKUP_CODES = "BACKUP_CODES",
  TOTP = "TOTP",
  WEBAUTHN = "WEBAUTHN",
  OAUTH = "OAUTH",
}

export interface OAuthConnection {
  id: string;
  name: string;
  provider: string;
}

export interface AccountForm {
  currentPassword: string;
  email: string;
  newPassword: string;
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

export enum OAuthMode {
  LOGIN = "LOGIN",
  SIGNUP = "SIGNUP",
  SETTINGS = "SETTINGS",
}

export interface Webhook {
  canceledBy: string;
  details: Details;
  duration: {
    /** @format int32 */
    nano?: number;
    negative?: boolean;
    positive?: boolean;
    /** @format int64 */
    seconds?: number;
    units?: {
      dateBased?: boolean;
      durationEstimated?: boolean;
      timeBased?: boolean;
    }[];
    zero?: boolean;
  };
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

export enum JobType {
  SEND_EMAIL = "SEND_EMAIL",
  SEND_WEBHOOK = "SEND_WEBHOOK",
  SCHEDULED_TASK = "SCHEDULED_TASK",
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

export enum InviteStatus {
  ACCEPT = "ACCEPT",
  DECLINE = "DECLINE",
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
    acceptLanguageAsLocales?: {
      country?: string;
      displayCountry?: string;
      displayLanguage?: string;
      displayName?: string;
      displayScript?: string;
      displayVariant?: string;
      /** @uniqueItems true */
      extensionKeys?: string[];
      iso3Country?: string;
      iso3Language?: string;
      language?: string;
      script?: string;
      /** @uniqueItems true */
      unicodeLocaleAttributes?: string[];
      /** @uniqueItems true */
      unicodeLocaleKeys?: string[];
      variant?: string;
    }[];
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
    contentLanguage?: {
      country?: string;
      displayCountry?: string;
      displayLanguage?: string;
      displayName?: string;
      displayScript?: string;
      displayVariant?: string;
      /** @uniqueItems true */
      extensionKeys?: string[];
      iso3Country?: string;
      iso3Language?: string;
      language?: string;
      script?: string;
      /** @uniqueItems true */
      unicodeLocaleAttributes?: string[];
      /** @uniqueItems true */
      unicodeLocaleKeys?: string[];
      variant?: string;
    };
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
    acceptLanguageAsLocales?: {
      country?: string;
      displayCountry?: string;
      displayLanguage?: string;
      displayName?: string;
      displayScript?: string;
      displayVariant?: string;
      /** @uniqueItems true */
      extensionKeys?: string[];
      iso3Country?: string;
      iso3Language?: string;
      language?: string;
      script?: string;
      /** @uniqueItems true */
      unicodeLocaleAttributes?: string[];
      /** @uniqueItems true */
      unicodeLocaleKeys?: string[];
      variant?: string;
    }[];
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
    contentLanguage?: {
      country?: string;
      displayCountry?: string;
      displayLanguage?: string;
      displayName?: string;
      displayScript?: string;
      displayVariant?: string;
      /** @uniqueItems true */
      extensionKeys?: string[];
      iso3Country?: string;
      iso3Language?: string;
      language?: string;
      script?: string;
      /** @uniqueItems true */
      unicodeLocaleAttributes?: string[];
      /** @uniqueItems true */
      unicodeLocaleKeys?: string[];
      variant?: string;
    };
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
    acceptLanguageAsLocales?: {
      country?: string;
      displayCountry?: string;
      displayLanguage?: string;
      displayName?: string;
      displayScript?: string;
      displayVariant?: string;
      /** @uniqueItems true */
      extensionKeys?: string[];
      iso3Country?: string;
      iso3Language?: string;
      language?: string;
      script?: string;
      /** @uniqueItems true */
      unicodeLocaleAttributes?: string[];
      /** @uniqueItems true */
      unicodeLocaleKeys?: string[];
      variant?: string;
    }[];
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
    contentLanguage?: {
      country?: string;
      displayCountry?: string;
      displayLanguage?: string;
      displayName?: string;
      displayScript?: string;
      displayVariant?: string;
      /** @uniqueItems true */
      extensionKeys?: string[];
      iso3Country?: string;
      iso3Language?: string;
      language?: string;
      script?: string;
      /** @uniqueItems true */
      unicodeLocaleAttributes?: string[];
      /** @uniqueItems true */
      unicodeLocaleKeys?: string[];
      variant?: string;
    };
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
    acceptLanguageAsLocales?: {
      country?: string;
      displayCountry?: string;
      displayLanguage?: string;
      displayName?: string;
      displayScript?: string;
      displayVariant?: string;
      /** @uniqueItems true */
      extensionKeys?: string[];
      iso3Country?: string;
      iso3Language?: string;
      language?: string;
      script?: string;
      /** @uniqueItems true */
      unicodeLocaleAttributes?: string[];
      /** @uniqueItems true */
      unicodeLocaleKeys?: string[];
      variant?: string;
    }[];
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
    contentLanguage?: {
      country?: string;
      displayCountry?: string;
      displayLanguage?: string;
      displayName?: string;
      displayScript?: string;
      displayVariant?: string;
      /** @uniqueItems true */
      extensionKeys?: string[];
      iso3Country?: string;
      iso3Language?: string;
      language?: string;
      script?: string;
      /** @uniqueItems true */
      unicodeLocaleAttributes?: string[];
      /** @uniqueItems true */
      unicodeLocaleKeys?: string[];
      variant?: string;
    };
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

export enum Tag {
  ADDON = "ADDON",
  LIBRARY = "LIBRARY",
  SUPPORTS_FOLIA = "SUPPORTS_FOLIA",
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

export enum PinnedStatus {
  NONE = "NONE",
  VERSION = "VERSION",
  CHANNEL = "CHANNEL",
}

export interface VersionStats {
  platformDownloads: Record<string, number>;
  /** @format int64 */
  totalDownloads: number;
}

export interface FlagForm {
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

export enum ChannelFlag {
  FROZEN = "FROZEN",
  UNSTABLE = "UNSTABLE",
  PINNED = "PINNED",
  SENDS_NOTIFICATIONS = "SENDS_NOTIFICATIONS",
  HIDE_BY_DEFAULT = "HIDE_BY_DEFAULT",
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

export enum PermissionType {
  Global = "global",
  Project = "project",
  Organization = "organization",
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

export enum Category {
  Server = "Server",
  Proxy = "Proxy",
}

export interface PlatformVersion {
  subVersions: string[];
  version: string;
}

export enum Prompt {
  CHANGE_AVATAR = "CHANGE_AVATAR",
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

export enum FlagReason {
  inappropriateContent = "project.flag.flags.inappropriateContent",
  impersonation = "project.flag.flags.impersonation",
  spam = "project.flag.flags.spam",
  malIntent = "project.flag.flags.malIntent",
  other = "project.flag.flags.other",
}

export enum ReviewState {
  Unreviewed = "unreviewed",
  Reviewed = "reviewed",
  UnderReview = "under_review",
  PartiallyReviewed = "partially_reviewed",
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

export interface CompactRole {
  category: string;
  color: Color;
  /** @format int32 */
  rank?: number;
  title: string;
}

export enum OrganizationRole {
  ORGANIZATION_SUPPORT = "ORGANIZATION_SUPPORT",
  ORGANIZATION_EDITOR = "ORGANIZATION_EDITOR",
  ORGANIZATION_DEVELOPER = "ORGANIZATION_DEVELOPER",
  ORGANIZATION_MAINTAINER = "ORGANIZATION_MAINTAINER",
  ORGANIZATION_ADMIN = "ORGANIZATION_ADMIN",
  ORGANIZATION_OWNER = "ORGANIZATION_OWNER",
}

export enum ProjectRole {
  PROJECT_SUPPORT = "PROJECT_SUPPORT",
  PROJECT_EDITOR = "PROJECT_EDITOR",
  PROJECT_DEVELOPER = "PROJECT_DEVELOPER",
  PROJECT_MAINTAINER = "PROJECT_MAINTAINER",
  PROJECT_ADMIN = "PROJECT_ADMIN",
  PROJECT_OWNER = "PROJECT_OWNER",
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
  name: string;
  role: OrganizationRole;
}

export interface ProjectMember {
  name: string;
  role: ProjectRole;
}

/** The path and new contents of the page */
export interface StringContent {
  /** A non-null, non-empty string */
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
  color: string;
  /** @format int32 */
  rank?: number;
  /** @format int64 */
  roleId: number;
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
  name: string;
}

export interface EditChannelForm {
  color: Color;
  description: string;
  /** @uniqueItems true */
  flags: ChannelFlag[];
  /** @format int64 */
  id: number;
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

export enum Context {
  PROJECT = "PROJECT",
  VERSION = "VERSION",
  PAGE = "PAGE",
  USER = "USER",
  ORGANIZATION = "ORGANIZATION",
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

export enum Type {
  CHANNEL = "CHANNEL",
  VERSION = "VERSION",
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
  /** @format int64 */
  unansweredInvites: number;
  /** @format int64 */
  unreadNotifications: number;
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

export enum InviteType {
  Project = "project",
  Organization = "organization",
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

export enum NotificationType {
  Neutral = "neutral",
  Success = "success",
  Info = "info",
  Warning = "warning",
  Error = "error",
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
  reviewEnded?: string;
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
  channelName: string;
  description: string;
  /**
   * @maxItems 3
   * @minItems 1
   */
  files: PendingVersionFile[];
  platformDependencies: Record<string, string[]>;
  pluginDependencies: Record<string, PluginDependency[]>;
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
  acceptLanguageAsLocales?: {
    country?: string;
    displayCountry?: string;
    displayLanguage?: string;
    displayName?: string;
    displayScript?: string;
    displayVariant?: string;
    /** @uniqueItems true */
    extensionKeys?: string[];
    iso3Country?: string;
    iso3Language?: string;
    language?: string;
    script?: string;
    /** @uniqueItems true */
    unicodeLocaleAttributes?: string[];
    /** @uniqueItems true */
    unicodeLocaleKeys?: string[];
    variant?: string;
  }[];
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
  contentLanguage?: {
    country?: string;
    displayCountry?: string;
    displayLanguage?: string;
    displayName?: string;
    displayScript?: string;
    displayVariant?: string;
    /** @uniqueItems true */
    extensionKeys?: string[];
    iso3Country?: string;
    iso3Language?: string;
    language?: string;
    script?: string;
    /** @uniqueItems true */
    unicodeLocaleAttributes?: string[];
    /** @uniqueItems true */
    unicodeLocaleKeys?: string[];
    variant?: string;
  };
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

export type StreamingResponseBody = Record<string, any>;

export type QueryParamsType = Record<string | number, any>;
export type ResponseFormat = keyof Omit<Body, "body" | "bodyUsed">;

export interface FullRequestParams extends Omit<RequestInit, "body"> {
  /** set parameter to `true` for call `securityWorker` for this request */
  secure?: boolean;
  /** request path */
  path: string;
  /** content type of request body */
  type?: ContentType;
  /** query params */
  query?: QueryParamsType;
  /** format of response (i.e. response.json() -> format: "json") */
  format?: ResponseFormat;
  /** request body */
  body?: unknown;
  /** base url */
  baseUrl?: string;
  /** request cancellation token */
  cancelToken?: CancelToken;
}

export type RequestParams = Omit<FullRequestParams, "body" | "method" | "query" | "path">;

export interface ApiConfig<SecurityDataType = unknown> {
  baseUrl?: string;
  baseApiParams?: Omit<RequestParams, "baseUrl" | "cancelToken" | "signal">;
  securityWorker?: (securityData: SecurityDataType | null) => Promise<RequestParams | void> | RequestParams | void;
  customFetch?: typeof fetch;
}

export interface HttpResponse<D extends unknown, E extends unknown = unknown> extends Response {
  data: D;
  error: E;
}

type CancelToken = Symbol | string | number;

export enum ContentType {
  Json = "application/json",
  FormData = "multipart/form-data",
  UrlEncoded = "application/x-www-form-urlencoded",
  Text = "text/plain",
}

export class HttpClient<SecurityDataType = unknown> {
  public baseUrl: string = "http://localhost:8080";
  private securityData: SecurityDataType | null = null;
  private securityWorker?: ApiConfig<SecurityDataType>["securityWorker"];
  private abortControllers = new Map<CancelToken, AbortController>();
  private customFetch = (...fetchParams: Parameters<typeof fetch>) => fetch(...fetchParams);

  private baseApiParams: RequestParams = {
    credentials: "same-origin",
    headers: {},
    redirect: "follow",
    referrerPolicy: "no-referrer",
  };

  constructor(apiConfig: ApiConfig<SecurityDataType> = {}) {
    Object.assign(this, apiConfig);
  }

  public setSecurityData = (data: SecurityDataType | null) => {
    this.securityData = data;
  };

  protected encodeQueryParam(key: string, value: any) {
    const encodedKey = encodeURIComponent(key);
    return `${encodedKey}=${encodeURIComponent(typeof value === "number" ? value : `${value}`)}`;
  }

  protected addQueryParam(query: QueryParamsType, key: string) {
    return this.encodeQueryParam(key, query[key]);
  }

  protected addArrayQueryParam(query: QueryParamsType, key: string) {
    const value = query[key];
    return value.map((v: any) => this.encodeQueryParam(key, v)).join("&");
  }

  protected toQueryString(rawQuery?: QueryParamsType): string {
    const query = rawQuery || {};
    const keys = Object.keys(query).filter((key) => "undefined" !== typeof query[key]);
    return keys.map((key) => (Array.isArray(query[key]) ? this.addArrayQueryParam(query, key) : this.addQueryParam(query, key))).join("&");
  }

  protected addQueryParams(rawQuery?: QueryParamsType): string {
    const queryString = this.toQueryString(rawQuery);
    return queryString ? `?${queryString}` : "";
  }

  private contentFormatters: Record<ContentType, (input: any) => any> = {
    [ContentType.Json]: (input: any) => (input !== null && (typeof input === "object" || typeof input === "string") ? JSON.stringify(input) : input),
    [ContentType.Text]: (input: any) => (input !== null && typeof input !== "string" ? JSON.stringify(input) : input),
    [ContentType.FormData]: (input: any) =>
      Object.keys(input || {}).reduce((formData, key) => {
        const property = input[key];
        formData.append(
          key,
          property instanceof Blob ? property : typeof property === "object" && property !== null ? JSON.stringify(property) : `${property}`
        );
        return formData;
      }, new FormData()),
    [ContentType.UrlEncoded]: (input: any) => this.toQueryString(input),
  };

  protected mergeRequestParams(params1: RequestParams, params2?: RequestParams): RequestParams {
    return {
      ...this.baseApiParams,
      ...params1,
      ...(params2 || {}),
      headers: {
        ...(this.baseApiParams.headers || {}),
        ...(params1.headers || {}),
        ...((params2 && params2.headers) || {}),
      },
    };
  }

  protected createAbortSignal = (cancelToken: CancelToken): AbortSignal | undefined => {
    if (this.abortControllers.has(cancelToken)) {
      const abortController = this.abortControllers.get(cancelToken);
      if (abortController) {
        return abortController.signal;
      }
      return void 0;
    }

    const abortController = new AbortController();
    this.abortControllers.set(cancelToken, abortController);
    return abortController.signal;
  };

  public abortRequest = (cancelToken: CancelToken) => {
    const abortController = this.abortControllers.get(cancelToken);

    if (abortController) {
      abortController.abort();
      this.abortControllers.delete(cancelToken);
    }
  };

  public request = async <T = any, E = any>({
    body,
    secure,
    path,
    type,
    query,
    format,
    baseUrl,
    cancelToken,
    ...params
  }: FullRequestParams): Promise<HttpResponse<T, E>> => {
    const secureParams =
      ((typeof secure === "boolean" ? secure : this.baseApiParams.secure) && this.securityWorker && (await this.securityWorker(this.securityData))) || {};
    const requestParams = this.mergeRequestParams(params, secureParams);
    const queryString = query && this.toQueryString(query);
    const payloadFormatter = this.contentFormatters[type || ContentType.Json];
    const responseFormat = format || requestParams.format;

    return this.customFetch(`${baseUrl || this.baseUrl || ""}${path}${queryString ? `?${queryString}` : ""}`, {
      ...requestParams,
      headers: {
        ...(requestParams.headers || {}),
        ...(type && type !== ContentType.FormData ? { "Content-Type": type } : {}),
      },
      signal: (cancelToken ? this.createAbortSignal(cancelToken) : requestParams.signal) || null,
      body: typeof body === "undefined" || body === null ? null : payloadFormatter(body),
    }).then(async (response) => {
      const r = response.clone() as HttpResponse<T, E>;
      r.data = null as unknown as T;
      r.error = null as unknown as E;

      const data = !responseFormat
        ? r
        : await response[responseFormat]()
            .then((data) => {
              if (r.ok) {
                r.data = data;
              } else {
                r.error = data;
              }
              return r;
            })
            .catch((e) => {
              r.error = e;
              return r;
            });

      if (cancelToken) {
        this.abortControllers.delete(cancelToken);
      }

      if (!response.ok) throw data;
      return data;
    });
  };
}

/**
 * @title OpenAPI definition
 * @version v0
 * @baseUrl http://localhost:8080
 */
export class Api<SecurityDataType extends unknown> extends HttpClient<SecurityDataType> {
  api = {
    /**
     * No description
     *
     * @tags application-controller
     * @name ApiRootRedirect
     * @request GET:/api
     */
    apiRootRedirect: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-activity-controller
     * @name GetFlagActivity
     * @request GET:/api/internal/admin/activity/{user}/flags
     */
    getFlagActivity: (user: string, params: RequestParams = {}) =>
      this.request<FlagActivity[], any>({
        path: `/api/internal/admin/activity/${user}/flags`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-activity-controller
     * @name GetReviewActivity
     * @request GET:/api/internal/admin/activity/{user}/reviews
     */
    getReviewActivity: (user: string, params: RequestParams = {}) =>
      this.request<ReviewActivity[], any>({
        path: `/api/internal/admin/activity/${user}/reviews`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-approval-controller
     * @name GetProjectApprovalQueueSize
     * @request GET:/api/internal/admin/approval/projectneedingapproval
     */
    getProjectApprovalQueueSize: (params: RequestParams = {}) =>
      this.request<number, any>({
        path: `/api/internal/admin/approval/projectneedingapproval`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-approval-controller
     * @name GetProjectApprovals
     * @request GET:/api/internal/admin/approval/projects
     */
    getProjectApprovals: (params: RequestParams = {}) =>
      this.request<ProjectApprovals, any>({
        path: `/api/internal/admin/approval/projects`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-approval-controller
     * @name GetReviewQueue
     * @request GET:/api/internal/admin/approval/versions
     */
    getReviewQueue: (params: RequestParams = {}) =>
      this.request<ReviewQueue, any>({
        path: `/api/internal/admin/approval/versions`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-approval-controller
     * @name GetVersionApprovalQueueSize
     * @request GET:/api/internal/admin/approval/versionsneedingapproval
     */
    getVersionApprovalQueueSize: (params: RequestParams = {}) =>
      this.request<number, any>({
        path: `/api/internal/admin/approval/versionsneedingapproval`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name FixAvatarUrls
     * @request POST:/api/internal/admin/fixAvatars
     */
    fixAvatarUrls: (
      query?: {
        force?: boolean;
      },
      params: RequestParams = {}
    ) =>
      this.request<string, any>({
        path: `/api/internal/admin/fixAvatars`,
        method: "POST",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name GetHealthReport
     * @request GET:/api/internal/admin/health
     */
    getHealthReport: (params: RequestParams = {}) =>
      this.request<HealthReport, any>({
        path: `/api/internal/admin/health`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name SetUserLock
     * @request POST:/api/internal/admin/lock-user/{user}
     */
    setUserLock: (
      user: string,
      query: {
        locked: boolean;
        /** @default false */
        toggleProjectDeletion?: boolean;
      },
      data: StringContent,
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/admin/lock-user/${user}`,
        method: "POST",
        query: query,
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name GetActionLog
     * @request GET:/api/internal/admin/log
     */
    getActionLog: (
      query: {
        pagination: RequestPagination;
        /** Filters by log action */
        logAction?: string;
        /** Filters based on a project page */
        pageId?: string;
        /** Filters logs by a project namespace */
        projectSlug?: string;
        /** Filters logs by a project namespace */
        authorName?: string;
        /** Filters by subject name, usually a user action where the subject name is the user the action is about, not the user that performed the action */
        subjectName?: string;
        /** The user whose action created the log entry */
        user?: string;
        /** Filters logs based on a version string and platform */
        platform?: string;
        /** Filters logs based on a version string and platform */
        versionString?: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultHangarLoggedAction, any>({
        path: `/api/internal/admin/log`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name ChangePlatformVersions
     * @request POST:/api/internal/admin/platformVersions
     */
    changePlatformVersions: (data: ChangePlatformVersionsForm, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/admin/platformVersions`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name ChangeRoles
     * @request POST:/api/internal/admin/roles
     */
    changeRoles: (data: ChangeRoleForm[], params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/admin/roles`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name ApproveVersionsWithSafeLinks
     * @request POST:/api/internal/admin/scanSafeLinks
     */
    approveVersionsWithSafeLinks: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/admin/scanSafeLinks`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name GetStats
     * @request GET:/api/internal/admin/stats
     */
    getStats: (
      query?: {
        /** @format date */
        from?: string;
        /** @format date */
        to?: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<DayStats[], any>({
        path: `/api/internal/admin/stats`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name UpdateHashes
     * @request POST:/api/internal/admin/updateHashes
     */
    updateHashes: (params: RequestParams = {}) =>
      this.request<string[], any>({
        path: `/api/internal/admin/updateHashes`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name RemoveRole
     * @request DELETE:/api/internal/admin/user/{user}/{role}
     */
    removeRole: (user: string, role: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/admin/user/${user}/${role}`,
        method: "DELETE",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name AddRole
     * @request POST:/api/internal/admin/user/{user}/{role}
     */
    addRole: (user: string, role: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/admin/user/${user}/${role}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags admin-controller
     * @name YeetusDeletus
     * @request POST:/api/internal/admin/yeet/{user}
     */
    yeetusDeletus: (user: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/admin/yeet/${user}`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags api-key-controller
     * @name CheckKeyName
     * @request GET:/api/internal/api-keys/check-key/{user}
     */
    checkKeyName: (
      user: string,
      query: {
        name: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/api-keys/check-key/${user}`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags api-key-controller
     * @name CreateApiKey
     * @request POST:/api/internal/api-keys/create-key/{user}
     */
    createApiKey: (user: string, data: CreateAPIKeyForm, params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/internal/api-keys/create-key/${user}`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags api-key-controller
     * @name DeleteApiKey
     * @request POST:/api/internal/api-keys/delete-key/{user}
     */
    deleteApiKey: (user: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/api-keys/delete-key/${user}`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags api-key-controller
     * @name GetApiKeys
     * @request GET:/api/internal/api-keys/existing-keys/{user}
     */
    getApiKeys: (user: string, params: RequestParams = {}) =>
      this.request<ApiKey[], any>({
        path: `/api/internal/api-keys/existing-keys/${user}`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags api-key-controller
     * @name GetPossiblePermissions
     * @request GET:/api/internal/api-keys/possible-perms/{user}
     */
    getPossiblePermissions: (user: string, params: RequestParams = {}) =>
      this.request<NamedPermission[], any>({
        path: `/api/internal/api-keys/possible-perms/${user}`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags auth-controller
     * @name SaveAccount
     * @request POST:/api/internal/auth/account
     */
    saveAccount: (data: AccountForm, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/auth/account`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name RegenerateBackupCodes
     * @request POST:/api/internal/auth/codes/regenerate
     */
    regenerateBackupCodes: (params: RequestParams = {}) =>
      this.request<BackupCode[], any>({
        path: `/api/internal/auth/codes/regenerate`,
        method: "POST",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name ShowBackupCodes
     * @request POST:/api/internal/auth/codes/show
     */
    showBackupCodes: (params: RequestParams = {}) =>
      this.request<BackupCode[], any>({
        path: `/api/internal/auth/codes/show`,
        method: "POST",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name SendEmailCode
     * @request POST:/api/internal/auth/email/send
     */
    sendEmailCode: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/auth/email/send`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name VerifyEmail
     * @request POST:/api/internal/auth/email/verify
     */
    verifyEmail: (data: string, params: RequestParams = {}) =>
      this.request<JsonNode, any>({
        path: `/api/internal/auth/email/verify`,
        method: "POST",
        body: data,
        type: ContentType.Text,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags auth-controller
     * @name InvalidateRefreshToken
     * @request GET:/api/internal/auth/invalidate
     */
    invalidateRefreshToken: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/auth/invalidate`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags login-controller
     * @name LoginBackup
     * @request POST:/api/internal/auth/login/backup
     */
    loginBackup: (data: LoginBackupForm, params: RequestParams = {}) =>
      this.request<LoginResponse, any>({
        path: `/api/internal/auth/login/backup`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags login-controller
     * @name LoginPassword
     * @request POST:/api/internal/auth/login/password
     */
    loginPassword: (data: LoginPasswordForm, params: RequestParams = {}) =>
      this.request<LoginResponse, any>({
        path: `/api/internal/auth/login/password`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags login-controller
     * @name LoginSudo
     * @request POST:/api/internal/auth/login/sudo
     */
    loginSudo: (params: RequestParams = {}) =>
      this.request<LoginResponse, any>({
        path: `/api/internal/auth/login/sudo`,
        method: "POST",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags login-controller
     * @name LoginTotp
     * @request POST:/api/internal/auth/login/totp
     */
    loginTotp: (data: LoginTotpForm, params: RequestParams = {}) =>
      this.request<LoginResponse, any>({
        path: `/api/internal/auth/login/totp`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags login-controller
     * @name LoginWebAuthN
     * @request POST:/api/internal/auth/login/webauthn
     */
    loginWebAuthN: (data: LoginWebAuthNForm, params: RequestParams = {}) =>
      this.request<LoginResponse, any>({
        path: `/api/internal/auth/login/webauthn`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags auth-controller
     * @name LoggedOut
     * @request GET:/api/internal/auth/logout
     */
    loggedOut: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/auth/logout`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags auth-controller
     * @name RefreshAccessToken
     * @request GET:/api/internal/auth/refresh
     */
    refreshAccessToken: (params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/internal/auth/refresh`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name SendResetMail
     * @request POST:/api/internal/auth/reset/send
     */
    sendResetMail: (data: ResetForm, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/auth/reset/send`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name SetNewPassword
     * @request POST:/api/internal/auth/reset/set
     */
    setNewPassword: (data: ResetForm, params: RequestParams = {}) =>
      this.request<JsonNode, any>({
        path: `/api/internal/auth/reset/set`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name VerifyResetCode
     * @request POST:/api/internal/auth/reset/verify
     */
    verifyResetCode: (data: ResetForm, params: RequestParams = {}) =>
      this.request<JsonNode, any>({
        path: `/api/internal/auth/reset/verify`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags auth-controller
     * @name Settings
     * @request POST:/api/internal/auth/settings
     */
    settings: (params: RequestParams = {}) =>
      this.request<SettingsResponse, any>({
        path: `/api/internal/auth/settings`,
        method: "POST",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags auth-controller
     * @name Signup
     * @request POST:/api/internal/auth/signup
     */
    signup: (data: SignupForm, params: RequestParams = {}) =>
      this.request<JsonNode, any>({
        path: `/api/internal/auth/signup`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name RegisterTotp
     * @request POST:/api/internal/auth/totp/register
     */
    registerTotp: (data: TotpForm, params: RequestParams = {}) =>
      this.request<JsonNode, any>({
        path: `/api/internal/auth/totp/register`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name RemoveTotp
     * @request POST:/api/internal/auth/totp/remove
     */
    removeTotp: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/auth/totp/remove`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name SetupTotp
     * @request POST:/api/internal/auth/totp/setup
     */
    setupTotp: (params: RequestParams = {}) =>
      this.request<TotpSetupResponse, any>({
        path: `/api/internal/auth/totp/setup`,
        method: "POST",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name VerifyTotp
     * @request POST:/api/internal/auth/totp/verify
     */
    verifyTotp: (data: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/auth/totp/verify`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name PrepareWebauthnLogin
     * @request POST:/api/internal/auth/webauthn/assert
     */
    prepareWebauthnLogin: (data: string, params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/internal/auth/webauthn/assert`,
        method: "POST",
        body: data,
        type: ContentType.Text,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name RegisterWebauthn
     * @request POST:/api/internal/auth/webauthn/register
     */
    registerWebauthn: (data: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/auth/webauthn/register`,
        method: "POST",
        body: data,
        type: ContentType.Text,
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name RenameWebauthn
     * @request POST:/api/internal/auth/webauthn/rename
     */
    renameWebauthn: (data: RenameRequest, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/auth/webauthn/rename`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name SetupWebauthn
     * @request POST:/api/internal/auth/webauthn/setup
     */
    setupWebauthn: (data: string, params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/internal/auth/webauthn/setup`,
        method: "POST",
        body: data,
        type: ContentType.Text,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags credential-controller
     * @name UnregisterWebauthnDevice
     * @request POST:/api/internal/auth/webauthn/unregister
     */
    unregisterWebauthnDevice: (data: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/auth/webauthn/unregister`,
        method: "POST",
        body: data,
        type: ContentType.Text,
        ...params,
      }),

    /**
     * No description
     *
     * @tags avatar-controller
     * @name GetAvatar
     * @request GET:/api/internal/avatar/{type}/{subject}.webp
     */
    getAvatar: (type: string, subject: string, params: RequestParams = {}) =>
      this.request<File, any>({
        path: `/api/internal/avatar/${type}/${subject}.webp`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags channel-controller
     * @name CheckColor
     * @request GET:/api/internal/channels/checkColor
     */
    checkColor: (
      query: {
        /** @format int64 */
        projectId: number;
        color: Color;
        existingColor?: Color;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/channels/checkColor`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags channel-controller
     * @name CheckName1
     * @request GET:/api/internal/channels/checkName
     */
    checkName1: (
      query: {
        /** @format int64 */
        projectId: number;
        name: string;
        existingName?: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/channels/checkName`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags channel-controller
     * @name CreateChannel
     * @request POST:/api/internal/channels/{projectId}/create
     */
    createChannel: (projectId: number, data: ChannelForm, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/channels/${projectId}/create`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags channel-controller
     * @name EditChannel
     * @request POST:/api/internal/channels/{projectId}/edit
     */
    editChannel: (projectId: number, data: EditChannelForm, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/channels/${projectId}/edit`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags channel-controller
     * @name GetChannels
     * @request GET:/api/internal/channels/{project}
     */
    getChannels: (project: string, params: RequestParams = {}) =>
      this.request<HangarChannel[], any>({
        path: `/api/internal/channels/${project}`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags channel-controller
     * @name DeleteChannel
     * @request POST:/api/internal/channels/{project}/delete/{channel}
     */
    deleteChannel: (project: string, channel: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/channels/${project}/delete/${channel}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags cors-proxy-controller
     * @name Proxy1
     * @request GET:/api/internal/cors/
     */
    proxy1: (
      query: {
        url: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<JsonNode, any>({
        path: `/api/internal/cors/`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetAnnouncements
     * @request GET:/api/internal/data/announcements
     */
    getAnnouncements: (params: RequestParams = {}) =>
      this.request<Announcement[], any>({
        path: `/api/internal/data/announcements`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetCategories
     * @request GET:/api/internal/data/categories
     */
    getCategories: (params: RequestParams = {}) =>
      this.request<CategoryData[], any>({
        path: `/api/internal/data/categories`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetColors
     * @request GET:/api/internal/data/channelColors
     */
    getColors: (params: RequestParams = {}) =>
      this.request<ColorData[], any>({
        path: `/api/internal/data/channelColors`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetFlagReasons
     * @request GET:/api/internal/data/flagReasons
     */
    getFlagReasons: (params: RequestParams = {}) =>
      this.request<FlagReasonData[], any>({
        path: `/api/internal/data/flagReasons`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetGlobalRoles
     * @request GET:/api/internal/data/globalRoles
     */
    getGlobalRoles: (params: RequestParams = {}) =>
      this.request<RoleData[], any>({
        path: `/api/internal/data/globalRoles`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetLicenses
     * @request GET:/api/internal/data/licenses
     */
    getLicenses: (params: RequestParams = {}) =>
      this.request<string[], any>({
        path: `/api/internal/data/licenses`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetLoggedActions
     * @request GET:/api/internal/data/loggedActions
     */
    getLoggedActions: (params: RequestParams = {}) =>
      this.request<string[], any>({
        path: `/api/internal/data/loggedActions`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetOrganizationRoles
     * @request GET:/api/internal/data/orgRoles
     */
    getOrganizationRoles: (params: RequestParams = {}) =>
      this.request<RoleData[], any>({
        path: `/api/internal/data/orgRoles`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetPermissions
     * @request GET:/api/internal/data/permissions
     */
    getPermissions: (params: RequestParams = {}) =>
      this.request<PermissionData[], any>({
        path: `/api/internal/data/permissions`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetPlatforms
     * @request GET:/api/internal/data/platforms
     */
    getPlatforms: (params: RequestParams = {}) =>
      this.request<PlatformData[], any>({
        path: `/api/internal/data/platforms`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetProjectRoles
     * @request GET:/api/internal/data/projectRoles
     */
    getProjectRoles: (params: RequestParams = {}) =>
      this.request<RoleData[], any>({
        path: `/api/internal/data/projectRoles`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetPrompts
     * @request GET:/api/internal/data/prompts
     */
    getPrompts: (params: RequestParams = {}) =>
      this.request<PromptData[], any>({
        path: `/api/internal/data/prompts`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetSecurity
     * @request GET:/api/internal/data/security
     */
    getSecurity: (params: RequestParams = {}) =>
      this.request<Security, any>({
        path: `/api/internal/data/security`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetValidations
     * @request GET:/api/internal/data/validations
     */
    getValidations: (params: RequestParams = {}) =>
      this.request<Validations, any>({
        path: `/api/internal/data/validations`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name Info
     * @request GET:/api/internal/data/version-info
     */
    info: (params: RequestParams = {}) =>
      this.request<VersionInfo, any>({
        path: `/api/internal/data/version-info`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags backend-data-controller
     * @name GetVisibilities
     * @request GET:/api/internal/data/visibilities
     */
    getVisibilities: (params: RequestParams = {}) =>
      this.request<VisibilityData[], any>({
        path: `/api/internal/data/visibilities`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags flag-controller
     * @name Flag
     * @request POST:/api/internal/flags/
     */
    flag: (data: FlagForm, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/flags/`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags flag-controller
     * @name GetResolvedFlags
     * @request GET:/api/internal/flags/resolved
     */
    getResolvedFlags: (
      query: {
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultHangarProjectFlag, any>({
        path: `/api/internal/flags/resolved`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags flag-controller
     * @name GetUnresolvedFlags
     * @request GET:/api/internal/flags/unresolved
     */
    getUnresolvedFlags: (
      query: {
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultHangarProjectFlag, any>({
        path: `/api/internal/flags/unresolved`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags flag-controller
     * @name GetUnresolvedFlagsQueueSize
     * @request GET:/api/internal/flags/unresolvedamount
     */
    getUnresolvedFlagsQueueSize: (params: RequestParams = {}) =>
      this.request<number, any>({
        path: `/api/internal/flags/unresolvedamount`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags flag-controller
     * @name GetNotifications2
     * @request GET:/api/internal/flags/{id}/notifications
     */
    getNotifications2: (id: number, params: RequestParams = {}) =>
      this.request<HangarProjectFlagNotification[], any>({
        path: `/api/internal/flags/${id}/notifications`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags flag-controller
     * @name NotifyReportParty
     * @request POST:/api/internal/flags/{id}/notify
     */
    notifyReportParty: (id: number, data: ReportNotificationForm, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/flags/${id}/notify`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags flag-controller
     * @name Resolve
     * @request POST:/api/internal/flags/{id}/resolve/{resolve}
     */
    resolve: (id: number, resolve: boolean, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/flags/${id}/resolve/${resolve}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags flag-controller
     * @name GetFlags
     * @request GET:/api/internal/flags/{slug}
     */
    getFlags: (slug: string, params: RequestParams = {}) =>
      this.request<HangarProjectFlag[], any>({
        path: `/api/internal/flags/${slug}`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags image-proxy-controller
     * @name Proxy
     * @request GET:/api/internal/image/**
     */
    proxy: (params: RequestParams = {}) =>
      this.request<StreamingResponseBody, any>({
        path: `/api/internal/image/**`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetUserInvites
     * @request GET:/api/internal/invites
     */
    getUserInvites: (params: RequestParams = {}) =>
      this.request<Invites, any>({
        path: `/api/internal/invites`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetUserInvites1
     * @request POST:/api/internal/invites
     */
    getUserInvites1: (params: RequestParams = {}) =>
      this.request<Invites, any>({
        path: `/api/internal/invites`,
        method: "POST",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name UpdateOrganizationInviteStatus
     * @request GET:/api/internal/invites/organization/{id}/{status}
     */
    updateOrganizationInviteStatus: (id: number, status: InviteStatus, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/invites/organization/${id}/${status}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name UpdateOrganizationInviteStatus1
     * @request POST:/api/internal/invites/organization/{id}/{status}
     */
    updateOrganizationInviteStatus1: (id: number, status: InviteStatus, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/invites/organization/${id}/${status}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name UpdateProjectInviteStatus
     * @request GET:/api/internal/invites/project/{id}/{status}
     */
    updateProjectInviteStatus: (id: number, status: InviteStatus, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/invites/project/${id}/${status}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name UpdateProjectInviteStatus1
     * @request POST:/api/internal/invites/project/{id}/{status}
     */
    updateProjectInviteStatus1: (id: number, status: InviteStatus, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/invites/project/${id}/${status}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags jar-scanning-controller
     * @name GetResults
     * @request GET:/api/internal/jarscanning/result/{versionId}
     */
    getResults: (versionId: number, params: RequestParams = {}) =>
      this.request<JarScanResult[], any>({
        path: `/api/internal/jarscanning/result/${versionId}`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags jar-scanning-controller
     * @name GetResult
     * @request GET:/api/internal/jarscanning/result/{versionId}/{platform}
     */
    getResult: (versionId: number, platform: Platform, params: RequestParams = {}) =>
      this.request<JarScanResult, any>({
        path: `/api/internal/jarscanning/result/${versionId}/${platform}`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags jar-scanning-controller
     * @name Scan
     * @request POST:/api/internal/jarscanning/scan/{versionId}/{platform}
     */
    scan: (versionId: number, platform: Platform, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/jarscanning/scan/${versionId}/${platform}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name MarkNotificationAsRead2
     * @request GET:/api/internal/markallread
     */
    markNotificationAsRead2: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/markallread`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name MarkNotificationAsRead3
     * @request POST:/api/internal/markallread
     */
    markNotificationAsRead3: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/markallread`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags meili-webhook-controller
     * @name Handle
     * @request POST:/api/internal/meili/webhook
     */
    handle: (data: Webhook[], params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/meili/webhook`,
        method: "POST",
        body: data,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetNotifications
     * @request GET:/api/internal/notifications
     */
    getNotifications: (
      query: {
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultHangarNotification, any>({
        path: `/api/internal/notifications`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetNotifications1
     * @request POST:/api/internal/notifications
     */
    getNotifications1: (
      query: {
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultHangarNotification, any>({
        path: `/api/internal/notifications`,
        method: "POST",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name MarkNotificationAsRead
     * @request GET:/api/internal/notifications/{id}
     */
    markNotificationAsRead: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/notifications/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name MarkNotificationAsRead1
     * @request POST:/api/internal/notifications/{id}
     */
    markNotificationAsRead1: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/notifications/${id}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags o-auth-controller
     * @name Register
     * @request POST:/api/internal/oauth/register
     */
    register: (data: OAuthSignupForm, params: RequestParams = {}) =>
      this.request<OAuthSignupResponse, any>({
        path: `/api/internal/oauth/register`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags o-auth-controller
     * @name Callback
     * @request GET:/api/internal/oauth/{provider}/callback
     */
    callback: (
      provider: string,
      query: {
        code?: string;
        state: string;
        error?: string;
        error_description?: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/oauth/${provider}/callback`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags o-auth-controller
     * @name Login
     * @request GET:/api/internal/oauth/{provider}/login
     */
    login: (
      provider: string,
      query: {
        mode: OAuthMode;
        returnUrl?: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<string, any>({
        path: `/api/internal/oauth/${provider}/login`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags o-auth-controller
     * @name Unlink
     * @request POST:/api/internal/oauth/{provider}/unlink/{id}
     */
    unlink: (provider: string, id: string, params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/internal/oauth/${provider}/unlink/${id}`,
        method: "POST",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags onboarding-controller
     * @name CreateUser
     * @request POST:/api/internal/onboarding/createUser
     */
    createUser: (data: CreateUserRequest, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/onboarding/createUser`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags onboarding-controller
     * @name GenerateE2EData
     * @request GET:/api/internal/onboarding/generateE2EData
     */
    generateE2EData: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/onboarding/generateE2EData`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags onboarding-controller
     * @name GenerateFakeData
     * @request GET:/api/internal/onboarding/generateFakeData
     */
    generateFakeData: (
      query: {
        /** @format int32 */
        users: number;
        /** @format int32 */
        projectsPerUser: number;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/onboarding/generateFakeData`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name Create1
     * @request POST:/api/internal/organizations/create
     */
    create1: (data: CreateOrganizationForm, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/organizations/create`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name GetOrganization
     * @request GET:/api/internal/organizations/org/{org}
     */
    getOrganization: (org: string, params: RequestParams = {}) =>
      this.request<HangarOrganization, any>({
        path: `/api/internal/organizations/org/${org}`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name CancelOrganizationTransfer
     * @request POST:/api/internal/organizations/org/{org}/canceltransfer
     */
    cancelOrganizationTransfer: (org: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/organizations/org/${org}/canceltransfer`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name Delete
     * @request POST:/api/internal/organizations/org/{org}/delete
     */
    delete: (org: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/organizations/org/${org}/delete`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name AddOrganizationMember
     * @request POST:/api/internal/organizations/org/{org}/members/add
     */
    addOrganizationMember: (org: string, data: OrgMember, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/organizations/org/${org}/members/add`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name EditOrganizationMember
     * @request POST:/api/internal/organizations/org/{org}/members/edit
     */
    editOrganizationMember: (org: string, data: OrgMember, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/organizations/org/${org}/members/edit`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name LeaveOrganization
     * @request POST:/api/internal/organizations/org/{org}/members/leave
     */
    leaveOrganization: (org: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/organizations/org/${org}/members/leave`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name RemoveOrganizationMember
     * @request POST:/api/internal/organizations/org/{org}/members/remove
     */
    removeOrganizationMember: (org: string, data: OrgMember, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/organizations/org/${org}/members/remove`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name ChangeAvatar2
     * @request POST:/api/internal/organizations/org/{org}/settings/avatar
     */
    changeAvatar2: (
      org: string,
      data: {
        /** @format binary */
        avatar: File;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/organizations/org/${org}/settings/avatar`,
        method: "POST",
        body: data,
        type: ContentType.FormData,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name SaveSocials2
     * @request POST:/api/internal/organizations/org/{org}/settings/socials
     */
    saveSocials2: (org: string, data: Record<string, string>, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/organizations/org/${org}/settings/socials`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name SaveTagline2
     * @request POST:/api/internal/organizations/org/{org}/settings/tagline
     */
    saveTagline2: (org: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/organizations/org/${org}/settings/tagline`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name TransferOrganization
     * @request POST:/api/internal/organizations/org/{org}/transfer
     */
    transferOrganization: (org: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/organizations/org/${org}/transfer`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name ValidateName
     * @request GET:/api/internal/organizations/validate
     */
    validateName: (
      query: {
        name: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/organizations/validate`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name ChangeUserOrganizationMembershipVisibility
     * @request POST:/api/internal/organizations/{org}/userOrganizationsVisibility
     */
    changeUserOrganizationMembershipVisibility: (
      org: string,
      query: {
        hidden: boolean;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/organizations/${org}/userOrganizationsVisibility`,
        method: "POST",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name GetUserOrganizationRoles
     * @request GET:/api/internal/organizations/{user}/userOrganizations
     */
    getUserOrganizationRoles: (user: string, params: RequestParams = {}) =>
      this.request<Record<string, OrganizationRoleTable>, any>({
        path: `/api/internal/organizations/${user}/userOrganizations`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags organization-controller
     * @name GetUserOrganizationMembershipVisibility
     * @request GET:/api/internal/organizations/{user}/userOrganizationsVisibility
     */
    getUserOrganizationMembershipVisibility: (user: string, params: RequestParams = {}) =>
      this.request<Record<string, boolean>, any>({
        path: `/api/internal/organizations/${user}/userOrganizationsVisibility`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-page-controller
     * @name CheckName
     * @request GET:/api/internal/pages/checkName
     */
    checkName: (
      query: {
        /** @format int64 */
        projectId: number;
        name: string;
        /** @format int64 */
        parentId?: number;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/pages/checkName`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-page-controller
     * @name ConvertBbCode
     * @request POST:/api/internal/pages/convert-bbcode
     */
    convertBbCode: (data: StringContent, params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/internal/pages/convert-bbcode`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-page-controller
     * @name CreateProjectPage
     * @request POST:/api/internal/pages/create/{projectId}
     */
    createProjectPage: (projectId: number, data: NewProjectPage, params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/internal/pages/create/${projectId}`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-page-controller
     * @name DeleteProjectPage
     * @request POST:/api/internal/pages/delete/{projectId}/{pageId}
     */
    deleteProjectPage: (projectId: number, pageId: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/pages/delete/${projectId}/${pageId}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-page-controller
     * @name ListProjectPages
     * @request GET:/api/internal/pages/list/{projectId}
     */
    listProjectPages: (projectId: number, params: RequestParams = {}) =>
      this.request<HangarProjectPage[], any>({
        path: `/api/internal/pages/list/${projectId}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-page-controller
     * @name GetProjectPage
     * @request GET:/api/internal/pages/page/{project}/**
     */
    getProjectPage: (project: string, params: RequestParams = {}) =>
      this.request<ProjectPageTable, any>({
        path: `/api/internal/pages/page/${project}/**`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-page-controller
     * @name SaveProjectPage
     * @request POST:/api/internal/pages/save/{projectId}/{pageId}
     */
    saveProjectPage: (projectId: number, pageId: number, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/pages/save/${projectId}/${pageId}`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name CreateProject
     * @request POST:/api/internal/projects/create
     */
    createProject: (data: NewProjectForm, params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/internal/projects/create`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-admin-controller
     * @name AddProjectNote
     * @request POST:/api/internal/projects/notes/{projectId}
     */
    addProjectNote: (projectId: number, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/notes/${projectId}`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-admin-controller
     * @name GetProjectNotes
     * @request GET:/api/internal/projects/notes/{slugOrId}
     */
    getProjectNotes: (slugOrId: string, params: RequestParams = {}) =>
      this.request<HangarProjectNote[], any>({
        path: `/api/internal/projects/notes/${slugOrId}`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name PossibleProjectCreators
     * @request GET:/api/internal/projects/possibleOwners
     */
    possibleProjectCreators: (params: RequestParams = {}) =>
      this.request<PossibleProjectOwner[], any>({
        path: `/api/internal/projects/possibleOwners`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name ProjectRedirect
     * @request GET:/api/internal/projects/project-redirect/{slugOrId}
     */
    projectRedirect: (slugOrId: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project-redirect/${slugOrId}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name SetProjectStarred
     * @request POST:/api/internal/projects/project/{id}/star/{state}
     */
    setProjectStarred: (id: number, state: boolean, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${id}/star/${state}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name SetProjectWatching
     * @request POST:/api/internal/projects/project/{id}/watch/{state}
     */
    setProjectWatching: (id: number, state: boolean, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${id}/watch/${state}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name SoftDeleteProject
     * @request POST:/api/internal/projects/project/{projectId}/manage/delete
     */
    softDeleteProject: (projectId: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${projectId}/manage/delete`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name HardDeleteProject
     * @request POST:/api/internal/projects/project/{projectId}/manage/hardDelete
     */
    hardDeleteProject: (projectId: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${projectId}/manage/hardDelete`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name GetHangarProject
     * @request GET:/api/internal/projects/project/{slugOrId}
     */
    getHangarProject: (slugOrId: string, params: RequestParams = {}) =>
      this.request<HangarProject, any>({
        path: `/api/internal/projects/project/${slugOrId}`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name CancelProjectTransfer
     * @request POST:/api/internal/projects/project/{slugOrId}/canceltransfer
     */
    cancelProjectTransfer: (slugOrId: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${slugOrId}/canceltransfer`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name AddProjectMember
     * @request POST:/api/internal/projects/project/{slugOrId}/members/add
     */
    addProjectMember: (slugOrId: string, data: ProjectMember, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${slugOrId}/members/add`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name EditProjectMember
     * @request POST:/api/internal/projects/project/{slugOrId}/members/edit
     */
    editProjectMember: (slugOrId: string, data: ProjectMember, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${slugOrId}/members/edit`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name LeaveProject
     * @request POST:/api/internal/projects/project/{slugOrId}/members/leave
     */
    leaveProject: (slugOrId: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${slugOrId}/members/leave`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name RemoveProjectMember
     * @request POST:/api/internal/projects/project/{slugOrId}/members/remove
     */
    removeProjectMember: (slugOrId: string, data: ProjectMember, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${slugOrId}/members/remove`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name SetPinnedStatus1
     * @request POST:/api/internal/projects/project/{slugOrId}/pin/{state}
     */
    setPinnedStatus1: (slugOrId: string, state: boolean, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${slugOrId}/pin/${state}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name RenameProject
     * @request POST:/api/internal/projects/project/{slugOrId}/rename
     */
    renameProject: (slugOrId: string, data: StringContent, params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/internal/projects/project/${slugOrId}/rename`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name ResetProjectIcon
     * @request POST:/api/internal/projects/project/{slugOrId}/resetIcon
     */
    resetProjectIcon: (slugOrId: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${slugOrId}/resetIcon`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name SaveProjectIcon
     * @request POST:/api/internal/projects/project/{slugOrId}/saveIcon
     */
    saveProjectIcon: (
      slugOrId: string,
      data: {
        /** @format binary */
        projectIcon: File;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${slugOrId}/saveIcon`,
        method: "POST",
        body: data,
        type: ContentType.FormData,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name SaveProjectSettings1
     * @request POST:/api/internal/projects/project/{slugOrId}/settings
     */
    saveProjectSettings1: (slugOrId: string, data: ProjectSettingsForm, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${slugOrId}/settings`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name SaveProjectSettings
     * @request POST:/api/internal/projects/project/{slugOrId}/sponsors
     */
    saveProjectSettings: (slugOrId: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${slugOrId}/sponsors`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name TransferProject
     * @request POST:/api/internal/projects/project/{slugOrId}/transfer
     */
    transferProject: (slugOrId: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/project/${slugOrId}/transfer`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-controller
     * @name ValidateProjectName
     * @request GET:/api/internal/projects/validateName
     */
    validateProjectName: (
      query: {
        value: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/projects/validateName`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-admin-controller
     * @name ChangeProjectVisibility
     * @request POST:/api/internal/projects/visibility/{projectId}
     */
    changeProjectVisibility: (projectId: number, data: VisibilityChangeForm, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/visibility/${projectId}`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags project-admin-controller
     * @name SendProjectForApproval
     * @request POST:/api/internal/projects/visibility/{projectId}/sendforapproval
     */
    sendProjectForApproval: (projectId: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/projects/visibility/${projectId}/sendforapproval`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name ReadPrompt
     * @request GET:/api/internal/read-prompt/{prompt}
     */
    readPrompt: (prompt: Prompt, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/read-prompt/${prompt}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name ReadPrompt1
     * @request POST:/api/internal/read-prompt/{prompt}
     */
    readPrompt1: (prompt: Prompt, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/read-prompt/${prompt}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetReadNotifications
     * @request GET:/api/internal/readnotifications
     */
    getReadNotifications: (
      query: {
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultHangarNotification, any>({
        path: `/api/internal/readnotifications`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetReadNotifications1
     * @request POST:/api/internal/readnotifications
     */
    getReadNotifications1: (
      query: {
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultHangarNotification, any>({
        path: `/api/internal/readnotifications`,
        method: "POST",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetRecentNotifications
     * @request GET:/api/internal/recentnotifications
     */
    getRecentNotifications: (
      query: {
        /** @format int32 */
        amount: number;
      },
      params: RequestParams = {}
    ) =>
      this.request<HangarNotification[], any>({
        path: `/api/internal/recentnotifications`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetRecentNotifications1
     * @request POST:/api/internal/recentnotifications
     */
    getRecentNotifications1: (
      query: {
        /** @format int32 */
        amount: number;
      },
      params: RequestParams = {}
    ) =>
      this.request<HangarNotification[], any>({
        path: `/api/internal/recentnotifications`,
        method: "POST",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags review-controller
     * @name GetVersionReviews
     * @request GET:/api/internal/reviews/{versionId}/reviews
     */
    getVersionReviews: (versionId: number, params: RequestParams = {}) =>
      this.request<HangarReview[], any>({
        path: `/api/internal/reviews/${versionId}/reviews`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags review-controller
     * @name ApproveVersionReview
     * @request POST:/api/internal/reviews/{versionId}/reviews/approve
     */
    approveVersionReview: (versionId: number, data: ReviewMessage, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/reviews/${versionId}/reviews/approve`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags review-controller
     * @name ApprovePartialVersionReview
     * @request POST:/api/internal/reviews/{versionId}/reviews/approvePartial
     */
    approvePartialVersionReview: (versionId: number, data: ReviewMessage, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/reviews/${versionId}/reviews/approvePartial`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags review-controller
     * @name AddVersionReviewMessage
     * @request POST:/api/internal/reviews/{versionId}/reviews/message
     */
    addVersionReviewMessage: (versionId: number, data: ReviewMessage, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/reviews/${versionId}/reviews/message`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags review-controller
     * @name ReopenVersionReview
     * @request POST:/api/internal/reviews/{versionId}/reviews/reopen
     */
    reopenVersionReview: (versionId: number, data: ReviewMessage, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/reviews/${versionId}/reviews/reopen`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags review-controller
     * @name StartVersionReview
     * @request POST:/api/internal/reviews/{versionId}/reviews/start
     */
    startVersionReview: (versionId: number, data: ReviewMessage, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/reviews/${versionId}/reviews/start`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags review-controller
     * @name StopVersionReview
     * @request POST:/api/internal/reviews/{versionId}/reviews/stop
     */
    stopVersionReview: (versionId: number, data: ReviewMessage, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/reviews/${versionId}/reviews/stop`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags review-controller
     * @name UndoApproval
     * @request POST:/api/internal/reviews/{versionId}/reviews/undoApproval
     */
    undoApproval: (versionId: number, data: ReviewMessage, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/reviews/${versionId}/reviews/undoApproval`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetUnreadNotifications2
     * @request GET:/api/internal/unreadcount
     */
    getUnreadNotifications2: (params: RequestParams = {}) =>
      this.request<number, any>({
        path: `/api/internal/unreadcount`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetUnreadNotifications3
     * @request POST:/api/internal/unreadcount
     */
    getUnreadNotifications3: (params: RequestParams = {}) =>
      this.request<number, any>({
        path: `/api/internal/unreadcount`,
        method: "POST",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetUnreadNotifications
     * @request GET:/api/internal/unreadnotifications
     */
    getUnreadNotifications: (
      query: {
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultHangarNotification, any>({
        path: `/api/internal/unreadnotifications`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetUnreadNotifications1
     * @request POST:/api/internal/unreadnotifications
     */
    getUnreadNotifications1: (
      query: {
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultHangarNotification, any>({
        path: `/api/internal/unreadnotifications`,
        method: "POST",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetCurrentUser
     * @request GET:/api/internal/users/@me
     */
    getCurrentUser: (params: RequestParams = {}) =>
      this.request<JsonNode, any>({
        path: `/api/internal/users/@me`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name GetCurrentUser1
     * @request POST:/api/internal/users/@me
     */
    getCurrentUser1: (params: RequestParams = {}) =>
      this.request<JsonNode, any>({
        path: `/api/internal/users/@me`,
        method: "POST",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name SaveSettings2
     * @request GET:/api/internal/users/anon/settings/
     */
    saveSettings2: (data: UserSettings, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/anon/settings/`,
        method: "GET",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name SaveSettings3
     * @request POST:/api/internal/users/anon/settings/
     */
    saveSettings3: (data: UserSettings, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/anon/settings/`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name PossibleAltAccounts
     * @request GET:/api/internal/users/{userName}/alts
     */
    possibleAltAccounts: (userName: string, params: RequestParams = {}) =>
      this.request<string[], any>({
        path: `/api/internal/users/${userName}/alts`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name PossibleAltAccounts1
     * @request POST:/api/internal/users/{userName}/alts
     */
    possibleAltAccounts1: (userName: string, params: RequestParams = {}) =>
      this.request<string[], any>({
        path: `/api/internal/users/${userName}/alts`,
        method: "POST",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name SaveSettings
     * @request GET:/api/internal/users/{userName}/settings/
     */
    saveSettings: (userName: string, data: UserSettings, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/`,
        method: "GET",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name SaveSettings1
     * @request POST:/api/internal/users/{userName}/settings/
     */
    saveSettings1: (userName: string, data: UserSettings, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name ChangeAvatar
     * @request GET:/api/internal/users/{userName}/settings/avatar
     */
    changeAvatar: (
      userName: string,
      data: {
        /** @format binary */
        avatar: File;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/avatar`,
        method: "GET",
        body: data,
        type: ContentType.FormData,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name ChangeAvatar1
     * @request POST:/api/internal/users/{userName}/settings/avatar
     */
    changeAvatar1: (
      userName: string,
      data: {
        /** @format binary */
        avatar: File;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/avatar`,
        method: "POST",
        body: data,
        type: ContentType.FormData,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name SaveProfileSettings
     * @request GET:/api/internal/users/{userName}/settings/profile
     */
    saveProfileSettings: (userName: string, data: UserProfileSettings, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/profile`,
        method: "GET",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name SaveProfileSettings1
     * @request POST:/api/internal/users/{userName}/settings/profile
     */
    saveProfileSettings1: (userName: string, data: UserProfileSettings, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/profile`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name ResetTagline
     * @request GET:/api/internal/users/{userName}/settings/resetTagline
     */
    resetTagline: (userName: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/resetTagline`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name ResetTagline1
     * @request POST:/api/internal/users/{userName}/settings/resetTagline
     */
    resetTagline1: (userName: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/resetTagline`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name SaveSocials
     * @request GET:/api/internal/users/{userName}/settings/socials
     */
    saveSocials: (userName: string, data: Record<string, string>, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/socials`,
        method: "GET",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name SaveSocials1
     * @request POST:/api/internal/users/{userName}/settings/socials
     */
    saveSocials1: (userName: string, data: Record<string, string>, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/socials`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name SaveTagline
     * @request GET:/api/internal/users/{userName}/settings/tagline
     */
    saveTagline: (userName: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/tagline`,
        method: "GET",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags hangar-user-controller
     * @name SaveTagline1
     * @request POST:/api/internal/users/{userName}/settings/tagline
     */
    saveTagline1: (userName: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/users/${userName}/settings/tagline`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags version-controller
     * @name CreateVersion
     * @request POST:/api/internal/versions/version/{id}/create
     */
    createVersion: (id: number, data: PendingVersion, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/versions/version/${id}/create`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags version-controller
     * @name Create
     * @request POST:/api/internal/versions/version/{id}/upload
     */
    create: (
      id: number,
      data: {
        channel: string;
        data: MultipartFileOrUrl[];
        files?: File[];
      },
      params: RequestParams = {}
    ) =>
      this.request<PendingVersion, any>({
        path: `/api/internal/versions/version/${id}/upload`,
        method: "POST",
        body: data,
        type: ContentType.FormData,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags version-controller
     * @name SoftDeleteVersion
     * @request POST:/api/internal/versions/version/{projectId}/{versionId}/delete
     */
    softDeleteVersion: (projectId: number, versionId: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/versions/version/${projectId}/${versionId}/delete`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags version-controller
     * @name HardDeleteVersion
     * @request POST:/api/internal/versions/version/{projectId}/{versionId}/hardDelete
     */
    hardDeleteVersion: (projectId: string, versionId: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/versions/version/${projectId}/${versionId}/hardDelete`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags version-controller
     * @name SetPinnedStatus
     * @request POST:/api/internal/versions/version/{projectId}/{versionId}/pinned
     */
    setPinnedStatus: (
      projectId: number,
      versionId: number,
      query: {
        value: boolean;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/internal/versions/version/${projectId}/${versionId}/pinned`,
        method: "POST",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags version-controller
     * @name RestoreVersion
     * @request POST:/api/internal/versions/version/{projectId}/{versionId}/restore
     */
    restoreVersion: (projectId: number, versionId: string, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/versions/version/${projectId}/${versionId}/restore`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags version-controller
     * @name SaveDescription
     * @request POST:/api/internal/versions/version/{projectId}/{versionId}/saveDescription
     */
    saveDescription: (projectId: number, versionId: number, data: StringContent, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/versions/version/${projectId}/${versionId}/saveDescription`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags version-controller
     * @name SavePlatformVersions
     * @request POST:/api/internal/versions/version/{projectId}/{versionId}/savePlatformVersions
     */
    savePlatformVersions: (projectId: number, versionId: number, data: UpdatePlatformVersions, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/versions/version/${projectId}/${versionId}/savePlatformVersions`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags version-controller
     * @name SavePluginDependencies
     * @request POST:/api/internal/versions/version/{projectId}/{versionId}/savePluginDependencies
     */
    savePluginDependencies: (projectId: number, versionId: number, data: UpdatePluginDependencies, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/versions/version/${projectId}/${versionId}/savePluginDependencies`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags version-controller
     * @name Download
     * @request GET:/api/internal/versions/version/{project}/versions/{version}/{platform}/download
     */
    download: (project: string, version: string, platform: Platform, params: RequestParams = {}) =>
      this.request<JsonNode, any>({
        path: `/api/internal/versions/version/${project}/versions/${version}/${platform}/download`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags version-controller
     * @name TrackDownload
     * @request GET:/api/internal/versions/version/{versionId}/{platform}/track
     */
    trackDownload: (versionId: number, platform: Platform, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/internal/versions/version/${versionId}/${platform}/track`,
        method: "GET",
        ...params,
      }),

    /**
     * @description `Log-in` with your API key in order to be able to call other endpoints authenticated. The returned JWT should be specified as a header in all following requests: `Authorization: HangarAuth your.jwt`
     *
     * @tags Authentication
     * @name Authenticate
     * @summary Creates an API JWT
     * @request POST:/api/v1/authenticate
     */
    authenticate: (
      query: {
        /** JWT */
        apiKey: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<ApiSession, ApiSession>({
        path: `/api/v1/authenticate`,
        method: "POST",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns all users that have at least one public project. Requires the `view_public_info` permission.
     *
     * @tags Users
     * @name GetAuthors
     * @summary Returns all users with at least one public project
     * @request GET:/api/v1/authors
     * @secure
     */
    getAuthors: (
      query: {
        /** The search query */
        query: string;
        /** Pagination information */
        pagination: RequestPagination;
        /** Used to sort the result */
        sort?: "name" | "createdAt" | "projectCount";
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultUser, PaginatedResultUser>({
        path: `/api/v1/authors`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Deletes an API key. Requires the `edit_api_keys` permission.
     *
     * @tags API Keys
     * @name DeleteKey
     * @summary Deletes an API key
     * @request DELETE:/api/v1/keys
     * @secure
     */
    deleteKey: (
      query: {
        /** The name of the key to delete */
        name: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, void>({
        path: `/api/v1/keys`,
        method: "DELETE",
        query: query,
        secure: true,
        ...params,
      }),

    /**
     * @description Fetches a list of API Keys. Requires the `edit_api_keys` permission.
     *
     * @tags API Keys
     * @name GetKeys
     * @summary Fetches a list of API Keys
     * @request GET:/api/v1/keys
     * @secure
     */
    getKeys: (params: RequestParams = {}) =>
      this.request<ApiKey[], ApiKey[]>({
        path: `/api/v1/keys`,
        method: "GET",
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Creates an API key. Requires the `edit_api_keys` permission.
     *
     * @tags API Keys
     * @name CreateKey
     * @summary Creates an API key
     * @request POST:/api/v1/keys
     * @secure
     */
    createKey: (data: CreateAPIKeyForm, params: RequestParams = {}) =>
      this.request<string, string>({
        path: `/api/v1/keys`,
        method: "POST",
        body: data,
        secure: true,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description Edits a page of a project. Requires the `edit_page` permission in the project or owning organization.
     *
     * @tags Pages
     * @name EditPage
     * @summary Edits a page of a project
     * @request PATCH:/api/v1/pages/edit/{project}
     * @secure
     */
    editPage: (project: string, data: PageEditForm, params: RequestParams = {}) =>
      this.request<void, void>({
        path: `/api/v1/pages/edit/${project}`,
        method: "PATCH",
        body: data,
        secure: true,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description Edits the main page of a project. Requires the `edit_page` permission in the project or owning organization.
     *
     * @tags Pages
     * @name EditMainPage
     * @summary Edits the main page of a project
     * @request PATCH:/api/v1/pages/editmain/{project}
     * @secure
     */
    editMainPage: (project: string, data: StringContent, params: RequestParams = {}) =>
      this.request<void, void>({
        path: `/api/v1/pages/editmain/${project}`,
        method: "PATCH",
        body: data,
        secure: true,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description Returns the main page of a project. Requires visibility of the page.
     *
     * @tags Pages
     * @name GetMainPage
     * @summary Returns the main page of a project
     * @request GET:/api/v1/pages/main/{project}
     */
    getMainPage: (project: string, params: RequestParams = {}) =>
      this.request<string, string>({
        path: `/api/v1/pages/main/${project}`,
        method: "GET",
        ...params,
      }),

    /**
     * @description Returns a page of a project. Requires visibility of the page.
     *
     * @tags Pages
     * @name GetPage
     * @summary Returns a page of a project
     * @request GET:/api/v1/pages/page/{project}
     */
    getPage: (
      project: string,
      query: {
        /** The path of the page */
        path: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<string, string>({
        path: `/api/v1/pages/page/${project}`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description Returns a list of permissions you have in the given context
     *
     * @tags Permissions
     * @name ShowPermissions
     * @summary Returns your permissions
     * @request GET:/api/v1/permissions
     * @secure
     */
    showPermissions: (
      query?: {
        /**
         * Deprecated alias for `project`
         * @deprecated
         */
        slug?: string;
        /** The id or name of the organization to check permissions in. Must not be used together with `project` */
        organization?: string;
        /** The id or slug of the project to check permissions in. Must not be used together with `organization` */
        project?: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<UserPermissions, UserPermissions>({
        path: `/api/v1/permissions`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Checks whether you have all the provided permissions in the given context
     *
     * @tags Permissions
     * @name HasAll
     * @summary Checks whether you have all the provided permissions
     * @request GET:/api/v1/permissions/hasAll
     * @secure
     */
    hasAll: (
      query: {
        /**
         * The permissions to check
         * @maxItems 50
         * @minItems 0
         * @uniqueItems true
         */
        permissions: NamedPermission[];
        /**
         * Deprecated alias for `project`
         * @deprecated
         */
        slug?: string;
        /** The id or name of the organization to check permissions in. Must not be used together with `slug` */
        organization?: string;
        /** The id or slug of the project to check permissions in. Must not be used together with `organization` */
        project?: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<PermissionCheck, PermissionCheck>({
        path: `/api/v1/permissions/hasAll`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Checks whether you have at least one of the provided permissions in the given context
     *
     * @tags Permissions
     * @name HasAny
     * @summary Checks whether you have at least one of the provided permissions
     * @request GET:/api/v1/permissions/hasAny
     * @secure
     */
    hasAny: (
      query: {
        /**
         * The permissions to check
         * @uniqueItems true
         */
        permissions: NamedPermission[];
        /**
         * Deprecated alias for `project`
         * @deprecated
         */
        slug?: string;
        /** The id or name of the organization to check permissions in. Must not be used together with `project` */
        organization?: string;
        /** The id or slug of the project to check permissions in. Must not be used together with `organization` */
        project?: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<PermissionCheck, PermissionCheck>({
        path: `/api/v1/permissions/hasAny`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Searches all the projects on Hangar, or for a single user. Requires the `view_public_info` permission.
     *
     * @tags Projects
     * @name GetProjects
     * @summary Searches the projects on Hangar
     * @request GET:/api/v1/projects
     * @secure
     */
    getProjects: (
      query: {
        /**
         * Whether to prioritize the project with an exact name match if present
         * @deprecated
         * @default true
         */
        prioritizeExactMatch?: boolean;
        /** Pagination information */
        pagination: RequestPagination;
        /** Used to sort the result */
        sort?: "views" | "downloads" | "newest" | "stars" | "updated" | "recent_downloads" | "recent_views" | "slug";
        /** A category to filter for */
        category?: string;
        /** A platform to filter for */
        platform?: string;
        /** The author of the project */
        owner?: string;
        /**
         * Deprecated: Use 'query' instead
         * @deprecated
         */
        q?: string;
        /** The query to use when searching */
        query?: string;
        /** A license to filter for */
        license?: string;
        /** A platform version to filter for */
        version?: string;
        /** A tag to filter for */
        tag?: string;
        /** The member of the project */
        member?: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultProject, PaginatedResultProject>({
        path: `/api/v1/projects`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Versions
     * @name GetLatestVersion
     * @request GET:/api/v1/projects/{author}/{slugOrId}/latest
     * @deprecated
     */
    getLatestVersion: (
      author: string,
      slugOrId: string,
      query: {
        /** The channel to return the latest version for */
        channel: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<string, any>({
        path: `/api/v1/projects/${author}/${slugOrId}/latest`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Versions
     * @name GetLatestReleaseVersion
     * @request GET:/api/v1/projects/{author}/{slugOrId}/latestrelease
     * @deprecated
     */
    getLatestReleaseVersion: (author: string, slugOrId: string, params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/v1/projects/${author}/${slugOrId}/latestrelease`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Versions
     * @name UploadVersion1
     * @request POST:/api/v1/projects/{author}/{slugOrId}/upload
     * @deprecated
     */
    uploadVersion1: (
      author: string,
      slugOrId: string,
      data: {
        /** The version files in order of selected platforms, if any */
        files?: File[];
        /** Version data. See the VersionUpload schema for more info */
        versionUpload: VersionUpload;
      },
      params: RequestParams = {}
    ) =>
      this.request<UploadedVersion, any>({
        path: `/api/v1/projects/${author}/${slugOrId}/upload`,
        method: "POST",
        body: data,
        type: ContentType.FormData,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Versions
     * @name GetVersions
     * @request GET:/api/v1/projects/{author}/{slugOrId}/versions
     * @deprecated
     */
    getVersions: (
      author: string,
      slugOrId: string,
      query: {
        /** Pagination information */
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultVersion, any>({
        path: `/api/v1/projects/${author}/${slugOrId}/versions`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Versions
     * @name GetVersion
     * @request GET:/api/v1/projects/{author}/{slugOrId}/versions/{nameOrId}
     * @deprecated
     */
    getVersion: (author: string, slugOrId: string, nameOrId: string, params: RequestParams = {}) =>
      this.request<Version, any>({
        path: `/api/v1/projects/${author}/${slugOrId}/versions/${nameOrId}`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Versions
     * @name GetVersionStats
     * @request GET:/api/v1/projects/{author}/{slugOrId}/versions/{nameOrId}/stats
     * @deprecated
     */
    getVersionStats: (
      author: string,
      slugOrId: string,
      nameOrId: string,
      query: {
        /**
         * The first date to include in the result
         * @format date-time
         */
        fromDate: string;
        /**
         * The last date to include in the result
         * @format date-time
         */
        toDate: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<Record<string, VersionStats>, any>({
        path: `/api/v1/projects/${author}/${slugOrId}/versions/${nameOrId}/stats`,
        method: "GET",
        query: query,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Versions
     * @name DownloadVersion1
     * @request GET:/api/v1/projects/{author}/{slugOrId}/versions/{nameOrId}/{platform}/download
     * @deprecated
     */
    downloadVersion1: (author: string, slugOrId: string, nameOrId: string, platform: Platform, params: RequestParams = {}) =>
      this.request<JsonNode, any>({
        path: `/api/v1/projects/${author}/${slugOrId}/versions/${nameOrId}/${platform}/download`,
        method: "GET",
        format: "json",
        ...params,
      }),

    /**
     * @description Returns info on a specific project. Requires the `view_public_info` permission.
     *
     * @tags Projects
     * @name GetProject
     * @summary Returns info on a specific project
     * @request GET:/api/v1/projects/{slugOrId}
     * @secure
     */
    getProject: (slugOrId: string, params: RequestParams = {}) =>
      this.request<Project, Project>({
        path: `/api/v1/projects/${slugOrId}`,
        method: "GET",
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns the latest version of a project. Requires the `view_public_info` permission in the project or owning organization.
     *
     * @tags Versions
     * @name LatestVersion
     * @summary Returns the latest version of a project for a specific channel
     * @request GET:/api/v1/projects/{slugOrId}/latest
     * @secure
     */
    latestVersion: (
      slugOrId: string,
      query: {
        /** The channel to return the latest version for */
        channel: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<string, string>({
        path: `/api/v1/projects/${slugOrId}/latest`,
        method: "GET",
        query: query,
        secure: true,
        ...params,
      }),

    /**
     * @description Returns the latest version of a project. Requires the `view_public_info` permission in the project or owning organizations.
     *
     * @tags Versions
     * @name LatestReleaseVersion
     * @summary Returns the latest release version of a project
     * @request GET:/api/v1/projects/{slugOrId}/latestrelease
     * @secure
     */
    latestReleaseVersion: (slugOrId: string, params: RequestParams = {}) =>
      this.request<string, string>({
        path: `/api/v1/projects/${slugOrId}/latestrelease`,
        method: "GET",
        secure: true,
        ...params,
      }),

    /**
     * @description Returns the members of a project. Requires the `view_public_info` permission.
     *
     * @tags Projects
     * @name GetProjectMembers
     * @summary Returns the members of a project
     * @request GET:/api/v1/projects/{slugOrId}/members
     * @secure
     */
    getProjectMembers: (
      slugOrId: string,
      query: {
        /** Pagination information */
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultProjectMember, PaginatedResultProjectMember>({
        path: `/api/v1/projects/${slugOrId}/members`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns the stargazers of a project. Requires the `view_public_info` permission.
     *
     * @tags Projects
     * @name GetProjectStargazers
     * @summary Returns the stargazers of a project
     * @request GET:/api/v1/projects/{slugOrId}/stargazers
     * @secure
     */
    getProjectStargazers: (
      slugOrId: string,
      query: {
        /** Pagination information */
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultUser, PaginatedResultUser>({
        path: `/api/v1/projects/${slugOrId}/stargazers`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns the stats (downloads and views) for a project per day for a certain date range. Requires the `is_subject_member` permission.
     *
     * @tags Projects
     * @name ShowProjectStats
     * @summary Returns the stats for a project
     * @request GET:/api/v1/projects/{slugOrId}/stats
     * @secure
     */
    showProjectStats: (
      slugOrId: string,
      query: {
        /**
         * The first date to include in the result
         * @format date-time
         */
        fromDate: string;
        /**
         * The last date to include in the result
         * @format date-time
         */
        toDate: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<Record<string, DayProjectStats>, Record<string, DayProjectStats>>({
        path: `/api/v1/projects/${slugOrId}/stats`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Creates a new version for a project. Requires the `create_version` permission in the project or owning organization. Make sure you provide the contents of this request as multipart/form-data. You can find a simple example implementation written in Java here: https://gist.github.com/kennytv/a227d82249f54e0ad35005330256fee2
     *
     * @tags Versions
     * @name UploadVersion
     * @summary Creates a new version and returns parts of its metadata
     * @request POST:/api/v1/projects/{slugOrId}/upload
     * @secure
     */
    uploadVersion: (
      slugOrId: string,
      data: {
        /** The version files in order of selected platforms, if any */
        files?: File[];
        /** Version data. See the VersionUpload schema for more info */
        versionUpload: VersionUpload;
      },
      params: RequestParams = {}
    ) =>
      this.request<UploadedVersion, UploadedVersion>({
        path: `/api/v1/projects/${slugOrId}/upload`,
        method: "POST",
        body: data,
        secure: true,
        type: ContentType.FormData,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns all versions of a project. Requires the `view_public_info` permission in the project or owning organization.
     *
     * @tags Versions
     * @name ListVersions
     * @summary Returns all versions of a project
     * @request GET:/api/v1/projects/{slugOrId}/versions
     * @secure
     */
    listVersions: (
      slugOrId: string,
      query: {
        /** Pagination information */
        pagination: RequestPagination;
        /**
         * Whether to include hidden-by-default channels in the result, defaults to try
         * @default true
         */
        includeHiddenChannels?: boolean;
        /** A name of a version channel to filter for */
        channel?: string;
        /** A platform name to filter for */
        platform?: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultVersion, PaginatedResultVersion>({
        path: `/api/v1/projects/${slugOrId}/versions`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns a specific version of a project. Requires the `view_public_info` permission in the project or owning organization.
     *
     * @tags Versions
     * @name ShowVersion
     * @summary Returns a specific version of a project
     * @request GET:/api/v1/projects/{slugOrId}/versions/{nameOrId}
     * @secure
     */
    showVersion: (slugOrId: string, nameOrId: string, params: RequestParams = {}) =>
      this.request<Version, Version>({
        path: `/api/v1/projects/${slugOrId}/versions/${nameOrId}`,
        method: "GET",
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns the stats (downloads) for a version per day for a certain date range. Requires the `is_subject_member` permission.
     *
     * @tags Versions
     * @name ShowVersionStats
     * @summary Returns the stats for a version
     * @request GET:/api/v1/projects/{slugOrId}/versions/{nameOrId}/stats
     * @secure
     */
    showVersionStats: (
      slugOrId: string,
      nameOrId: string,
      query: {
        /**
         * The first date to include in the result
         * @format date-time
         */
        fromDate: string;
        /**
         * The last date to include in the result
         * @format date-time
         */
        toDate: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<Record<string, VersionStats>, Record<string, VersionStats>>({
        path: `/api/v1/projects/${slugOrId}/versions/${nameOrId}/stats`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Downloads the file for a specific platform of a version. Requires visibility of the project and version.
     *
     * @tags Versions
     * @name DownloadVersion
     * @summary Downloads a version
     * @request GET:/api/v1/projects/{slugOrId}/versions/{nameOrId}/{platform}/download
     * @secure
     */
    downloadVersion: (slugOrId: string, nameOrId: string, platform: Platform, params: RequestParams = {}) =>
      this.request<JsonNode, JsonNode>({
        path: `/api/v1/projects/${slugOrId}/versions/${nameOrId}/${platform}/download`,
        method: "GET",
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns the watchers of a project. Requires the `view_public_info` permission.
     *
     * @tags Projects
     * @name GetProjectWatchers
     * @summary Returns the watchers of a project
     * @request GET:/api/v1/projects/{slugOrId}/watchers
     * @secure
     */
    getProjectWatchers: (
      slugOrId: string,
      query: {
        /** Pagination information */
        pagination: RequestPagination;
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultUser, PaginatedResultUser>({
        path: `/api/v1/projects/${slugOrId}/watchers`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns Hanagr staff. Requires the `view_public_info` permission.
     *
     * @tags Users
     * @name GetStaff
     * @summary Returns Hangar staff
     * @request GET:/api/v1/staff
     * @secure
     */
    getStaff: (
      query: {
        /** The search query */
        query: string;
        /** Pagination information */
        pagination: RequestPagination;
        /** Used to sort the result */
        sort?: "name" | "createdAt" | "roles";
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultUser, PaginatedResultUser>({
        path: `/api/v1/staff`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns a list of users based on a search query. Requires the `view_public_info` permission.
     *
     * @tags Users
     * @name ShowUsers
     * @summary Searches for users
     * @request GET:/api/v1/users
     * @secure
     */
    showUsers: (
      query: {
        /** The search query */
        query: string;
        /** Pagination information */
        pagination: RequestPagination;
        /** Used to sort the result */
        sort?: "name" | "createdAt" | "projectCount" | "locked" | "org" | "roles";
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultUser, PaginatedResultUser>({
        path: `/api/v1/users`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns a specific user. Requires the `view_public_info` permission.
     *
     * @tags Users
     * @name GetUser
     * @summary Returns a specific user
     * @request GET:/api/v1/users/{user}
     * @secure
     */
    getUser: (user: string, params: RequestParams = {}) =>
      this.request<User, User>({
        path: `/api/v1/users/${user}`,
        method: "GET",
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns the pinned projects for a specific user. Requires the `view_public_info` permission.
     *
     * @tags Users
     * @name GetUserPinnedProjects
     * @summary Returns the pinned projects for a specific user
     * @request GET:/api/v1/users/{user}/pinned
     * @secure
     */
    getUserPinnedProjects: (user: string, params: RequestParams = {}) =>
      this.request<ProjectCompact[], ProjectCompact[]>({
        path: `/api/v1/users/${user}/pinned`,
        method: "GET",
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns the starred projects for a specific user. Requires the `view_public_info` permission.
     *
     * @tags Users
     * @name ShowStarred
     * @summary Returns the starred projects for a specific user
     * @request GET:/api/v1/users/{user}/starred
     * @secure
     */
    showStarred: (
      user: string,
      query: {
        /** Pagination information */
        pagination: RequestPagination;
        /** Used to sort the result */
        sort?: "views" | "downloads" | "newest" | "stars" | "updated" | "recent_downloads" | "recent_views" | "slug";
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultProjectCompact, PaginatedResultProjectCompact>({
        path: `/api/v1/users/${user}/starred`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns the watched projects for a specific user. Requires the `view_public_info` permission.
     *
     * @tags Users
     * @name GetUserWatching
     * @summary Returns the watched projects for a specific user
     * @request GET:/api/v1/users/{user}/watching
     * @secure
     */
    getUserWatching: (
      user: string,
      query: {
        /** Pagination information */
        pagination: RequestPagination;
        /** Used to sort the result */
        sort?: "views" | "downloads" | "newest" | "stars" | "updated" | "recent_downloads" | "recent_views" | "slug";
      },
      params: RequestParams = {}
    ) =>
      this.request<PaginatedResultProjectCompact, PaginatedResultProjectCompact>({
        path: `/api/v1/users/${user}/watching`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * No description
     *
     * @tags Projects
     * @name ProjectByVersionHash
     * @summary Returns project of the first version that matches the given file hash (SHA-256)
     * @request GET:/api/v1/versions/hash/{hash}
     * @secure
     */
    projectByVersionHash: (hash: string, params: RequestParams = {}) =>
      this.request<Project, Project>({
        path: `/api/v1/versions/hash/${hash}`,
        method: "GET",
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns a specific version by its ID. Requires the `view_public_info` permission in the project or owning organization.
     *
     * @tags Versions
     * @name ShowVersionById
     * @summary Returns a specific version by its ID
     * @request GET:/api/v1/versions/{id}
     * @secure
     */
    showVersionById: (id: string, params: RequestParams = {}) =>
      this.request<Version, Version>({
        path: `/api/v1/versions/${id}`,
        method: "GET",
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Returns the stats (downloads) for a version per day for a certain date range. Requires the `is_subject_member` permission.
     *
     * @tags Versions
     * @name ShowVersionStatsById
     * @summary Returns the stats for a version by its ID
     * @request GET:/api/v1/versions/{id}/stats
     * @secure
     */
    showVersionStatsById: (
      id: string,
      query: {
        /**
         * The first date to include in the result
         * @format date-time
         */
        fromDate: string;
        /**
         * The last date to include in the result
         * @format date-time
         */
        toDate: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<Record<string, VersionStats>, Record<string, VersionStats>>({
        path: `/api/v1/versions/${id}/stats`,
        method: "GET",
        query: query,
        secure: true,
        format: "json",
        ...params,
      }),

    /**
     * @description Downloads the file for a specific platform of a version. Requires visibility of the project and version.
     *
     * @tags Versions
     * @name DownloadVersionById
     * @summary Downloads a version by its ID
     * @request GET:/api/v1/versions/{id}/{platform}/download
     * @secure
     */
    downloadVersionById: (id: string, platform: Platform, params: RequestParams = {}) =>
      this.request<JsonNode, JsonNode>({
        path: `/api/v1/versions/${id}/${platform}/download`,
        method: "GET",
        secure: true,
        format: "json",
        ...params,
      }),
  };
}
