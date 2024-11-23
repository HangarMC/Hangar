import { helpers } from "@vuelidate/validators";

export const validProjectName = withOverrideMessage(() =>
  helpers.withParams(
    { type: "validProjectName" },
    helpers.withAsync(async (value: string) => {
      if (!helpers.req(value)) {
        return { $valid: true };
      }
      try {
        await useInternalApi("projects/validateName", "get", {
          value,
        });
        return { $valid: true };
      } catch (err: any) {
        return err.response?.data.isHangarApiException ? { $valid: false, $message: err.response?.data.message } : { $valid: false };
      }
    })
  )
);

export const validOrgName = withOverrideMessage(
  helpers.withAsync(async (value: string) => {
    if (!helpers.req(value)) {
      return { $valid: true };
    }

    if (value.includes(" ")) {
      return { $valid: false, $message: "organization.new.error.noSpaces" };
    }

    try {
      await useInternalApi("organizations/validate", "get", {
        name: value,
      });
      return { $valid: true };
    } catch {
      return { $valid: false, $message: "organization.new.error.duplicateName" };
    }
  })
);

export const validApiKeyName = withOverrideMessage((username: string) =>
  helpers.withParams(
    { username, type: "validApiKeyName" },
    helpers.withAsync(async (value: string) => {
      if (!helpers.req(value)) {
        return { $valid: true };
      }
      try {
        await useInternalApi(`api-keys/check-key/${username}`, "get", {
          name: value,
        });
        return { $valid: true };
      } catch (err: any) {
        return err.response?.data.isHangarApiException ? { $valid: false, $message: err.response?.data.message } : { $valid: false };
      }
    })
  )
);

export const validChannelName = withOverrideMessage((projectId: string, existingName?: string) =>
  helpers.withParams(
    { projectId, type: "validChannelName" },
    helpers.withAsync(async (value: string) => {
      if (!helpers.req(value)) {
        return { $valid: true };
      }
      try {
        await useInternalApi("channels/checkName", "get", {
          projectId,
          name: value,
          existingName,
        });
        return { $valid: true };
      } catch (err: any) {
        return err.response?.data.isHangarApiException ? { $valid: false, $message: err.response?.data.message } : { $valid: false };
      }
    })
  )
);

export const validChannelColor = withOverrideMessage((projectId: string, existingColor?: string) =>
  helpers.withParams(
    { projectId, type: "validChannelColor" },
    helpers.withAsync(async (value: string) => {
      if (!helpers.req(value)) {
        return { $valid: true };
      }
      try {
        await useInternalApi("channels/checkColor", "get", {
          projectId,
          color: value,
          existingColor,
        });
        return { $valid: true };
      } catch (err: any) {
        return err.response?.data.isHangarApiException ? { $valid: false, $message: err.response?.data.message } : { $valid: false };
      }
    })
  )
);
