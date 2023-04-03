declare module "hangar-internal" {
  import type { Model, ProjectNamespace } from "hangar-api";
  import type { LogContext, Platform, Visibility } from "~/types/enums";

  interface LogProject {
    id: number;
    slug: string;
    owner: string;
  }

  interface LogVersion {
    id: number;
    versionString: string;
    platforms: Platform[];
  }

  interface LogPage {
    id: number;
    name: string;
    slug: string;
  }

  interface LogSubject {
    id: number;
    name: string;
  }

  interface LoggedActionType {
    pgLoggedAction: string;
    name: string;
    description: string;
  }

  interface LoggedAction extends Model {
    userId: number | null;
    userName: string | null;
    address: string;
    action: LoggedActionType;
    contextType: LogContext;
    newState: string;
    oldState: string;
    project: LogProject | null;
    version: LogVersion | null;
    page: LogPage | null;
    subject: LogSubject | null;
  }

  interface Job extends Model {
    jobType: string;
    state: string;
    lastError: string;
    lastErrorDescriptor: string;
    retryAt: string;
    lastUpdated: string;
    jobProperties: Record<string, string>;
  }

  interface MissingFile {
    namespace: ProjectNamespace;
    versionString: string;
    platforms: Platform[];
  }

  interface UnhealthyProject {
    namespace: ProjectNamespace;
    lastUpdated: string;
    visibility: Visibility;
  }

  interface HealthReport {
    noTopicProjects: UnhealthyProject[];
    staleProjects: UnhealthyProject[];
    nonPublicProjects: UnhealthyProject[];
    missingFiles: MissingFile[];
    erroredJobs: Job[];
  }

  interface Activity {
    namespace: ProjectNamespace;
  }

  interface FlagActivity extends Activity {
    resolvedAt: string;
  }

  interface ReviewActivity extends Activity {
    endedAt: string;
    versionString: string;
    platforms: Platform[];
  }
}
