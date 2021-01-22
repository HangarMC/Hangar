import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { AxiosPromise, AxiosError } from 'axios';
import { ApiSession } from 'hangar-api';
import { NuxtAxiosInstance } from '@nuxtjs/axios';
import { NuxtCookies } from 'cookie-universal-nuxt';
import { Store } from 'vuex';
import { ApiSessionType } from '~/types/enums';

const createApi = ($axios: NuxtAxiosInstance, $cookies: NuxtCookies, store: Store<any>) => {
    class API {
        getSession(): Promise<string> {
            return new Promise((resolve, reject) => {
                let session: ApiSession;
                const date = new Date();
                date.setTime(date.getTime() + 60000);

                if (store.state.auth.authenticated) {
                    session = $cookies.get('api_session');
                    if (typeof session === 'undefined' || (!isNaN(new Date(session.expires).getTime()) && new Date(session.expires) < date)) {
                        return $axios
                            .post<ApiSession>('/api/v1/authenticate/user', {}, { headers: { 'Content-Type': 'application/json' } })
                            .then(({ data }) => {
                                if (data.type !== ApiSessionType.USER) {
                                    reject(new Error('Expected user session from user authentication'));
                                } else {
                                    $cookies.set('api_session', JSON.stringify(data), {
                                        path: '/',
                                        expires: new Date(data.expires),
                                        secure: true,
                                    });
                                    resolve(data.session);
                                }
                            })
                            .catch((error) => {
                                store.commit('auth/SET_AUTHED', false);
                                reject(error);
                            });
                    } else {
                        resolve(session.session);
                    }
                } else {
                    session = $cookies.get('public_api_session');
                    if (typeof session === 'undefined' || (!isNaN(new Date(session.expires).getTime()) && new Date(session.expires) < date)) {
                        $axios
                            .post<ApiSession>('/api/v1/authenticate', {}, { headers: { 'Content-Type': 'application/json' } })
                            .then(({ data }) => {
                                if (data.type !== ApiSessionType.PUBLIC) {
                                    reject(new Error('Expected public session from public authentication'));
                                } else {
                                    $cookies.set('public_api_session', JSON.stringify(data), {
                                        path: '/',
                                        expires: new Date(data.expires),
                                        secure: true,
                                    });
                                    resolve(data.session);
                                }
                            })
                            .catch((error) => {
                                reject(error);
                            });
                    } else {
                        resolve(session.session);
                    }
                }
            });
        }

        invalidateSession(): void {
            store.commit('auth/SET_AUTHED', false);
            store.commit('auth/SET_USER', null);
            $cookies.remove('api_session', {
                path: '/',
            });
            $cookies.remove('public_api_session', {
                path: '/',
            });
        }

        requestInternal<T>(url: string, method: 'get' | 'post' = 'get', data: object = {}): Promise<T> {
            return this._request(`internal/${url}`, method, data);
        }

        request<T>(url: string, method: 'get' | 'post' = 'get', data: object = {}): Promise<T> {
            return this._request(`v1/${url}`, method, data);
        }

        private _request<T>(url: string, method: 'get' | 'post', data: object): Promise<T> {
            return new Promise<T>((resolve, reject) => {
                return this.getSession()
                    .then((session) => {
                        return ($axios({
                            method,
                            url: '/api/' + url,
                            headers: { Authorization: 'HangarApi session="' + session + '"' },
                            data,
                        }) as AxiosPromise<T>)
                            .then(({ data }) => resolve(data))
                            .catch((error: AxiosError) => {
                                reject(error);
                                // console.log(error);
                                // if (error.response && (error.response.error === 'Api session expired' || error.response.error === 'Invalid session')) {
                                //     // This should never happen but just in case we catch it and invalidate the session to definitely get a new one
                                //     this.invalidateSession();
                                //     this.request<T>(url, method, data)
                                //         .then((data) => {
                                //             resolve(data);
                                //         })
                                //         .catch((error) => {
                                //             reject(error);
                                //         });
                                // } else {
                                //     reject(error.response.statusText);
                                // }
                            });
                    })
                    .catch((reason) => {
                        console.log(reason);
                        // TODO error popup here
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
    interface Store<S> {
        $api: apiType;
    }
}

export default ({ $axios, app: { $cookies }, store }: Context, inject: Inject) => {
    inject('api', createApi($axios, $cookies, store));
};
