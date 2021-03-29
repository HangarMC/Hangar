declare module 'hangar-internal' {
    import { Model } from 'hangar-api';
    import { LogContext } from '~/types/enums';

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
}
