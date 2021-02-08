declare module 'hangar-internal' {
    import { Model } from 'hangar-api';
    import { ProjectCategory } from '~/types/enums';

    interface Table extends Model {
        id: number;
    }

    interface IProjectCategory {
        title: string;
        icon: string;
        apiName: ProjectCategory;
        visible: boolean;
    }

    interface FlagReason {
        type: string;
        title: string;
    }
}
