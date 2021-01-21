import { parseJsonOrNull } from './utils';
import axios from 'axios';

export class API {
    static request(url, method = 'get', data = {}) {
        return new Promise((resolve, reject) => {
            return this.getSession().then((session) => {
                return axios({
                    method: method,
                    url: '/api/v1/' + url,
                    headers: { Authorization: 'HangarApi session="' + session + '"' },
                    data: data,
                })
                    .then((response) => resolve(response))
                    .catch((error) => {
                        if (error.response && (error.response.error === 'Api session expired' || error.response.error === 'Invalid session')) {
                            // This should never happen but just in case we catch it and invalidate the session to definitely get a new one
                            API.invalidateSession();
                            API.request(url, method, data)
                                .then(({ data }) => {
                                    resolve(data);
                                })
                                .catch((error) => {
                                    reject(error);
                                });
                        } else {
                            reject(error.response.statusText);
                        }
                    });
            });
        });
    }

    static getSession() {
        return new Promise((resolve, reject) => {
            let session;
            const date = new Date();
            date.setTime(date.getTime() + 60000);

            if (window.isLoggedIn) {
                session = parseJsonOrNull(localStorage.getItem('api_session'));
                if (session === null || (!isNaN(new Date(session.expires).getTime()) && new Date(session.expires) < date)) {
                    return axios
                        .post('/api/v1/authenticate/user', {}, { headers: { 'Content-Type': 'application/json' } })
                        .then((data) => {
                            if (data.type !== 'user') {
                                reject('Expected user session from user authentication');
                            } else {
                                localStorage.setItem('api_session', JSON.stringify(data));
                                resolve(data.session);
                            }
                        })
                        .catch((error) => {
                            reject(error);
                        });
                } else {
                    resolve(session.session);
                }
            } else {
                session = parseJsonOrNull(localStorage.getItem('public_api_session'));
                if (session === null || (!isNaN(new Date(session.expires).getTime()) && new Date(session.expires) < date)) {
                    axios
                        .post('/api/v1/authenticate', {}, { headers: { 'Content-Type': 'application/json' } })
                        .then(({ data }) => {
                            if (data.type !== 'public') {
                                reject('Expected public session from public authentication');
                            } else {
                                localStorage.setItem('public_api_session', JSON.stringify(data));
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

    static invalidateSession() {
        if (window.isLoggedIn) {
            localStorage.removeItem('api_session');
        } else {
            localStorage.removeItem('public_api_session');
        }
    }
}
