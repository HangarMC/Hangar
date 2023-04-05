import fs from "fs";
import path from "node:path";
import { getFileInfo, check, resolveConfig, format } from "prettier";
import qs from "qs";
import { type Plugin } from "vite";
import { createFilter } from "@rollup/pluginutils";

export default function prettier(): Plugin {
  const defaults = {
    include: ["src/**/*.js", "src/**/*.ts", "src/**/*.vue", "src/**/*.html", "src/**/*.css", "src/**/*.json"],
  };

  const filter = createFilter(defaults.include, /node_modules/);

  function normalize(id: string): string {
    return path.relative(process.cwd(), id).split(path.sep).join("/");
  }

  function checkVueFile(id: string): boolean {
    if (!id.includes("?")) return false;

    const rawQuery = id.split("?", 2)[1];

    return qs.parse(rawQuery).vue !== null;
  }

  return {
    name: "vite:prettier",
    transform(_, id) {
      const file = normalize(id);
      const fileInfo = getFileInfo.sync(id);
      if (!checkVueFile(id) && !fileInfo.ignored && filter(id)) {
        const config = resolveConfig.sync(file);
        if (config) {
          if (!("parser" in config) && fileInfo.inferredParser) {
            config.parser = fileInfo.inferredParser;
          }
          const source = fs.readFileSync(file, "utf-8");
          if (source && !check(source, config)) {
            this.warn(`${file} has formatting errors, fixing...`);
            fs.writeFile(file, format(source, config), (err) => {
              if (err) {
                this.error(err);
              }
            });
          }
        }
      }
      return null;
    },
  };
}
