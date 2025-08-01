const path = require("node:path");
const fs = require("node:fs");
const { generateApi } = require("swagger-typescript-api");

generateApi({
  name: "index.ts",
  input: path.resolve(process.cwd(), "api.json"),
  fileName: "index.ts",
  generateClient: false,
  hooks: {
    onFormatTypeName(typeName, rawTypeName, schemaType) {
      let prefix = "";
      if (rawTypeName.startsWith("io.papermc.hangar.model.api.PaginatedResult")) {
        prefix = "PaginatedResult";
      }
      if (rawTypeName.startsWith("io.papermc.hangar.model.internal.user.JoinableMember")) {
        prefix = "JoinableMember";
      }
      if (rawTypeName.includes(".")) {
        const newName = rawTypeName.slice(Math.max(0, rawTypeName.lastIndexOf(".") + 1));
        if (newName.includes("$")) {
          return prefix + newName.slice(Math.max(0, newName.lastIndexOf("$") + 1));
        }
        return prefix + newName;
      } else {
        return typeName;
      }
    },
  },
  primitiveTypeConstructs: (constructs) => ({
    ...constructs,
    object: "Record<string, any>",
    //string: {
    //  "date-time": "Date",
    //},
  }),
})
  .then(({ files, configuration }) => {
    for (let { fileName, fileExtension, fileContent } of files) {
      const prefix = "// noinspection JSValidateJSDoc,JSUnusedGlobalSymbols\n\n";

      // patch permission data
      fileContent = fileContent.replace(
        `export interface PermissionData {
  frontendName: string;
  permission: string;
  value: string;
}`,
        `export interface PermissionData {
  frontendName: string;
  permission: bigint;
  value: string;
}`
      );

      // patch platform data
      fileContent = fileContent.replace(
        `export interface PlatformData {
  category: Category;
  enumName: string;
  name: string;
  platformVersions: PlatformVersion[];
  url: string;
  visible: boolean;
}`,
        `export interface PlatformData {
  category: Category;
  enumName: Platform;
  name: string;
  platformVersions: PlatformVersion[];
  url: string;
  visible: boolean;
}`
      );

      fs.writeFileSync(fileName + fileExtension, prefix + fileContent);
    }
  })
  .catch((err) => console.error(err));
