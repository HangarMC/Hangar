FROM node:12-alpine

WORKDIR hangar-frontend
ENV TERM xterm-256color

EXPOSE 3000
ENTRYPOINT ["ls", "-ahl", "&&", "ls", "-ahl", "/hangar-frontend/node_modules/", "./node_modules/.bin/nuxt-ts", "run", "start"]

COPY package.json /hangar-frontend/package.json
COPY nuxt.config.ts /hangar-frontend/nuxt.config.ts
COPY node_modules/ /hangar-frontend/node_modules/
COPY .nuxt/ /hangar-frontend/.nuxt/

