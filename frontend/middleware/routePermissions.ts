import { Context } from '@nuxt/types';
import { UserPermissions } from 'hangar-api';
import { AuthState } from '~/store/auth';

export default ({ store, route, $api, $util }: Context) => {
    if (route.params.author && route.params.slug) {
        return $api
            .request<UserPermissions>('permissions', true, 'get', {
                author: route.params.author,
                slug: route.params.slug,
            })
            .then((userPermissions) => {
                store.commit('auth/SET_ROUTE_PERMISSIONS', userPermissions.permissionBinString);
            })
            .catch(() => {
                store.commit('auth/SET_ROUTE_PERMISSIONS', null);
            });
    } else if ($util.isLoggedIn()) {
        // Catch-all (just use global permissions)
        store.commit('auth/SET_ROUTE_PERMISSIONS', (store.state.auth as AuthState).user!.headerData.globalPermission);
    } else {
        store.commit('auth/SET_ROUTE_PERMISSIONS', null);
    }
    // TODO other route permissions
};
