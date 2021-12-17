import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { AxiosError } from 'axios';
import filesize from 'filesize';
// TODO fix it complaining about no type declaration file
// @ts-ignore
// import { contrastRatio, HexToRGBA, parseHex } from 'vuetify/es5/util/colorUtils'; // TODO remove or fix
import { HangarApiException, HangarValidationException, MultiHangarApiException } from 'hangar-api';
import { HangarProject, HangarUser } from 'hangar-internal';
import { TranslateResult } from 'vue-i18n';
import { NuxtI18nInstance } from '@nuxtjs/i18n';
import { NamedPermission, Visibility } from '~/types/enums';
import { RootState } from '~/store';
import { NotifPayload } from '~/store/snackbar';
import { AuthState } from '~/store/auth';

// for page request errors
function handleRequestError(err: AxiosError, error: Context['error'], i18n: Context['app']['i18n']) {
    if (!err.isAxiosError) {
        // everything should be an AxiosError
        error({
            statusCode: 500,
        });
        console.log(err);
    } else if (err.response) {
        if (err.response.data.isHangarApiException) {
            const data: HangarApiException = err.response.data.isMultiException ? err.response.data.exceptions[0] : err.response.data;
            error({
                statusCode: data.httpError.statusCode,
                message: i18n.te(data.message) ? <string>i18n.t(data.message) : data.message,
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

function collectErrors(exception: HangarApiException | MultiHangarApiException, i18n: Context['app']['i18n']): TranslateResult[] {
    if (!exception.isMultiException) {
        return [i18n.te(exception.message) ? i18n.t(exception.message, [exception.messageArgs]) : exception.message];
    } else {
        const res: TranslateResult[] = [];
        for (const ex of exception.exceptions) {
            res.push(i18n.te(ex.message) ? i18n.t(ex.message, ex.messageArgs) : ex.message);
        }
        return res;
    }
}

const createUtil = ({ store, error, app: { i18n } }: Context) => {
    class Util {
        dummyUser(): HangarUser {
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
            } as unknown as HangarUser;
        }

        dummyProject(): HangarProject {
            return { namespace: { owner: 'test', slug: 'test2' }, visibility: Visibility.NEW } as unknown as HangarProject;
        }

        avatarUrl(name: string): string {
            return `/avatar/${name}?size=120x120`;
        }

        projectUrl(owner: string, slug: string): string {
            return `/api/internal/projects/project/${owner}/${slug}/icon`;
        }

        forumUrl(name: string): string {
            return 'https://papermc.io/forums/u/' + name;
        }

        prettyDate(date: Date | string | number): string {
            if (typeof date === 'string' || typeof date === 'number') {
                date = new Date(date);
            }
            return date.toLocaleDateString(undefined, {
                day: 'numeric',
                month: 'long',
                year: 'numeric',
            });
        }

        prettyDateTime(date: Date | string | number): string {
            if (typeof date === 'string' || typeof date === 'number') {
                date = new Date(date);
            }
            return date.toLocaleDateString(undefined, {
                day: 'numeric',
                month: '2-digit',
                year: 'numeric',
                hour: 'numeric',
                minute: 'numeric',
            });
        }

        /**
         * Requires yyyy-MM-DD format
         * @param dateString
         */
        fromISOString(dateString: string): Date {
            const ds = dateString.split('-').map((s) => parseInt(s));
            return new Date(ds[0], ds[1] - 1, ds[2]);
        }

        toISODateString(date: Date): string {
            return `${date.getFullYear()}-${('0' + (date.getMonth() + 1)).slice(-2)}-${('0' + date.getDate()).slice(-2)}`;
        }

        formatSize(input: any) {
            return filesize(input);
        }

        // TODO either fix or remove this
        // white = HexToRGBA(parseHex('#ffffff'));
        // black = HexToRGBA(parseHex('#000000'));

        isDark(hex: string): boolean {
            // const rgba = HexToRGBA(parseHex(hex));
            // return contrastRatio(rgba, this.white) > 2;
            return hex === null;
        }

        isLight(hex: string): boolean {
            // const rgba = HexToRGBA(parseHex(hex));
            // return contrastRatio(rgba, this.black) > 2;
            return hex === null;
        }

        /**
         * Checks if the supplier permission has all named permissions.
         * @param namedPermission perms required
         */
        hasPerms(...namedPermission: NamedPermission[]): boolean {
            const perms = (store.state.auth as AuthState).routePermissions;
            if (!perms) return false;
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

        /**
         * Used when an auth error should redirect to 404
         * @param err the axios request error
         */
        handlePageRequestError(err: AxiosError) {
            handleRequestError(err, error, i18n);
        }

        /**
         * Used for showing error popups. See handlePageRequestError to show an error page.
         * @param err the axios request error
         * @param msg optional error message (should be i18n)
         */
        handleRequestError(err: AxiosError, msg: string | undefined = undefined) {
            if (process.server) {
                handleRequestError(err, error, i18n);
                return;
            }
            if (!err.isAxiosError) {
                // everything should be an AxiosError
                error({
                    statusCode: 500,
                });
                console.log(err);
            } else if (err.response) {
                if (err.response.data.isHangarApiException) {
                    for (const errorMsg of collectErrors(err.response.data, i18n)) {
                        store.dispatch('snackbar/SHOW_NOTIF', {
                            message: msg ? `${i18n.t(msg)}: ${errorMsg}` : errorMsg,
                            color: 'error',
                            timeout: 3000,
                        } as NotifPayload);
                    }
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
                            message: i18n.t(msg),
                            color: 'error',
                            timeout: 3000,
                        } as NotifPayload);
                    }
                } else {
                    store.dispatch('snackbar/SHOW_NOTIF', {
                        message: msg ? `${i18n.t(msg)}: ${err.response.statusText}` : err.response.statusText,
                        color: 'error',
                        timeout: 2000,
                    } as NotifPayload);
                }
            } else {
                console.log(err);
            }
        }

        $vc = {
            required:
                (name: TranslateResult = 'Field') =>
                (v: string) =>
                    !!v || i18n.t('validation.required', [name]),
            maxLength: (maxLength: number) => (v: string | any[]) => {
                return (
                    ((v === null || typeof v === 'undefined' || typeof v === 'string') && !v) ||
                    (Array.isArray(v) && v.length === 0) ||
                    v.length <= maxLength ||
                    i18n.t('validation.maxLength', [maxLength])
                );
            },
            minLength: (minLength: number) => (v: string | any[]) => {
                return (
                    ((v === null || typeof v === 'string') && !v) ||
                    (Array.isArray(v) && v.length === 0) ||
                    v.length >= minLength ||
                    i18n.t('validation.minLength', [minLength])
                );
            },
            requireNonEmptyArray:
                (name: TranslateResult = 'Field') =>
                (v: any[]) =>
                    v.length > 0 || i18n.t('validation.required', [name]),
            url: (v: string) => !v || new RegExp((store.state as RootState).validations.urlRegex).test(v) || i18n.t('validation.invalidUrl'),
            regex:
                (name: TranslateResult = 'Field', regexp: string) =>
                (v: string) => {
                    return !v || new RegExp(regexp).test(v) || i18n.t('validation.invalidFormat', [name]);
                },
            requireNumberArray: () => (v: string | any[]) => {
                return (
                    ((v === null || typeof v === 'undefined' || typeof v === 'string') && !v) ||
                    (Array.isArray(v) && v.length === 0) ||
                    (v as any[]).some((i) => !isNaN(i)) ||
                    i18n.t('validation.numberArray')
                );
            },
        };

        error(err: TranslateResult | NotifPayload) {
            if (typeof err === 'string') {
                store.dispatch('snackbar/SHOW_NOTIF', { message: err, color: 'error' } as NotifPayload);
            } else {
                store.dispatch('snackbar/SHOW_NOTIF', err);
            }
        }

        success(msg: TranslateResult) {
            store.dispatch('snackbar/SHOW_NOTIF', { message: msg, color: 'success' } as NotifPayload);
        }

        i18n(): NuxtI18nInstance {
            return i18n;
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
