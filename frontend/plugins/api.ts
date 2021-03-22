import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { AxiosError, AxiosRequestConfig } from 'axios';
import jwtDecode, { JwtPayload } from 'jwt-decode';
import qs from 'qs';

const createApi = ({ $axios, store, app: { $cookies } }: Context) => {
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
                    .get<{ token: string; refreshToken: string; cookieName: string; expiresIn: number }>('/refresh')
                    .then((value) => {
                        store.commit('auth/SET_TOKEN', value.data.token);
                        $cookies.set(value.data.cookieName, value.data.refreshToken, {
                            path: '/',
                            expires: new Date(Date.now() + value.data.expiresIn * 1000),
                            sameSite: 'strict',
                            secure: process.env.nodeEnv === 'production',
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

        requestInternal<T = void>(url: string, authed: boolean = true, method: AxiosRequestConfig['method'] = 'get', data: object = {}): Promise<T> {
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
            let tokenPromise: Promise<string | null>;
            if (token && !API.validateToken(token)) {
                tokenPromise = this.getToken(true);
            } else {
                tokenPromise = Promise.resolve(token);
            }

            return tokenPromise.then((token) => {
                if (authed && !token) {
                    // TODO figure out how to not have to make the request if you know its going to fail
                }
                // if (authed && !token) {
                //     // TODO this return doesn't match others
                //     return Promise.reject({
                //         isAxiosError: true,
                //         respose: {
                //             data: {
                //                 isHangarApiException: true,
                //                 httpError: {
                //                     statusCode: 401,
                //                 },
                //                 message: 'You must be logged in',
                //             },
                //         },
                //     });
                // } else {
                //     return this._request(`internal/${url}`, token, method, data);
                // }
                return this._request(`internal/${url}`, token, method, data);
            });
        }

        private static validateToken(token: string): boolean {
            const decodedToken = jwtDecode<JwtPayload>(token);
            if (!decodedToken.exp) {
                return false;
            }
            return decodedToken.exp * 1000 > Date.now() - 10 * 1000; // check against 10 seconds earlier to mitigate tokens expiring mid-request
        }

        private _request<T>(url: string, token: string | null, method: AxiosRequestConfig['method'], data: object): Promise<T> {
            const headers: Record<string, string> = token ? { Authorization: `HangarAuth ${token}` } : {};
            return new Promise<T>((resolve, reject) => {
                return $axios
                    .request<T>({
                        method,
                        url: `/api/${url}`,
                        headers,
                        data: method?.toLowerCase() !== 'get' ? data : {},
                        params: method?.toLowerCase() === 'get' ? data : {},
                        paramsSerializer: (params) => {
                            return qs.stringify(params, {
                                arrayFormat: 'repeat',
                            });
                        },
                    })
                    .then(({ data }) => resolve(data))
                    .catch((error: AxiosError) => {
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
