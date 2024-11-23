/// <reference types='codeceptjs' />
type util = typeof import('./utils/util');
type IndexPage = typeof import('./utils/IndexPage');

declare namespace CodeceptJS {
  interface SupportObject { I: I, current: any, util: util, IndexPage: IndexPage }
  interface Methods extends WebDriver, ExpectHelper {}
  interface I extends WithTranslation<Methods> {}
  namespace Translation {
    interface Actions {}
  }
}
