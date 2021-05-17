import { Context, Middleware } from '@nuxt/types';
import { PermissionCheck } from 'hangar-api';
import { NamedPermission, PermissionType } from '~/types/enums';
import { AuthState } from '~/store/auth';

const loggedInMiddleware =
    (code: number, msg?: string): Middleware =>
    ({ store, error }: Context) => {
        if (!store.state.auth.authenticated) {
            error({
                message: msg,
                statusCode: code,
            });
        }
    };

export function NotLoggedIn(constructor: Function) {
    addMiddleware(constructor, ({ store, redirect }) => {
        if (store.state.auth.authenticated) {
            redirect('/');
        }
    });
}

export function LoggedIn(constructor: Function) {
    addMiddleware(constructor, loggedInMiddleware(401, 'You must be logged in to perform this action'));
}

export function CurrentUser(constructor: Function) {
    addMiddleware(constructor, loggedInMiddleware(403), ({ params, store, $perms, error }) => {
        if (!$perms.canEditAllUserSettings) {
            if (!params.user) {
                throw new TypeError("Must have 'user' as a route param to use CurrentUser");
            }
            if (params.user !== (store.state.auth as AuthState).user!.name) {
                error({
                    statusCode: 403,
                });
            }
        }
    });
}

// TODO this maybe should use the global permissions store in the JWT to reduce db lookups?
export function GlobalPermission(...permissions: NamedPermission[]) {
    const middleware: Middleware = ({ error, $api, $util }: Context) => {
        return $api
            .request<PermissionCheck>('permissions/hasAll', true, 'get', {
                permissions,
            })
            .then((check) => {
                if (check.type !== PermissionType.GLOBAL || !check.result) {
                    error({
                        message: 'Not Found',
                        statusCode: 404,
                    });
                }
            })
            .catch($util.handlePageRequestError);
    };

    return function (constructor: Function) {
        addMiddleware(constructor, loggedInMiddleware(404), middleware);
    };
}

export function ProjectPermission(...permissions: NamedPermission[]) {
    const middleware: Middleware = ({ store, route, $util, error }: Context) => {
        if (!route.params.author || !route.params.slug) {
            throw new Error("Can't use this decorator on a route without `author` and `slug` path params");
        }
        if (!(store.state.auth as AuthState).routePermissions) {
            error({
                statusCode: 404,
            });
            return;
        }
        if (!$util.hasPerms(...permissions)) {
            error({
                statusCode: 404,
            });
        }
    };

    return function (constructor: Function) {
        addMiddleware(constructor, loggedInMiddleware(404), middleware);
    };
}

function addMiddleware(constructor: Function, ...middleware: Middleware[]): void {
    if (!constructor.prototype.middleware) {
        constructor.prototype.middleware = [...middleware];
    } else if (typeof constructor.prototype.middleware === 'string') {
        constructor.prototype.middleware = [constructor.prototype.middleware, ...middleware];
    } else if (Array.isArray(constructor.prototype.middleware)) {
        constructor.prototype.middleware = [...constructor.prototype.middleware, ...middleware];
    } else {
        throw new TypeError('Unable to add permissions middleware');
    }
}
