// to prevent unnecessary profile loads
export default defineEventHandler((event) => {
  setResponseStatus(event, 404);
  return `Unknown language: ${event.context?.params?._?.replace("prism-", "")?.replace(".min.js", "")}`;
});
