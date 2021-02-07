FROM node:12-alpine

WORKDIR hangar-frontend
ENV TERM xterm-256color

EXPOSE 3000
ENTRYPOINT ["./entrypoint.sh"]

COPY /frontend/package.json /hangar-frontend/package.json
COPY /docker/deployment/hangar-frontend/entrypoint.sh /hangar-frontend/entrypoint.sh
COPY /frontend/nuxt.config.ts /hangar-frontend/nuxt.config.ts
COPY /frontend/node_modules/ /hangar-frontend/node_modules/
COPY /frontend/.nuxt/ /hangar-frontend/.nuxt/
