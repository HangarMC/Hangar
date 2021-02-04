import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { AxiosError, AxiosRequestConfig } from 'axios';
import jwtDecode, { JwtPayload } from 'jwt-decode';

const createApi = ({ $axios, store, app: { $cookies }, error }: Context) => {
    class API {
        getToken(forceFetch: boolean = true): Promise<string | null> {
            if (store.state.auth.token) {
                if (API.validateToken(store.state.auth.token)) {
                    return Promise.resolve(store.state.auth.token);
                } else {
                    return this.refreshToken();
                }
            } else if (forceFetch) {
                return this.refreshToken();
            } else {
                return Promise.resolve(null);
            }
        }

        private refreshToken(): Promise<string | null> {
            return new Promise<string | null>((resolve) => {
                return $axios
                    .get<{ token: string; refreshToken: string; cookieName: string }>('/refresh')
                    .then((value) => {
                        store.commit('auth/SET_TOKEN', value.data.token);
                        $cookies.set(value.data.cookieName, value.data.refreshToken, {
                            path: '/',
                            expires: new Date(<number>jwtDecode<JwtPayload>(value.data.token).exp * 1000),
                            sameSite: 'strict',
                            secure: process.env.NODE_ENV === 'production',
                        });
                        resolve(value.data.token);
                    })
                    .catch(() => {
                        resolve(null);
                    });
            });
        }

        request<T>(url: string, authed: boolean = true, method: AxiosRequestConfig['method'] = 'get', data: object = {}): Promise<T> {
            return this.getToken(authed).then((token) => {
                return this._request(`v1/${url}`, token, method, data);
            });
        }

        requestInternal<T>(url: string, authed: boolean = true, method: AxiosRequestConfig['method'] = 'get', data: object = {}): Promise<T> {
            return this.getToken(authed).then((token) => {
                return this.requestInternalWithToken<T>(url, token, authed, method, data);
            });
        }

        requestInternalWithToken<T>(
            url: string,
            token: string | null,
            authed: boolean = true,
            method: AxiosRequestConfig['method'] = 'get',
            data: object = {}
        ): Promise<T> {
            let promise: Promise<string | null>;
            if (token && !API.validateToken(token)) {
                promise = this.getToken(true);
            } else {
                promise = Promise.resolve(token);
            }

            return promise.then((token) => {
                if (authed && !token) {
                    error({
                        statusCode: 401,
                        message: 'Requires authorization',
                    });
                    return Promise.reject(new Error('Requires authorization'));
                } else {
                    return this._request(`internal/${url}`, token, method, data);
                }
            });
        }

        private static validateToken(token: string): boolean {
            const decodedToken = jwtDecode<JwtPayload>(token);
            if (!decodedToken.exp) {
                return false;
            }
            return decodedToken.exp * 1000 > Date.now();
        }

        private _request<T>(url: string, token: string | null, method: AxiosRequestConfig['method'], data: object): Promise<T> {
            const headers = token ? { Authorization: `HangarAuth ${token}` } : {};

            return new Promise<T>((resolve, reject) => {
                return $axios
                    .request<T>({
                        method,
                        url: `/api/${url}`,
                        headers,
                        data,
                    })
                    .then(({ data }) => resolve(data))
                    .catch((error: AxiosError) => {
                        // TODO error popup
                        reject(error);
                    });
            });
        }
    }

    return new API();
};

type apiType = ReturnType<typeof createApi>;

declare module 'vue/types/vue' {
    interface Vue {
        $api: apiType;
    }
}

declare module '@nuxt/types' {
    interface NuxtAppOptions {
        $api: apiType;
    }

    interface Context {
        $api: apiType;
    }
}

declare module 'vuex/types/index' {
    // eslint-disable-next-line no-unused-vars,@typescript-eslint/no-unused-vars
    interface Store<S> {
        $api: apiType;
    }
}

export default (ctx: Context, inject: Inject) => {
    inject('api', createApi(ctx));
};
