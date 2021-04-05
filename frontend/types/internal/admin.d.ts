declare module 'hangar-internal' {
    import { Model, ProjectNamespace } from 'hangar-api';
    import { LogContext, Platform, Visibility } from '~/types/enums';

    interface LogProject {
        id: number;
        slug: string;
        owner: string;
    }

    interface LogVersion {
        id: number;
        versionString: string;
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
        jobProperties: { [key: string]: string };
    }

    interface MissingFile {
        platform: Platform;
        versionString: string;
        fileName: string;
        namespace: ProjectNamespace;
        name: string;
    }

    interface UnhealthyProject {
        namespace: ProjectNamespace;
        topicId: number | null;
        postId: number | null;
        lastUpdated: string;
        visibility: Visibility;
    }
}
