declare module 'hangar-internal' {
    import { Project } from 'hangar-api';

    interface ProjectOwner {
        id: number;
        name: string;
        userId: number;
        isOrganization: boolean;
    }

    interface HangarProjectInfo {
        publicVersions: number;
        noteCount: number;
        starCount: number;
        watcherCount: number;
    }

    interface HangarProject extends Project {
        owner: ProjectOwner;
        members: object[];
        lastVisibilityChangeComment: string;
        lastVisibilityChangeUserName: string;
        info: HangarProjectInfo;
    }
}
