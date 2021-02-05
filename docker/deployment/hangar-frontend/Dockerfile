FROM node:12-alpine

WORKDIR hangar-frontend

COPY .nuxt /hangar-frontend/
COPY node_modules /hangar-frontend/
COPY package.json /hangar-frontend/
COPY nuxt.config.ts /hangar-frontend/

ENV TERM xterm-256color

EXPOSE 3000
ENTRYPOINT ["nuxt-ts", "start"]
