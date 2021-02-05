declare module 'hangar-internal' {
    import { ProjectCategory } from '~/types/enums';

    interface IProjectCategory {
        title: string;
        icon: string;
        apiName: ProjectCategory;
        visible: boolean;
    }
}
