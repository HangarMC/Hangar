import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { IPermission } from 'hangar-api';
import { NamedPermission } from '~/types/enums';
import { RootState } from '~/store';

const createUtil = ({ $util, store }: Context) => {
    class Perms {
        get canEditSubjectSettings() {
            return $util.hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS);
        }

        // Subjects
        get canManageSubjectMembers() {
            return $util.hasPerms(NamedPermission.MANAGE_SUBJECT_MEMBERS);
        }

        get isSubjectOwner() {
            return $util.hasPerms(NamedPermission.IS_SUBJECT_OWNER);
        }

        get isSubjectMember() {
            return $util.hasPerms(NamedPermission.IS_SUBJECT_MEMBER);
        }

        // Projects
        get canCreateProject() {
            return $util.hasPerms(NamedPermission.CREATE_PROJECT);
        }

        get canEditPage() {
            return $util.hasPerms(NamedPermission.EDIT_PAGE);
        }

        get canDeleteProject() {
            return $util.hasPerms(NamedPermission.DELETE_PROJECT);
        }

        // Versions
        get canCreateVersion() {
            return $util.hasPerms(NamedPermission.CREATE_VERSION);
        }

        get canEditVersion() {
            return $util.hasPerms(NamedPermission.EDIT_VERSION);
        }

        get canDeleteVersion() {
            return $util.hasPerms(NamedPermission.DELETE_VERSION);
        }

        get canEditTags() {
            return $util.hasPerms(NamedPermission.EDIT_TAGS);
        }

        // Organizations
        get canCreateOrganization() {
            return $util.hasPerms(NamedPermission.CREATE_ORGANIZATION);
        }

        get canPostAsOrganization() {
            return $util.hasPerms(NamedPermission.POST_AS_ORGANIZATION);
        }

        // Moderators
        get canAccessModNotesAndFlags() {
            return $util.hasPerms(NamedPermission.MOD_NOTES_AND_FLAGS);
        }

        get canSeeHidden() {
            return $util.hasPerms(NamedPermission.SEE_HIDDEN);
        }

        get isStaff() {
            return $util.hasPerms(NamedPermission.IS_STAFF);
        }

        get isReviewer() {
            return $util.hasPerms(NamedPermission.REVIEWER);
        }

        // Developers
        get canViewHealth() {
            return $util.hasPerms(NamedPermission.VIEW_HEALTH);
        }

        get canViewIp() {
            return $util.hasPerms(NamedPermission.VIEW_IP);
        }

        get canViewStats() {
            return $util.hasPerms(NamedPermission.VIEW_STATS);
        }

        get canViewLogs() {
            return $util.hasPerms(NamedPermission.VIEW_LOGS);
        }

        // Admins
        get canDoManualValueChanges() {
            return $util.hasPerms(NamedPermission.MANUAL_VALUE_CHANGES);
        }

        get canHardDeleteProject() {
            return $util.hasPerms(NamedPermission.HARD_DELETE_PROJECT);
        }

        get canHardDeleteVersion() {
            return $util.hasPerms(NamedPermission.HARD_DELETE_VERSION);
        }

        get canEditAllUserSettings() {
            return $util.hasPerms(NamedPermission.EDIT_ALL_USER_SETTINGS);
        }

        convert(value: NamedPermission): IPermission {
            return (store.state as RootState).permissions.get(value)!;
        }
    }

    return new Perms();
};

type permsType = ReturnType<typeof createUtil>;

declare module 'vue/types/vue' {
    interface Vue {
        $perms: permsType;
    }
}

declare module '@nuxt/types' {
    interface NuxtAppOptions {
        $perms: permsType;
    }

    interface Context {
        $perms: permsType;
    }
}

declare module 'vuex/types/index' {
    // eslint-disable-next-line no-unused-vars,@typescript-eslint/no-unused-vars
    interface Store<S> {
        $perms: permsType;
    }
}

export default (ctx: Context, inject: Inject) => {
    inject('perms', createUtil(ctx));
};
