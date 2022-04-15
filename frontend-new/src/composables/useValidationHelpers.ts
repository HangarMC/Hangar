import { ErrorObject, useVuelidate, ValidationRule } from "@vuelidate/core";
import { computed, Ref } from "vue";
import * as validators from "@vuelidate/validators";
import { createI18nMessage, helpers, ValidatorWrapper } from "@vuelidate/validators";
import { I18n } from "~/i18n";
import { useInternalApi } from "~/composables/useApi";

export function isErrorObject(errorObject: string | ErrorObject): errorObject is ErrorObject {
  return (<ErrorObject>errorObject).$message !== undefined;
}

export function constructValidators<T>(rules: ValidationRule<T | undefined>[] | undefined, name: string) {
  // no clue why this cast is needed
  return rules ? { [name]: rules } : { [name]: {} };
}

export function useValidation<T>(
  name: string | undefined,
  rules: ValidationRule<T | undefined>[] | undefined,
  state: Ref,
  errorMessages?: string[] | undefined
) {
  const n = name || "val";
  const v = useVuelidate(constructValidators(rules, n), { [n]: state });
  const errors = computed(() => {
    const e = [];
    if (errorMessages) {
      e.push(...errorMessages);
    }
    if (v.value.$errors) {
      e.push(...v.value.$errors);
    }
    return e;
  });
  const hasError = computed(() => (errorMessages && errorMessages.length) || v.value.$error);

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
      console.log("translate", msg, overrideMsg, params);
      return I18n.value.global.t(msg, params);
    },
  })(validator, { withArguments: true });
};

function withOverrideMessage<T extends ValidationRule | ValidatorWrapper>(rule: T) {
  return (overrideMsg?: string) => {
    return withI18nMessage(rule, overrideMsg);
  };
}

// basic
export const required = withOverrideMessage(validators.required);
export const requiredIf = withOverrideMessage(validators.requiredIf);
export const minLength = withOverrideMessage(validators.minLength);
export const maxLength = withOverrideMessage(validators.maxLength);
export const url = withOverrideMessage(validators.url);

// custom
export const validName = withOverrideMessage(
  (ownerId: string) =>
    helpers.withParams(
      { ownerId, type: "validName" },
      helpers.withAsync(async (value: string) => {
        if (!helpers.req(value)) {
          return { $valid: true };
        }
        try {
          await useInternalApi("projects/validateName", false, "get", {
            userId: ownerId,
            value: value,
          });
          return { $valid: true };
        } catch (e) {
          return !e.response?.data.isHangarApiException ? { $valid: false } : { $valid: false, $message: e.response?.data.message };
        }
      })
    ) as ValidationRule<{ ownerId: string }>
);

export const pattern = withOverrideMessage(
  (regex: string) =>
    helpers.withParams({ regex, type: "pattern" }, (value: string) => {
      if (!helpers.req(value)) {
        return { $valid: true };
      }
      return { $valid: new RegExp(regex).test(value) };
    }) as ValidationRule<{ regex: string }>
);
