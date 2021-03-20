declare module 'hangar-api' {
    import { Model, Named } from 'hangar-api';
    import { NamedPermission, RoleCategory } from '~/types/enums';

    interface Role {
        assignable: boolean;
        rank?: number | null;
        value: string;
        roleId: number;
        category: RoleCategory;
        permissions: string;
        title: string;
        color: string;
    }

    interface User extends Model, Named {
        tagline: string | null;
        joinDate: string;
        roles: Role[];
        projectCount: number;
    }

    interface ApiKey extends Model, Named {
        key: string;
        identifier: string;
        permissions: NamedPermission[];
    }

    interface Organization extends Model, Named {
        owner: User;
    }
}
