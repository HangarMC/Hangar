import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { AxiosError } from 'axios';
import { HangarException, User } from 'hangar-api';
import { NamedPermission } from '~/types/enums';
import { RootState } from '~/store';

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
                    hasNotice: true,
                    hasProjectApprovals: true,
                    hasReviewQueue: true,
                    hasUnreadNotifications: true,
                    unresolvedFlags: true,
                },
                joinDate: this.prettyDate(new Date()),
            };
        }

        avatarUrl(_name: string): string {
            return 'https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png';
            // TODO avatar url
            // return '/avatar/' + name + '?size=120x120';
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
         * @param perms user permissions
         * @param namedPermission perms required
         */
        hasPerms(perms: bigint | string, ...namedPermission: NamedPermission[]): boolean {
            let _perms: bigint;
            let result = false;
            for (const np of namedPermission) {
                const perm = (store.state as RootState).permissions.get(np);
                if (!perm) {
                    throw new Error(namedPermission + ' is not valid');
                }
                if (typeof perms === 'string') {
                    _perms = BigInt('0b' + perm.value);
                } else {
                    _perms = perms;
                }

                result = result && (_perms & perm.permission) === perm.permission;
            }
            return result;
        }

        // TODO have boolean for doing error toast notification instead of error page;
        handleAxiosError(err: AxiosError) {
            if (err.response) {
                // response outside of 2xx
                const statusCode = err.response.status;
                if (err.response.data.isHangarException) {
                    const data: HangarException = err.response.data;
                    error({
                        statusCode: data.error.code,
                        message: data.reason,
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
