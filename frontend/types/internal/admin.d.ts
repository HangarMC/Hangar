import { Model } from 'hangar-api';
import { HangarProject, HangarVersion, HangarProjectPage } from 'hangar-internal';

declare module 'hangar-internal' {
    interface LoggedSubject {
        id: number;
        userName: String;
    }

    interface LoggedActionType {
        name: String;
        description: String;
    }

    interface LoggedAction extends Model {
        userId: number;
        userName: String;
        address: String;
        action: LoggedActionType;
        actionContext: any;
        newState: String;
        oldState: String;
        project: HangarProject;
        version: HangarVersion;
        page: HangarProjectPage;
        subject: LoggedSubject;
    }
}
