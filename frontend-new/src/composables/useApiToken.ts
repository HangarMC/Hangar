import type { JwtPayload } from 'jwt-decode';
import jwtDecode from 'jwt-decode';
import { useCookies } from '@vueuse/integrations';
import { useAuthStore } from '~/store/auth';
import { useAxios } from '~/composables/useAxios';

function refreshToken(): Promise<string | null> {
    return new Promise<string | null>((resolve) => {
        return useAxios
            .get<{ token: string; refreshToken: string; cookieName: string; expiresIn: number }>('/refresh')
            .then((value) => {
                useAuthStore().$patch({ token: value.data.token });
                useCookies().set(value.data.cookieName, value.data.refreshToken, {
                    path: '/',
                    expires: new Date(Date.now() + value.data.expiresIn * 1000),
                    sameSite: 'strict',
                    secure: process.env.nodeEnv === 'production'
                });
                resolve(value.data.token);
            })
            .catch(() => {
                resolve(null);
            });
    });
}

function validateToken(token: string): boolean {
    const decodedToken = jwtDecode<JwtPayload>(token);
    if (!decodedToken.exp) {
        return false;
    }
    return decodedToken.exp * 1000 > Date.now() - 10 * 1000; // check against 10 seconds earlier to mitigate tokens expiring mid-request
}

export function useApiToken(forceFetch = true): Promise<string | null> {
    const store = useAuthStore();
    if (store.token) {
        if (validateToken(store.token)) {
            return Promise.resolve(store.token);
        } else {
            return refreshToken();
        }
    } else if (forceFetch) {
        return refreshToken();
    } else {
        return Promise.resolve(null);
    }
}
