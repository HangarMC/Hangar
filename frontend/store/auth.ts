import { MutationTree } from 'vuex';
import { HangarUser } from 'hangar-internal';

export const state = () => ({
    authenticated: false,
    user: null as HangarUser | null,
    token: null as string | null,
    routePermissions: null as string | null,
});

export type AuthState = ReturnType<typeof state>;

export const mutations: MutationTree<AuthState> = {
    SET_USER: (state: AuthState, user: HangarUser) => {
        state.user = user;
    },
    SET_AUTHED: (state: AuthState, auth: boolean) => (state.authenticated = auth),
    SET_TOKEN: (state: AuthState, token: string) => {
        state.token = token;
    },
    SET_ROUTE_PERMISSIONS: (state: AuthState, perms: string) => {
        state.routePermissions = perms;
    },
};
