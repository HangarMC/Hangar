import { AxiosError } from "axios";
import { HangarApiException, HangarValidationException } from "hangar-api";
import { Composer, VueMessageType } from "vue-i18n";
import { Context } from "vite-ssr/vue";

export function handleRequestError(
  err: AxiosError,
  { writeResponse }: Context,
  i18n: Composer<unknown, unknown, unknown, VueMessageType>,
  msg: string | undefined = undefined
) {
  if (import.meta.env.SSR) {
    _handleRequestError(err, writeResponse, i18n);
    return;
  }
  if (!err.isAxiosError) {
    // everything should be an AxiosError
    console.log(err);
  } else if (err.response) {
    // TODO snackbar
    // if (err.response.data.isHangarApiException) {
    //   for (const errorMsg of collectErrors(err.response.data, i18n)) {
    //     store.dispatch('snackbar/SHOW_NOTIF', {
    //       message: msg ? `${i18n.t(msg)}: ${errorMsg}` : errorMsg,
    //       color: 'error',
    //       timeout: 3000,
    //     } as NotifPayload);
    //   }
    // } else if (err.response.data.isHangarValidationException) {
    //   const data: HangarValidationException = err.response.data;
    //   for (const fieldError of data.fieldErrors) {
    //     store.dispatch('snackbar/SHOW_NOTIF', {
    //       message: fieldError.errorMsg,
    //       color: 'error',
    //       timeout: 3000,
    //     } as NotifPayload);
    //   }
    //   if (msg) {
    //     store.dispatch('snackbar/SHOW_NOTIF', {
    //       message: t(msg),
    //       color: 'error',
    //       timeout: 3000,
    //     } as NotifPayload);
    //   }
    // } else {
    //   store.dispatch('snackbar/SHOW_NOTIF', {
    //     message: msg ? `${t(msg)}: ${err.response.statusText}` : err.response.statusText,
    //     color: 'error',
    //     timeout: 2000,
    //   } as NotifPayload);
    // }
    console.log("request error", err.response);
  } else {
    console.log(err);
  }
}

function _handleRequestError(err: AxiosError, writeResponse: Context["writeResponse"], i18n: Composer<unknown, unknown, unknown, VueMessageType>) {
  if (!err.isAxiosError) {
    // everything should be an AxiosError
    writeResponse({
      status: 500,
    });
    console.log(err);
  } else if (err.response) {
    if (err.response.data.isHangarApiException) {
      const data: HangarApiException = err.response.data.isMultiException ? err.response.data.exceptions[0] : err.response.data;
      writeResponse({
        status: data.httpError.statusCode,
        statusText: i18n.te(data.message) ? i18n.t(data.message) : data.message,
      });
    } else if (err.response.data.isHangarValidationException) {
      const data: HangarValidationException = err.response.data;
      writeResponse({
        status: data.httpError.statusCode,
        statusText: data.fieldErrors.map((f) => f.errorMsg).join(", "),
      });
    } else {
      writeResponse({
        status: err.response.status,
        statusText: err.response.statusText,
      });
    }
  } else {
    writeResponse({
      statusText: "This shouldn't happen...",
    });
    console.log(err);
  }
}

// function collectErrors(exception: HangarApiException | MultiHangarApiException, i18n: Context["app"]["i18n"]): TranslateResult[] {
//   if (!exception.isMultiException) {
//     return [i18n.te(exception.message) ? i18n.t(exception.message, [exception.messageArgs]) : exception.message];
//   } else {
//     const res: TranslateResult[] = [];
//     for (const ex of exception.exceptions) {
//       res.push(i18n.te(ex.message) ? i18n.t(ex.message, ex.messageArgs) : ex.message);
//     }
//     return res;
//   }
// }
