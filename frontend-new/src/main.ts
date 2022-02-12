import 'windi.css';
import './styles/main.css';
import viteSSR, { ClientOnly } from 'vite-ssr';
import { createHead } from '@vueuse/head';
import generatedRoutes from 'virtual:generated-pages';
import { setupLayouts } from 'virtual:generated-layouts';
import { createPinia } from 'pinia';
import App from '~/App.vue';

const routes = setupLayouts(generatedRoutes);

const options: Parameters<typeof viteSSR>['1'] = {
    routes
};

export default viteSSR( App, options, async (ctx) => {
        // install all modules under `modules/`
        Object.values(import.meta.globEager('./modules/*.ts')).map(i =>
            i.install?.(ctx)
        );

        const { app, initialState } = ctx;

        app.component(ClientOnly.name, ClientOnly);

        const head = createHead();
        const pinia = createPinia();
        app.use(pinia).use(head);

        if (import.meta.env.SSR) {
            initialState.pinia = pinia.state.value;
        } else {
            pinia.state.value = initialState.pinia;
        }

        return { head };
    }
);
