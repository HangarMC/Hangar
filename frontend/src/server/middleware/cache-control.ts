export default defineEventHandler((event) => {
  const statics = 31536000; // 1 year
  const other = 60 * 60; // 1 hour
  const url = event.node.req.url;
  if (url?.startsWith("/api")) return;
  const maxage = url?.match(/(.+)\.(jpg|jpeg|webp|gif|css|png|js|ico|svg|mjs)/) ? statics : other;
  event.node.res.setHeader("Cache-Control", `max-age=${maxage} s-maxage=${maxage}`);
});
