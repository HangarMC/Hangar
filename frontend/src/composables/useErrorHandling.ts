import axios, { AxiosError } from "axios";
import { HangarApiException, HangarValidationException, MultiHangarApiException } from "hangar-api";
import { Composer } from "vue-i18n";
import { useNotificationStore } from "~/lib/store/notification";
import { I18n } from "~/lib/i18n";
import { createError } from "#imports";
import { fetchLog } from "~/lib/composables/useLog";

export function handleRequestError(err: AxiosError | unknown, msg: string | undefined = undefined, alwaysShowErrorPage = false) {
  const i18n: Composer = I18n.value;
  if (import.meta.env.SSR || alwaysShowErrorPage) {
    _handleRequestError(err, i18n);
  }
  const notification = useNotificationStore();
  const transformed = transformAxiosError(err);
  if (!axios.isAxiosError(err)) {
    // everything should be an AxiosError
    fetchLog("no axios request error", transformed);
    notification.error(transformed.message?.toString() || "Unknown error");
  } else if (err.response && typeof err.response.data === "object" && err.response.data) {
    if ("isHangarApiException" in err.response.data) {
      for (const errorMsg of collectErrors(err.response.data as HangarApiException, i18n)) {
        notification.error(msg ? `${i18n.t(msg)}: ${errorMsg}` : errorMsg);
      }
    } else if ("isHangarValidationException" in err.response.data) {
      const data = err.response.data as HangarValidationException;
      for (const fieldError of data.fieldErrors) {
        notification.error(i18n.te(fieldError.errorMsg) ? i18n.t(fieldError.errorMsg) : fieldError.errorMsg);
      }
      if (msg) {
        notification.error(i18n.t(msg));
      }
    } else {
      notification.error(msg ? `${i18n.t(msg)}: ${err.response.statusText}` : err.response.statusText);
    }
    fetchLog("request error", transformed);
  } else {
    fetchLog("unknown error", transformed);
    notification.error(transformed.message?.toString() || "Unknown error");
  }
}

function _handleRequestError(err: AxiosError | unknown, i18n: Composer) {
  const transformed = transformAxiosError(err);
  if (!axios.isAxiosError(err)) {
    // everything should be an AxiosError
    createError({
      statusCode: 500,
    });
    fetchLog("handle not axios error", transformed);
  } else if (err.response && typeof err.response.data === "object" && err.response.data) {
    if ("isHangarApiException" in err.response.data) {
      const data =
        "isMultiException" in err.response.data ? (err.response.data as MultiHangarApiException).exceptions[0] : (err.response.data as HangarApiException);
      createError({
        statusCode: data.httpError.statusCode,
        statusMessage: data.message ? (i18n.te(data.message) ? i18n.t(data.message) : data.message) : null,
      });
    } else if ("isHangarValidationException" in err.response.data) {
      const data = err.response.data as HangarValidationException;
      createError({
        statusCode: data.httpError.statusCode,
        statusMessage: data.fieldErrors.map((f) => f.errorMsg).join(", "),
      });
    } else {
      createError({
        statusCode: err.response.status,
        statusMessage: err.response.statusText,
      });
    }
  } else {
    createError({
      statusCode: 500,
      statusMessage: "Internal Error: " + transformed.code,
    });
  }
}

function collectErrors(exception: HangarApiException | MultiHangarApiException, i18n: Composer): string[] {
  if (!exception.isMultiException) {
    return exception.message ? [i18n.te(exception.message) ? i18n.t(exception.message, [exception.messageArgs]) : exception.message] : [];
  } else {
    const res: string[] = [];
    for (const ex of exception.exceptions) {
      res.push(i18n.te(ex.message) ? i18n.t(ex.message, ex.messageArgs) : ex.message);
    }
    return res;
  }
}

export function transformAxiosError(err: AxiosError | unknown): Record<string, unknown> {
  return axios.isAxiosError(err)
    ? {
        code: err?.code,
        requestUrl: err?.request?.path || err?.config?.url,
        status: err?.response?.status,
        data: err?.response?.data,
        message: err?.message,
      }
    : (err as Record<string, unknown>);
}
