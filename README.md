# Hangar - Paper's upcoming Plugin Repository

This is the repository for Hangar, a plugin repository used for paper plugins and similar pieces of software.

Hangar is a "fork" of [Ore](https://github.com/SpongePowered/Ore), created by the Sponge project, 
but rebuild from the ground up using the Spring Boot Framework in Java for the backend, and nuxt (and vuetify) for the frontend (which is partially server rendered).
We would like the thank all Ore contributors, without them, this project would never have been possible.

There may or may not be a staging instance running at https://hangar-new.minidigger.me or https://hangar.minidigger.me
It may or may not allow you to log in, please don't create too much of a mess so that I don't always need to nuke the DB when I want to use it.

## Building

The project uses maven and should be straight forward (we even include a wrapper for you, wow!)  
The frontend is located in `frontend` and uses webpack. You will need yarn to build that. Initially install dependencies via `yarn`, then start the dev environment using `yarn run dev`.

Additionally, you will need a postgresql database. For your convenience, this repo contains a dev-db docker compose stack in the docker folder that will get you setup.

This project currently uses java 11.

### Building with Docker
If you want to build both Hangar and [HangarAuth](https://github.com/MiniDigger/HangarAuth)
to run together (without a fakeuser), clone both this repo, and [this](https://github.com/PaperMC/HangarAuth).
Make sure both Hangar and HangarAuth directories are siblings in your file system. cd into Hangar/docker and run `docker-compose up -d`. That should set everything up for you. You can view the logs via Intellij's docker integration.
I find its better to view the logs there, so that the Hangar logs and HangarAuth logs are separated. 
Note that when using docker, a different spring configuration file is used, `Hangar/docker/hangar/application.yml`. To reload changes to Hangar, just CTRL+F9 (rebuild) in Intellij. To rebuild changes to HangarAuth, just run `docker-compose up -d --build` 
and that will rebuild if there were any changes.

## Deployment

Deployment happens via Docker, checkout the stack in the docker folder. You will want to modify the application.properties in the hangar folder.

## Contributing

There is a bunch of stuff to do, some of that is noted in the [**Roadmap Project**](https://github.com/PaperMC/Hangar/projects/1). 
Your best bet is joining #hangar-dev on the [paper discord](https://discord.gg/papermc) and just discussing with us.
All contributions are very welcome, we will not be able to finish this alone!

## Licence

Most of the frontend is a fork of Ore, licensed under MIT [here](https://github.com/SpongePowered/Ore/blob/staging/LICENSE.txt). 
The rest is new code (but created in reference of Ore) and is licenced under the MIT licence too.
