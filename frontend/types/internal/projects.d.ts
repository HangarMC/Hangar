declare module 'hangar-internal' {
    import { Table } from 'hangar-internal';
    import { Project } from 'hangar-api';

    interface ProjectOwner {
        id: number;
        name: string;
        userId: number;
        isOrganization: boolean;
    }

    interface HangarProjectInfo {
        publicVersions: number;
        flagCount: number;
        noteCount: number;
        starCount: number;
        watcherCount: number;
    }

    interface HangarProjectPage {
        id: number;
        name: string;
        slug: string;
        home: boolean;
        children: HangarProjectPage[];
    }

    interface HangarProject extends Project, Table {
        owner: ProjectOwner;
        members: object[];
        lastVisibilityChangeComment: string;
        lastVisibilityChangeUserName: string;
        info: HangarProjectInfo;
        pages: HangarProjectPage[];
    }

    interface ProjectPage extends Table {
        name: string;
        slug: string;
        contents: string;
        deletable: boolean;
    }
}
