#!/bin/bash

# Sets up an bare-bones Hangar environment to make developing in Codespaces quicker and easier.

# Create Docker containers (db & email)
cd docker && docker-compose -f dev.yml up --no-start

# Install PNPM dependencies (frontend)
cd ../frontend && pnpm i --silent

# Install Maven dependencies (backend)
cd ../backend && exec mvn dependency:go-offline -q
