import { Context } from '@nuxt/types';
import { Inject } from '@nuxt/types/app';
import { TranslateResult } from 'vue-i18n';
import { Route } from 'vue-router';
import { MetaInfo } from 'vue-meta';

const createUtil = (context: Context) => {
    class Seo {
        head(title: string | TranslateResult, description: string | TranslateResult | null, route: Route, image: string | null): MetaInfo {
            description = description || 'Plugin repository for Paper plugins and more!';
            const canonical = this.baseUrl() + (route.fullPath.endsWith('/') ? route.fullPath : route.fullPath + '/');
            image = image || 'https://docs.papermc.io/img/paper.png';
            image = image.startsWith('http') ? image : this.baseUrl() + image;
            const seo = {
                title,
                link: [{ rel: 'canonical', href: canonical }],
                meta: [
                    { hid: 'description', name: 'description', content: description },
                    { property: 'og:description', name: 'og:description', vmid: 'og:description', hid: 'og:description', content: description },
                    {
                        property: 'twitter:description',
                        name: 'twitter:description',
                        vmid: 'twitter:description',
                        hid: 'twitter:description',
                        content: description,
                    },
                    {
                        property: 'og:title',
                        name: 'og:title',
                        hid: 'og:title',
                        template: (chunk: string) => (chunk ? `${chunk} | Hangar` : 'Hangar'),
                        content: title,
                    },
                    {
                        property: 'twitter:title',
                        name: 'twitter:title',
                        hid: 'twitter:title',
                        template: (chunk: string) => (chunk ? `${chunk} | Hangar` : 'Hangar'),
                        content: title,
                    },
                    {
                        property: 'og:url',
                        name: 'og:url',
                        hid: 'og:url',
                        content: canonical,
                    },
                    {
                        property: 'twitter:url',
                        name: 'twitter:url',
                        hid: 'twitter:url',
                        content: canonical,
                    },
                    {
                        property: 'og:image',
                        name: 'og:image',
                        hid: 'og:image',
                        content: image,
                    },
                ],
                script: [
                    {
                        type: 'application/ld+json',
                        json: {
                            '@context': 'https://schema.org',
                            '@type': 'BreadcrumbList',
                            itemListElement: this.generateBreadcrumbs(route),
                        },
                    },
                ] as any[],
            } as MetaInfo;

            if (context.app.i18n.locale === 'dum') {
                console.log('found crowdin language activated, lets inject the script');
                seo.script = seo.script ? seo.script : [];
                seo.script.push({
                    type: 'text/javascript',
                    innerHTML: "var _jipt = []; _jipt.push(['project', '0cbf58a3d76226e92659632533015495']); _jipt.push(['domain', 'hangar']);",
                });
                seo.script.push({
                    type: 'text/javascript',
                    src: 'https://cdn.crowdin.com/jipt/jipt.js',
                });
                seo.__dangerouslyDisableSanitizers = ['script'];
            }

            return seo;
        }

        generateBreadcrumbs(route: Route) {
            const arr = [];
            const split = route.fullPath.split('/');
            let curr = '';
            for (let i = 0; i < split.length; i++) {
                curr = curr + split[i] + '/';
                arr.push({
                    '@type': 'ListItem',
                    position: i,
                    name: this.guessTitle(split[i]),
                    item: this.baseUrl() + curr,
                });
            }

            return arr;
        }

        guessTitle(segment: string): string {
            if (segment === '/' || segment === '') {
                return 'Hangar';
            } else {
                return segment;
            }
        }

        baseUrl(): string {
            // todo get this from somewhere
            return 'https://hangar.benndorf.dev';
        }
    }

    return new Seo();
};

type seoType = ReturnType<typeof createUtil>;

declare module 'vue/types/vue' {
    interface Vue {
        $seo: seoType;
    }
}

declare module '@nuxt/types' {
    interface NuxtAppOptions {
        $seo: seoType;
    }

    interface Context {
        $seo: seoType;
    }
}

declare module 'vuex/types/index' {
    // eslint-disable-next-line no-unused-vars,@typescript-eslint/no-unused-vars
    interface Store<S> {
        $seo: seoType;
    }
}

export default (ctx: Context, inject: Inject) => {
    inject('seo', createUtil(ctx));
};
