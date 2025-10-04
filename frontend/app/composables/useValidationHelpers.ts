import { useVuelidate } from "@vuelidate/core";
import type { ErrorObject, ValidationRule } from "@vuelidate/core";
import type { Ref } from "vue";
import * as validators from "@vuelidate/validators";
import { createI18nMessage, helpers } from "@vuelidate/validators";
import type { ValidatorWrapper } from "@vuelidate/validators";
import { difference, isEmpty, uniq } from "lodash-es";
import { AxiosError } from "axios";

export function isErrorObject(errorObject?: string | ErrorObject): errorObject is ErrorObject {
  return typeof errorObject === "object" && "$message" in errorObject;
}

export function constructValidators<T>(rules: ValidationRule<T | undefined>[] | undefined, name: string) {
  return rules ? { [name]: rules } : { [name]: {} };
}

export function useValidation<T, V = any>(
  name: string | undefined,
  rules: ValidationRule<T | undefined>[] | undefined,
  state: Ref<V>,
  errorMessages?: Ref<string[] | undefined>,
  silentErrors = false,
  autoDirty = false
) {
  const n = name || "val";
  const v = useVuelidate(constructValidators(rules, n), { [n]: state }, { $autoDirty: autoDirty });
  const errors = computed(() => {
    const e = [];
    if (errorMessages?.value) {
      e.push(...errorMessages.value);
    }
    if (silentErrors) {
      if (v.value.$silentErrors) {
        e.push(...v.value.$silentErrors);
      }
    } else if (v.value.$errors) {
      e.push(...v.value.$errors);
    }
    return e;
  });
  const hasError = computed(() => (errorMessages?.value && errorMessages.value.length > 0) || v.value.$error);

  return { v, errors, hasError };
}

export const withI18nMessage = <T extends ValidationRule | ValidatorWrapper>(validator: T, overrideMsg?: string): T => {
  return createI18nMessage({
    t: (_, params) => {
      let msg = "validation." + params.type;
      const response = params.response;
      if (response && response.$message) {
        msg = response.$message;
      }
      if (overrideMsg) {
        msg = overrideMsg;
      }
      return useNuxtApp().$i18n.t(msg, params);
    },
  })(validator, { withArguments: true });
};

export function withOverrideMessage<T extends ValidationRule | ValidatorWrapper>(rule: T) {
  return (overrideMsg?: string) => {
    return withI18nMessage(rule, overrideMsg);
  };
}

// basic
export const required = withOverrideMessage(validators.required);
export const requiredIf = withOverrideMessage(validators.requiredIf);
export const requiredUnless = withOverrideMessage(validators.requiredUnless);
export const minLength = withOverrideMessage(validators.minLength);
export const maxLength = withOverrideMessage(validators.maxLength);
export const integer = withOverrideMessage(validators.integer);
export const validUrl = withOverrideMessage(validators.url);
export const email = withOverrideMessage(validators.email);
export const sameAs = withOverrideMessage(validators.sameAs);

// custom
export const pattern = withOverrideMessage((regex: string) =>
  helpers.withParams({ regex, type: "pattern" }, (value: string) => {
    if (!helpers.req(value)) {
      return { $valid: true };
    }
    return { $valid: new RegExp(regex).test(value) };
  })
);

export const maxFileSize = withOverrideMessage((maxSize: number) =>
  helpers.withParams({ maxSize, type: "maxFileSize" }, (file: File) => {
    return { $valid: !file || file.size <= maxSize };
  })
);

export const noDuplicated = withOverrideMessage((elements: any[] | (() => any[] | undefined) | undefined) =>
  helpers.withParams({ elements, type: "noDuplicated" }, () => {
    const els = typeof elements === "function" ? elements() : unref(elements);
    if (!els) {
      return { $valid: true };
    }
    return { $valid: new Set(els).size === els.length };
  })
);

export const validPageName = withOverrideMessage((body: { projectId: number; parentId?: number; name: string }) =>
  helpers.withParams(
    { body, type: "validPageName" },
    helpers.withAsync(async () => {
      try {
        await useInternalApi("pages/checkName", "get", body);
        return { $valid: true };
      } catch (err: AxiosError | any) {
        if (err?.response?.data?.detail) {
          return { $valid: false, $message: err.response.data.detail };
        } else if (err?.response?.data?.message) {
          return { $valid: false, $message: err.response.data.message };
        } else {
          return { $valid: false };
        }
      }
    }, body)
  )
);

export function isSame(arrayOne?: any[], arrayTwo?: any[]) {
  const a = uniq(arrayOne);
  const b = uniq(arrayTwo);
  return a.length === b.length && isEmpty(difference(b.toSorted(), a.toSorted()));
}
