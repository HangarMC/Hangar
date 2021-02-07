FROM node:12-alpine

WORKDIR hangar-frontend
ENV TERM xterm-256color

EXPOSE 3000
ENTRYPOINT ["./node_modules/.bin/nuxt-ts", "run", "start"]

COPY package.json /hangar-frontend/
COPY nuxt.config.ts /hangar-frontend/
COPY node_modules /hangar-frontend/
COPY .nuxt /hangar-frontend/

