import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { AxiosError } from 'axios';
import filesize from 'filesize';
// TODO fix it complaining about no type declaration file
// @ts-ignore
import { contrastRatio, HexToRGBA, parseHex } from 'vuetify/es5/util/colorUtils';
import { HangarApiException, HangarValidationException } from 'hangar-api';
import { HangarUser } from 'hangar-internal';
import { TranslateResult } from 'vue-i18n';
import { NamedPermission } from '~/types/enums';
import { RootState } from '~/store';
import { NotifPayload } from '~/store/snackbar';
import { AuthState } from '~/store/auth';

type Validation = (v: string) => boolean | string;
type ValidationArgument = (any: any) => Validation;

function handleRequestError(err: AxiosError, error: Context['error']) {
    if (!err.isAxiosError) {
        // everything should be an AxiosError
        error({
            statusCode: 500,
        });
        console.log(err);
    } else if (err.response) {
        if (err.response.data.isHangarApiException) {
            const data: HangarApiException = err.response.data;
            error({
                statusCode: data.httpError.statusCode,
                message: data.message,
            });
        } else if (err.response.data.isHangarValidationException) {
            const data: HangarValidationException = err.response.data;
            error({
                statusCode: data.httpError.statusCode,
                message: data.fieldErrors.map((f) => f.errorMsg).join(', '),
            });
        } else {
            error({
                statusCode: err.response.status,
                message: err.response.statusText,
            });
        }
    } else {
        error({
            message: "This shouldn't happen...",
        });
        console.log(err);
    }
}

const createUtil = ({ store, error }: Context) => {
    class Util {
        dummyUser() {
            return ({
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
            } as unknown) as HangarUser;
        }

        avatarUrl(name: string): string {
            return `/avatar/${name}?size=120x120`;
        }

        forumUrl(name: string): string {
            return 'https://papermc.io/forums/u/' + name;
        }

        prettyDate(date: Date | string): string {
            if (typeof date === 'string') {
                date = new Date(date);
            }
            return date.toLocaleDateString(undefined, {
                day: 'numeric',
                month: 'long',
                year: 'numeric',
            });
        }

        formatSize(input: any) {
            return filesize(input);
        }

        white = HexToRGBA(parseHex('#ffffff'));
        black = HexToRGBA(parseHex('#000000'));

        isDark(hex: string): boolean {
            const rgba = HexToRGBA(parseHex(hex));
            return contrastRatio(rgba, this.white) > 2;
        }

        isLight(hex: string): boolean {
            const rgba = HexToRGBA(parseHex(hex));
            return contrastRatio(rgba, this.black) > 2;
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

        /**
         * Used when an auth error should redirect to 404
         * @param err the axios request error
         */
        handlePageRequestError(err: AxiosError) {
            handleRequestError(err, error);
        }

        /**
         * Used for showing error popups. See handlePageRequestError to show an error page.
         * @param err the axios request error
         * @param msg optional error message
         */
        handleRequestError(err: AxiosError, msg: string | undefined = 'Could not load data') {
            if (process.server) {
                handleRequestError(err, error);
                return;
            }
            if (!err.isAxiosError) {
                // everything should be an AxiosError
                error({
                    statusCode: 500,
                });
                console.log(err);
            } else if (err.response) {
                // TODO check is msg is a i18n key and use that instead (use $te to check existence of a key)
                if (err.response.data.isHangarApiException) {
                    const data: HangarApiException = err.response.data;
                    store.dispatch('snackbar/SHOW_NOTIF', {
                        message: msg ? `${msg}: ${data.message}` : data.message,
                        color: 'error',
                        timeout: 3000,
                    } as NotifPayload);
                } else if (err.response.data.isHangarValidationException) {
                    const data: HangarValidationException = err.response.data;
                    for (const fieldError of data.fieldErrors) {
                        store.dispatch('snackbar/SHOW_NOTIF', {
                            message: fieldError.errorMsg,
                            color: 'error',
                            timeout: 3000,
                        } as NotifPayload);
                    }
                    if (msg) {
                        store.dispatch('snackbar/SHOW_NOTIF', {
                            message: msg,
                            color: 'error',
                            timeout: 3000,
                        } as NotifPayload);
                    }
                } else {
                    store.dispatch('snackbar/SHOW_NOTIF', {
                        message: msg ? `${msg}: ${err.response.statusText}` : err.response.statusText,
                        color: 'error',
                        timeout: 2000,
                    } as NotifPayload);
                }
            } else {
                console.log(err);
            }
        }

        $vc: Record<string, ValidationArgument> = {
            require: ((name: string | TranslateResult = 'Field') => (v: string) => !!v || `${name} is required`) as ValidationArgument,
        };

        $v: Record<string, Validation> = {};

        error(err: string | NotifPayload) {
            if (typeof err === 'string') {
                store.dispatch('snackbar/SHOW_NOTIF', { message: err, color: 'error' } as NotifPayload);
            } else {
                store.dispatch('snackbar/SHOW_NOTIF', err);
            }
        }

        success(msg: string) {
            store.dispatch('snackbar/SHOW_NOTIF', { message: msg, color: 'success' } as NotifPayload);
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
