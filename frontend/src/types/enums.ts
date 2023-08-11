export enum RoleCategory {
  GLOBAL = "global",
  PROJECT = "project",
  ORGANIZATION = "organization",
}

export enum ProjectCategory {
  ADMIN_TOOLS = "admin_tools",
  CHAT = "chat",
  DEV_TOOLS = "dev_tools",
  ECONOMY = "economy",
  GAMEPLAY = "gameplay",
  GAMES = "games",
  PROTECTION = "protection",
  ROLE_PLAYING = "role_playing",
  WORLD_MANAGEMENT = "world_management",
  MISC = "misc",
  UNDEFINED = "undefined",
}

export enum Visibility {
  PUBLIC = "public",
  NEW = "new",
  NEEDS_CHANGES = "needsChanges",
  NEEDS_APPROVAL = "needsApproval",
  SOFT_DELETE = "softDelete",
}

export enum ReviewState {
  UNREVIEWED = "unreviewed",
  REVIEWED = "reviewed",
  UNDER_REVIEW = "under_review",
  BACKLOG = "backlog",
  PARTIALLY_REVIEWED = "partially_reviewed",
}

export enum PermissionType {
  GLOBAL = "global",
  PROJECT = "project",
  ORGANIZATION = "organization",
}

export enum NamedPermission {
  VIEW_PUBLIC_INFO = "view_public_info",
  EDIT_OWN_USER_SETTINGS = "edit_own_user_settings",
  EDIT_API_KEYS = "edit_api_keys",

  EDIT_SUBJECT_SETTINGS = "edit_subject_settings",
  MANAGE_SUBJECT_MEMBERS = "manage_subject_members",
  IS_SUBJECT_OWNER = "is_subject_owner",
  IS_SUBJECT_MEMBER = "is_subject_member",

  CREATE_PROJECT = "create_project",
  EDIT_PAGE = "edit_page",
  DELETE_PROJECT = "delete_project",

  CREATE_VERSION = "create_version",
  EDIT_VERSION = "edit_version",
  DELETE_VERSION = "delete_version",
  EDIT_CHANNELS = "edit_channels",

  CREATE_ORGANIZATION = "create_organization",
  POST_AS_ORGANIZATION = "post_as_organization",

  MOD_NOTES_AND_FLAGS = "mod_notes_and_flags",
  SEE_HIDDEN = "see_hidden",
  IS_STAFF = "is_staff",
  REVIEWER = "reviewer",

  VIEW_HEALTH = "view_health",
  VIEW_IP = "view_ip",
  VIEW_STATS = "view_stats",
  VIEW_LOGS = "view_logs",

  MANUAL_VALUE_CHANGES = "manual_value_changes",
  HARD_DELETE_PROJECT = "hard_delete_project",
  HARD_DELETE_VERSION = "hard_delete_version",
  EDIT_ALL_USER_SETTINGS = "edit_all_user_settings",
}

export enum Platform {
  PAPER = "PAPER",
  WATERFALL = "WATERFALL",
  VELOCITY = "VELOCITY",
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

export enum Tag {
  ADDON = "ADDON",
  LIBRARY = "LIBRARY",
  SUPPORTS_FOLIA = "SUPPORTS_FOLIA",
}

export enum LogContext {
  PROJECT = "PROJECT",
  VERSION = "VERSION",
  PAGE = "PAGE",
  USER = "USER",
  ORGANIZATION = "ORGANIZATION",
}

export enum Prompt {
  CHANGE_AVATAR = "CHANGE_AVATAR",
}

export enum ChannelFlag {
  FROZEN = "FROZEN",
  UNSTABLE = "UNSTABLE",
  PINNED = "PINNED",
  SENDS_NOTIFICATIONS = "SENDS_NOTIFICATIONS",
}

export enum PinnedStatus {
  CHANNEL = "CHANNEL",
  VERSION = "VERSION",
  NONE = "NONE",
}
