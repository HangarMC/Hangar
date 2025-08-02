FROM oven/bun:1.2.19

USER bun
WORKDIR /hangar-frontend
ENV TERM=xterm-256color
ENV HOST=0.0.0.0

EXPOSE 1337
ENV PORT=1337
ENV NODE_ENV=production
ENTRYPOINT ["./entrypoint.sh"]

COPY --chown=bun:bun --chmod=744 /chart/dockerfiles/frontend/entrypoint.sh /hangar-frontend/entrypoint.sh
COPY --chown=bun:bun /frontend/.output/ /hangar-frontend/
