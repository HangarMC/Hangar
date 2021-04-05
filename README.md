# Hangar - Papers upcoming Plugin Repository

This is the repository for Hangar, a plugin repository used for Paper plugins and similar software.

Hangar is a "fork" of [Ore](https://github.com/SpongePowered/Ore), created by the Sponge project, 
but rebuilt from the ground up using the Spring Boot Framework in Java for the backend and nuxt (and vuetify) for the frontend (which is partially server rendered).
We would like the thank all Ore contributors. Without them, this project would never have been possible.

There may or may not be a staging instance running at https://hangar-new.minidigger.me or https://hangar.minidigger.me
It may or may not allow you to log in, please don't create too much of a mess so that I don't always need to nuke the DB when I want to use it.

## Contributing
## Hangar
The project consists out of 4 parts. The frontend (Nuxt and Vue), the backend (Spring Boot), the database (PostgreSQL) and an optional [HangarAuth] project.
Which will be discussed below.


### Fork the project
Fork the project and pull it in your IDE.
### Prerequisites
- Docker is required in order to run the PostgreSQL database.
- Java 11 or higher.
### Setting up
To get the project running locally you need to follow a few steps:
1. To get the dummy database up and running move to the docker folder `cd docker` then run `docker-compose -f dev-db.yml up -d` (`-d` as an optional parameter to run the containers in the background).
   Alternatively if you are using IntelliJ you can press the green arrow in the `docker/dev-db.yml` file.
2. Run the Spring Boot application. You can do it in the CLI with `mvn spring-boot:run` or if you're using IntelliJ, it's included in the run configurations.
3. Move to the `frontend` directory: `cd ../frontend`. In that directory, run `yarn install`. This will install all the needed Node modules.
5. After the installation, run `yarn run dev` in the frontend directory to initiate the build and launch. Changes you do to the frontend will be reloaded automatically.
6. After that browse to http://localhost:3000 and if all went well, Hangar should be up and running.

### Hangar Auth

#### Building with Docker
To get directly into it, you can enable a fake user (see the `fake-user` application setting), otherwise you have to build both Hangar and HangarAuth to run together.
Make sure both Hangar and HangarAuth directories are siblings in your file system. cd into Hangar/docker and run `docker-compose up -d`. That should set everything up for you. You can view the logs via IntelliJ's docker integration.
I find it's better to view the logs there, so that the Hangar logs and HangarAuth logs are separated.
Note that when using Docker, a different Spring configuration file is used, `Hangar/docker/hangar/application.yml`. To reload changes to Hangar, just CTRL+F9 (rebuild) in IntelliJ. To rebuild changes to HangarAuth, just run `docker-compose up -d --build`
and that will rebuild if there were any changes.

## Deployment

Deployment happens via Docker, checkout the stack in the docker folder. You will want to modify the application.properties in the hangar folder.

## Contributing

There is a bunch of stuff to do, some of that is noted in the [**Roadmap Project**](https://github.com/PaperMC/Hangar/projects/1). 
Your best bet is joining #hangar-dev on the [Paper Discord](https://discord.gg/papermc) and just discussing with us.
All contributions are very welcome, we will not be able to finish this alone!

## Licence

Most of the frontend is a fork of Ore, licensed under MIT [here](https://github.com/SpongePowered/Ore/blob/staging/LICENSE.txt). 
The rest is new code (but created in reference of Ore) and is licenced under the MIT licence too.

[HangarAuth]: https://github.com/PaperMC/HangarAuth
