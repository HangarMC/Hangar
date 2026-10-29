/// <reference types='codeceptjs' />
type util = typeof import('./utils/util').default;
type IndexPage = typeof import('./utils/IndexPage').default;
type steps_file = typeof import('./utils/custom_steps').default;

declare namespace CodeceptJS {
  interface SupportObject { I: I, current: any, util: util, IndexPage: IndexPage }
  interface Methods extends WebDriver {}
  interface I extends ReturnType<steps_file> {}
  namespace Translation {
    interface Actions {}
  }
}
