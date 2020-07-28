# Hangar2 - Ore, but in spring!

This is the repository for Hangar, a plugin repository used for paper plugins and similar pieces of software.

Hangar is a "fork" of [Ore](https://github.com/SpongePowered/Ore), created by the Sponge project, 
but rebuild from the group up using the Spring Boot Framework in Java, using the Freemarker templating engine.  
We would like the thank all Ore contributors, without them, this project would never have been possible.

The frontend is a mixture of vue and jquery, which will be cleaned up eventually.

There may or may not be a staging instance running at https://hangar-new.minidigger.me. 
It may or may not allow you to log in, please don't create too much of a mess so that I don't always need to nuke the DB when I want to use it.

## Building

The project uses maven and should be straight forward (we even include a wrapper for you, wow!)  
The frontend is located in `src/main/frontend` and uses webpack. You will need yarn to build that. Initially install dependencies via `yarn`, then start the dev environment using `yarn run serve`.

Additionally, you will need a postgresql database. For your convenice, this repo contains a dev-db docker compose stack in the docker folder that will get you setup.

This project currently uses java 11.

## Deployment

Deployment happens via docker, checkout the stack in the docker folder. You will want to modify the application.properties in the hangar folder.

## Contributing

There is a bunch of stuff to do, some of that is noted in the [status](STATUS.md) and [to-port](TO-PORT.md) files.  
Your best bet is joining the [discord](https://s.minidigger.me/discord) and just discussing with us.
All contributions are very welcome, I will not be able to finish this alone!

## Licence

Most of the frontend is a fork of Ore, licensed under MIT [here](https://github.com/SpongePowered/Ore/blob/staging/LICENSE.txt). 
The rest is new code (but created in reference of Ore) and is licenced under the MIT licence too.
