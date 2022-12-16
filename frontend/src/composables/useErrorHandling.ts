import { AxiosError } from "axios";
import { HangarApiException, HangarValidationException, MultiHangarApiException } from "hangar-api";
import { useI18n } from "vue-i18n";
import { ref } from "vue";
import { useNotificationStore } from "~/lib/store/notification";

export function handleRequestError(err: AxiosError, i18n: ReturnType<typeof useI18n>, msg: string | undefined = undefined) {
  if (import.meta.env.SSR) {
    _handleRequestError(err, i18n);
    return ref();
  }
  const notfication = useNotificationStore();
  if (!err.isAxiosError) {
    // everything should be an AxiosError
    console.log(err);
  } else if (err.response && typeof err.response.data === "object" && err.response.data) {
    if ("isHangarApiException" in err.response.data) {
      for (const errorMsg of collectErrors(err.response.data as HangarApiException, i18n)) {
        notfication.error(msg ? `${i18n.t(msg)}: ${errorMsg}` : errorMsg);
      }
    } else if ("isHangarValidationException" in err.response.data) {
      const data = err.response.data as HangarValidationException;
      for (const fieldError of data.fieldErrors) {
        notfication.error(i18n.te(fieldError.errorMsg) ? i18n.t(fieldError.errorMsg) : fieldError.errorMsg);
      }
      if (msg) {
        notfication.error(i18n.t(msg));
      }
    } else {
      notfication.error(msg ? `${i18n.t(msg)}: ${err.response.statusText}` : err.response.statusText);
    }
    console.log("request error", err.response);
  } else {
    console.log(err);
  }
  return ref();
}

function _handleRequestError(err: AxiosError, i18n: ReturnType<typeof useI18n>) {
  function writeResponse(object: unknown) {
    console.log("writeResponse", object);
    // throw new Error("TODO: Implement me"); // TODO
  }
  if (!err.isAxiosError) {
    // everything should be an AxiosError
    writeResponse({
      status: 500,
    });
    console.log(err);
  } else if (err.response && typeof err.response.data === "object" && err.response.data) {
    if ("isHangarApiException" in err.response.data) {
      const data =
        "isMultiException" in err.response.data ? (err.response.data as MultiHangarApiException).exceptions[0] : (err.response.data as HangarApiException);
      writeResponse({
        status: data.httpError.statusCode,
        statusText: i18n.te(data.message) ? i18n.t(data.message) : data.message,
      });
    } else if ("isHangarValidationException" in err.response.data) {
      const data = err.response.data as HangarValidationException;
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

function collectErrors(exception: HangarApiException | MultiHangarApiException, i18n: ReturnType<typeof useI18n>): string[] {
  if (!exception.isMultiException) {
    return [i18n.te(exception.message) ? i18n.t(exception.message, [exception.messageArgs]) : exception.message];
  } else {
    const res: string[] = [];
    for (const ex of exception.exceptions) {
      res.push(i18n.te(ex.message) ? i18n.t(ex.message, ex.messageArgs) : ex.message);
    }
    return res;
  }
}
