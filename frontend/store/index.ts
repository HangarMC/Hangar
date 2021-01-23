import { ActionTree, MutationTree, GetterTree } from 'vuex';
import { Context } from '@nuxt/types';
import { IProjectCategory } from 'hangar-api';
import { ProjectCategory } from '~/types/enums';

export const state = () => ({
    projectCategories: (null as unknown) as Map<ProjectCategory, IProjectCategory>,
});

export type RootState = ReturnType<typeof state>;

export const mutations: MutationTree<RootState> = {
    SET_PROJECT_CATEGORIES: (state, payload: Map<ProjectCategory, IProjectCategory>) => {
        state.projectCategories = payload;
    },
};

export const actions: ActionTree<RootState, RootState> = {
    async nuxtServerInit({ commit }, { $api }: Context) {
        try {
            const categoryResult = await $api.requestInternal<IProjectCategory[]>('data/categories');
            commit(
                'SET_PROJECT_CATEGORIES',
                convertToMap<ProjectCategory, IProjectCategory>(categoryResult, (value) => value.apiName)
            );
            // others
        } catch (e) {
            console.error('ERROR FETCHING BACKEND DATA');
            console.error(e);
        }
    },
};

export const getters: GetterTree<RootState, RootState> = {
    visibleCategories: (state: RootState) => Array.from(state.projectCategories.values()).filter((value) => value.visible),
};

function convertToMap<E, T>(values: T[], toStringFunc: (value: T) => string): Map<E, T> {
    const map = new Map<E, T>();
    for (const value of values) {
        map.set((toStringFunc(value) as unknown) as E, value);
    }
    return map;
}
