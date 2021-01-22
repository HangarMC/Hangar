import { Context } from '@nuxt/types';
import { ApiSession } from 'hangar-api';
import { ApiSessionType } from '~/types/enums';

export default ({ store, app: { $cookies }, $auth }: Context) => {
    const session = $cookies.get<ApiSession>('api_session');
    if (!!session && session.type === ApiSessionType.USER && (!store.state.auth.authenticated || !store.state.auth.user)) {
        return $auth.processLogin();
    }
};
