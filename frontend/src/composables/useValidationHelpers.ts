import { type ErrorObject, useVuelidate, type ValidationRule } from "@vuelidate/core";
import { computed } from "vue";
import type { ComputedRef, type Ref } from "vue";
import * as validators from "@vuelidate/validators";
import { createI18nMessage, helpers, type ValidatorWrapper } from "@vuelidate/validators";
import { difference, isEmpty, uniq } from "lodash-es";
import { AxiosError } from "axios";
import { I18n } from "~/i18n";
import { unref, useInternalApi } from "#imports";

export function isErrorObject(errorObject: string | ErrorObject): errorObject is ErrorObject {
  return typeof errorObject === "object" && "$message" in errorObject;
}

export function constructValidators<T>(rules: ValidationRule<T | undefined>[] | undefined, name: string) {
  return rules ? { [name]: rules } : { [name]: {} };
}

export function useValidation<T, V = any>(
  name: string | undefined,
  rules: ValidationRule<T | undefined>[] | undefined,
  state: Ref<V>,
  errorMessages: Ref<string[] | undefined> | undefined,
  silentErrors = false
) {
  const n = name || "val";
  const v = useVuelidate(constructValidators(rules, n), { [n]: state });
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
      return I18n.value.t(msg, params);
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
export const url = withOverrideMessage(validators.url);
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

export const noDuplicated = withOverrideMessage((elements: any[] | (() => any[])) =>
  helpers.withParams({ elements, type: "noDuplicated" }, () => {
    const els = typeof elements === "function" ? elements() : unref(elements);
    return { $valid: new Set(els).size === els.length };
  })
);

export const validPageName = withOverrideMessage((body: ComputedRef<{ projectId: number; parentId?: number; name: string }>) =>
  helpers.withParams(
    { body, type: "validPageName" },
    helpers.withAsync(async () => {
      try {
        await useInternalApi("pages/checkName", "get", body.value);
        return { $valid: true };
      } catch (e: AxiosError | any) {
        return e?.response?.data?.detail ? { $valid: false, $message: e.response.data.detail } : { $valid: false };
      }
    }, body)
  )
);

export function isSame(arrayOne?: any[], arrayTwo?: any[]) {
  const a = uniq(arrayOne);
  const b = uniq(arrayTwo);
  return a.length === b.length && isEmpty(difference(b.sort(), a.sort()));
}
