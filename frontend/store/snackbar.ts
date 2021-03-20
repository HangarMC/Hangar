import { ActionTree, MutationTree } from 'vuex';
import { TranslateResult } from 'vue-i18n';
import { RootState } from '~/store/index';

export const state = () => ({
    enabled: false,
    color: null as string | null,
    messages: [] as TranslateResult[],
    timeout: 3000 as number,
});

export type SnackbarState = ReturnType<typeof state>;

export interface NotifPayload {
    color?: string;
    timeout?: number;
    message: TranslateResult;
}

function timeout(ms: number) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

export const actions: ActionTree<SnackbarState, RootState> = {
    SHOW_NOTIF: async ({ commit, state }, payload: NotifPayload) => {
        commit('SET_COLOR', payload.color);
        commit('ADD_MESSAGE', payload.message);
        if (!state.enabled) {
            commit('SET_TIMEOUT', payload.timeout);
        } else {
            commit('SET_TIMEOUT', state.timeout + (payload.timeout || 3000));
        }
        commit('SHOW_SNACKBAR', true);
        await timeout(payload.timeout || 3000);
    },
};

export const mutations: MutationTree<SnackbarState> = {
    SET_COLOR: (state: SnackbarState, color: string | null) => {
        state.color = color || 'error';
    },
    SET_TIMEOUT: (state: SnackbarState, timeout: number | null) => {
        state.timeout = timeout || 3000;
    },
    ADD_MESSAGE: (state: SnackbarState, message: TranslateResult) => {
        state.messages.push(message);
    },
    SHOW_SNACKBAR: (state: SnackbarState, show: boolean) => {
        state.enabled = show;
    },
    CLEAR_MESSAGES: (state: SnackbarState) => {
        state.messages = [];
        state.timeout = 1000;
    },
};
