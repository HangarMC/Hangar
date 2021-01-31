// TODO use decorators once typescript eslint 4.14.1 comes out Monday Jan 25
import { MutationTree } from 'vuex';
import { User } from 'hangar-api';

export const state = () => ({
    authenticated: false,
    user: (null as unknown) as User,
});

export type AuthState = ReturnType<typeof state>;

export const mutations: MutationTree<AuthState> = {
    SET_USER: (state, user: User) => (state.user = user),
    SET_AUTHED: (state, auth: boolean) => (state.authenticated = auth),
};
