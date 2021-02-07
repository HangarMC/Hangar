FROM node:12-alpine

WORKDIR hangar-frontend
ENV TERM xterm-256color

EXPOSE 3000
ENTRYPOINT ["./entrypoint.sh"]

COPY package.json /hangar-frontend/package.json
COPY ../docker/deployment/hangar-frontend/entrypoint.sh /hangar-frontend/entrypoint.sh
COPY nuxt.config.ts /hangar-frontend/nuxt.config.ts
COPY node_modules/ /hangar-frontend/node_modules/
COPY .nuxt/ /hangar-frontend/.nuxt/

