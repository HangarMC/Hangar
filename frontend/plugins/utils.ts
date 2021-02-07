import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { AxiosError } from 'axios';
import { HangarException, User } from 'hangar-api';
import { NamedPermission } from '~/types/enums';
import { RootState } from '~/store';
import { ErrorPayload } from '~/store/snackbar';
import { AuthState } from '~/store/auth';

type Validation = (v: string) => boolean | string;
type ValidationArgument = (field?: string) => Validation;

const createUtil = ({ store, error }: Context) => {
    class Util {
        dummyUser(): User {
            return {
                name: 'Dummy',
                id: 42,
                tagline: null,
                createdAt: this.prettyDate(new Date()),
                roles: [],
                headerData: {
                    globalPermission: '',
                    unreadNotifications: 1,
                    projectApprovals: 3,
                    reviewQueueCount: 0,
                    unresolvedFlags: 2,
                },
                joinDate: this.prettyDate(new Date()),
            };
        }

        avatarUrl(name: string): string {
            return `/avatar/${name}?size=120x120`;
        }

        forumUrl(name: string): string {
            return 'https://papermc.io/forums/u/' + name;
        }

        prettyDate(date: Date): string {
            return date.toLocaleDateString(undefined, {
                day: 'numeric',
                month: 'long',
                year: 'numeric',
            });
        }

        /**
         * Checks if the supplier permission has all named permissions.
         * @param namedPermission perms required
         */
        hasPerms(...namedPermission: NamedPermission[]): boolean {
            const perms = (store.state.auth as AuthState).routePermissions;
            if (perms === null) return false;
            const _perms: bigint = BigInt('0b' + perms);
            let result = true;
            for (const np of namedPermission) {
                const perm = (store.state as RootState).permissions.get(np);
                if (!perm) {
                    throw new Error(namedPermission + ' is not valid');
                }
                const val = BigInt('0b' + perm.permission.toString(2));
                result = result && (_perms & val) === val;
            }
            return result;
        }

        isCurrentUser(id: number): boolean {
            return (store.state.auth as AuthState).user?.id === id;
        }

        isLoggedIn(): boolean {
            return (store.state.auth as AuthState).user != null;
        }

        // TODO have boolean for doing error toast notification instead of error page;
        handleAxiosError(err: AxiosError) {
            if (err.response) {
                // response outside of 2xx
                const statusCode = err.response.status;
                if (err.response.data.isHangarApiException || err.response.data.isHangarValidationException) {
                    const data: HangarException = err.response.data;
                    error({
                        statusCode: data.httpError.statusCode,
                        message: data.message,
                    });
                } else {
                    error({
                        statusCode,
                        message: err.response.statusText,
                    });
                }
            } else {
                error({
                    message: 'No response from the server: ' + err.message,
                });
            }
        }

        $vc: Record<string, ValidationArgument> = {
            require: ((name: string = 'Field') => (v: string) => !!v || `${name} is required`) as ValidationArgument,
        };

        $v: Record<string, Validation> = {};

        error(err: string | ErrorPayload) {
            if (typeof err === 'string') {
                store.dispatch('snackbar/SHOW_ERROR', { message: err });
            } else {
                store.dispatch('snackbar/SHOW_ERROR', err);
            }
        }
    }

    return new Util();
};

type utilType = ReturnType<typeof createUtil>;

declare module 'vue/types/vue' {
    interface Vue {
        $util: utilType;
    }
}

declare module '@nuxt/types' {
    interface NuxtAppOptions {
        $util: utilType;
    }

    interface Context {
        $util: utilType;
    }
}

declare module 'vuex/types/index' {
    // eslint-disable-next-line no-unused-vars,@typescript-eslint/no-unused-vars
    interface Store<S> {
        $util: utilType;
    }
}

export default (ctx: Context, inject: Inject) => {
    inject('util', createUtil(ctx));
};
