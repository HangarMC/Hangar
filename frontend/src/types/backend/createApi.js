const path = require("path");
const fs = require("fs");
const { generateApi } = require("swagger-typescript-api");

generateApi({
  name: "index.ts",
  input: path.resolve(process.cwd(), "api.json"),
  createClient: true,
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
        const newName = rawTypeName.substring(rawTypeName.lastIndexOf(".") + 1);
        if (newName.includes("$")) {
          return prefix + newName.substring(newName.lastIndexOf("$") + 1);
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
  value: string;
  frontendName: string;
  permission: string;
}`,
        `export interface PermissionData {
  value: string;
  frontendName: string;
  permission: bigint;
}`
      );

      // patch platform data
      fileContent = fileContent.replace(
        `export interface PlatformData {
  name: string;
  category: Category;
  url: string;
  enumName: string;
  visible: boolean;
  platformVersions: PlatformVersion[];
}`,
        `export interface PlatformData {
  name: string;
  category: Category;
  url: string;
  enumName: Platform;
  visible: boolean;
  platformVersions: PlatformVersion[];
}`
      );

      fs.writeFileSync(fileName + fileExtension, prefix + fileContent);
    }
  })
  .catch((e) => console.error(e));
