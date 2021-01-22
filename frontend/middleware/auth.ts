import { Context } from '@nuxt/types';
import { ApiSession } from 'hangar-api';
import { ApiSessionType } from '~/types/enums';

export default ({ store, app: { $cookies }, $auth, redirect }: Context) => {
    if ($cookies.get('returnRoute')) {
        // is returning from login
        const returnRoute: string = $cookies.get<string>('returnRoute');
        return $auth.processLogin().then(() => {
            $cookies.remove('returnRoute', {
                path: '/',
            });
            redirect(returnRoute);
        });
    } else {
        const session = $cookies.get<ApiSession>('api_session');
        if (!session || session.type !== ApiSessionType.USER) return;
        if (!store.state.auth.authenticated) {
            return $auth.processLogin();
        } else {
            return $auth.updateUser();
        }
    }
};
