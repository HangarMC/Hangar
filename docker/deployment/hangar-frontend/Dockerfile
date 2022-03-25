FROM node:16-alpine

WORKDIR hangar-frontend
ENV TERM xterm-256color

EXPOSE 1337
ENTRYPOINT ["./entrypoint.sh"]

COPY /docker/deployment/hangar-frontend/entrypoint.sh /hangar-frontend/entrypoint.sh
RUN chmod +x /hangar-frontend/entrypoint.sh

COPY /frontend-new/package.json /hangar-frontend/package.json
COPY /frontend-new/proxy.config.ts /hangar-frontend/proxy.config.ts
COPY /frontend-new/pnpm-lock.yaml /hangar-frontend/pnpm-lock.yaml
COPY /frontend-new/dist/ /hangar-frontend/dist/
COPY /frontend-new/node_modules/ /hangar-frontend/node_modules/
COPY /frontend-new/server/package.json /hangar-frontend/server/package.json
COPY /frontend-new/server/pnpm-lock.yaml /hangar-frontend/server/pnpm-lock.yaml
COPY /frontend-new/server/dist/ /hangar-frontend/server/dist/
COPY /frontend-new/server/node_modules/ /hangar-frontend/server/node_modules/
