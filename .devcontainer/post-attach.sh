#!/bin/bash

# Workaround required for GitHub Codespaces.
# Preflight requests are not handled correctly if the port is Private.
# @see https://github.com/orgs/community/discussions/15351

PORTS=(3333 24678);

if [ -n "$CODESPACES" ]; then
    # Required to prevent race condition
    sleep 3

    for PORT in ${PORTS[@]}
    do
        echo "Port $PORT is now public as an workaround to an Codespaces issue."
        gh codespace ports visibility $PORT:public -c $CODESPACE_NAME
    done
fi
