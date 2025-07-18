The index.ts in this folder contains all necessary types for the backend communication, generated from an openapi spec.

To generate a new version of the types:

- first enabled fully qualified names in the backend (control + f "springdoc" -> comment in)
- then save http://localhost:8080/v3/api-docs/combined to `api.json`
- format with prettier
- run `node createApi.js` to generate the types
- format index.ts with prettier
