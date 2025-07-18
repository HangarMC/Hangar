import fs from "node:fs";
import type { LocaleObject } from "@nuxtjs/i18n";

const localeFolder = "./app/i18n/locales/";
const processedFolder = localeFolder + "processed/";

export function loadLocales() {
  const locales: LocaleObject[] = [];
  for (const file of fs.readdirSync(localeFolder)) {
    if (!file.endsWith(".json")) continue;
    // eslint-disable-next-line unicorn/prefer-module,@typescript-eslint/no-require-imports
    const locale = require("./locales/" + file.replace(".json", ""));
    removeEmptyStrings(locale);
    if (!fs.existsSync(processedFolder)) fs.mkdirSync(processedFolder, {});
    fs.writeFileSync(processedFolder + file, JSON.stringify(locale));
    if (file === "dum.json") {
      locales.push({
        code: "dum",
        file,
        name: "In-Context Editor",
      });
    } else {
      if (!locale.meta.code || !locale.meta.name) {
        console.log("Invalid language file " + file + ", skipping...");
        continue;
      }
      locales.push({
        code: locale.meta.code,
        file,
        name: locale.meta.name,
      });
    }
  }
  return locales;
}

function removeEmptyStrings(obj: Record<string, string | object>) {
  for (const [key, value] of Object.entries(obj)) {
    if (value === "") {
      delete obj[key];
    } else if (typeof value === "object" && value !== null) {
      removeEmptyStrings(value as Record<string, string | object>);
    }
  }
}
