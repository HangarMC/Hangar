import type { AxiosError } from "axios";
import { isAxiosError } from "axios";
import type { Composer } from "vue-i18n";
import { tryUseNuxtApp } from "#app/nuxt";
import { isError as isH3Error } from "h3";
import type { H3Error } from "h3";
import type { HangarApiException, HangarValidationException, MultiHangarApiException } from "#shared/types/backend";

export function handleRequestError(err: AxiosError | unknown, msg?: string | undefined, alwaysShowErrorPage = false) {
  const i18n = tryUseNuxtApp()?.$i18n;
  if (!i18n) {
    console.error("didnt find i18n!");
    _handleRequestError(err, i18n);
    return;
  }
  if (import.meta.server || alwaysShowErrorPage) {
    _handleRequestError(err, i18n);
    return;
  }
  const notification = useNotificationStore();
  const transformed = transformAxiosError(err);
  if (!isAxiosError(err)) {
    // everything should be an AxiosError
    fetchLog("no axios request error", transformed);
    notification.error(transformed.message?.toString() || "Unknown error");
  } else if (err.response && typeof err.response.data === "object" && err.response.data) {
    if ("isHangarApiException" in err.response.data) {
      for (const errorMsg of collectErrors(err.response.data as HangarApiException, i18n)) {
        console.log("dum", err.response.data, errorMsg, msg);
        notification.error(msg ? `${i18n.t(msg)}: ${errorMsg}` : errorMsg);
      }
    } else if ("isHangarValidationException" in err.response.data) {
      const data = err.response.data as HangarValidationException;
      if (data.fieldErrors) {
        for (const fieldError of data.fieldErrors) {
          if (!fieldError.errorMsg) continue;
          notification.error(
            i18n.te(fieldError.errorMsg)
              ? i18n.t(fieldError.errorMsg, [fieldError.fieldName, fieldError.rejectedValue])
              : fieldError.errorMsg + " '" + fieldError.fieldName + "': " + fieldError.rejectedValue
          );
        }
      }
      if (msg) {
        notification.error(i18n.t(msg));
      }
    } else {
      if (!msg) {
        // todo: This shouldn't happen
        if (err.response?.data?.detail) {
          msg = i18n.t(err.response.data.detail);
        }
        if (err.response?.data?.message) {
          msg = i18n.t(err.response.data.message);
        }
      }

      notification.error(msg ? `${err.response.statusText}: ${i18n.t(msg)}` : err.response.statusText);
    }
    fetchLog("request error", transformed);
  } else {
    fetchLog("unknown error", transformed);
    notification.error(transformed.message?.toString() || "Unknown error");
  }
}

function _handleRequestError(err: AxiosError | H3Error | unknown, i18n?: Composer) {
  const transformed = transformAxiosError(err);
  if (!isAxiosError(err) && !isH3Error(err)) {
    // everything should be an AxiosError or h3 error
    fetchLog("handle not axios error", transformed, err);
    throw createError({
      statusCode: 500,
    });
  } else if ("response" in err && typeof err.response?.data === "object" && err.response.data) {
    _handleErrorResponse(err.response.data, i18n);
  } else if ((err.cause as any)?.response?.data) {
    _handleErrorResponse((err.cause as any).response.data, i18n);
  } else if ((err.cause as any)?.statusCode) {
    // this error was rethrown, lets inform nuxt
    showError(err.cause as any);
  } else {
    throw createError({
      statusCode: 500,
      statusMessage: "Internal Error: " + transformed.code,
      cause: err,
    });
  }
}

function _handleErrorResponse(responseData: object, i18n?: Composer) {
  if ("isHangarApiException" in responseData) {
    const data = "isMultiException" in responseData ? (responseData as MultiHangarApiException).exceptions?.[0] : (responseData as HangarApiException);
    throw createError({
      statusCode: (data as HangarValidationException | undefined)?.httpError?.statusCode,
      statusMessage: data?.message ? (i18n?.te(data.message) ? i18n.t(data.message) : data.message) : undefined,
    });
  } else if ("isHangarValidationException" in responseData) {
    const data = responseData as HangarValidationException;
    throw createError({
      statusCode: data.httpError?.statusCode,
      statusMessage: data.fieldErrors?.map((f) => f.errorMsg).join(", "),
    });
  } else {
    throw createError({
      statusCode: ("status" in responseData ? (responseData?.status as number) : undefined) || 500,
      statusMessage: ("statusText" in responseData ? (responseData?.statusText as string) : undefined) || "Internal Server Error",
    });
  }
}

function collectErrors(exception: HangarApiException | MultiHangarApiException, i18n: Composer): string[] {
  if (!("isMultiException" in exception) || !exception.isMultiException) {
    return exception.message
      ? [i18n.te(exception.message) ? i18n.t(exception.message, [...(exception as { messageArgs: string }).messageArgs]) : exception.message]
      : [];
  } else {
    const res: string[] = [];
    const exceptions = (exception as MultiHangarApiException).exceptions;
    if (!exceptions) return [];
    for (const ex of exceptions) {
      if (!ex.message) {
        res.push("Unknown error");
        continue;
      }
      res.push(i18n.te(ex.message) ? i18n.t(ex.message, (ex as { messageArgs: string }).messageArgs) : ex.message);
    }
    return res;
  }
}

export function transformAxiosError(err: AxiosError | unknown): Record<string, unknown> {
  return isAxiosError(err)
    ? {
        code: err?.code,
        requestUrl: err?.request?.path || err?.config?.url,
        status: err?.response?.status,
        data: err?.response?.data,
        message: err?.message,
      }
    : (err as Record<string, unknown>);
}
