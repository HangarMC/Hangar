/// <reference types='codeceptjs' />
type steps_file = typeof import('./utils/steps');
type IndexPage = typeof import('./pages/IndexPage');

declare namespace CodeceptJS {
  interface SupportObject { I: I, current: any, IndexPage: IndexPage }
  interface Methods extends WebDriver {}
  interface I extends ReturnType<steps_file> {}
  namespace Translation {
    interface Actions {}
  }
}
