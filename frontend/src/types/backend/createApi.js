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
    string: {
      "date-time": "Date",
    },
  }),
})
  .then(({ files, configuration }) => {
    for (const { fileName, fileExtension, fileContent } of files) {
      const prefix = "// noinspection JSValidateJSDoc,JSUnusedGlobalSymbols\n\n";
      fs.writeFileSync(fileName + fileExtension, prefix + fileContent);
    }
  })
  .catch((e) => console.error(e));
