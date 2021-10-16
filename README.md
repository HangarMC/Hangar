# Hangar - Papers upcoming Plugin Repository

This is the repository for Hangar, a plugin repository used for Paper plugins and similar software.

Hangar is a "fork" of [Ore](https://github.com/SpongePowered/Ore), created by the Sponge project, 
but rebuilt from the ground up using the Spring Boot Framework in Java for the backend and nuxt (and vuetify) for the frontend (which is partially server rendered).
We would like the thank all Ore contributors. Without them, this project would never have been possible.

There may or may not be a staging instance running at https://hangar.benndorf.dev
It may or may not allow you to log in, please don't create too much of a mess so that I don't always need to nuke the DB when I want to use it.

## Contributing
The project consists out of 4 parts
* Frontend (written in Vue with Nuxt)
* Backend (Spring Boot)
* Database (PostgreSQL)
* User Management ([HangarAuth]) (optional). *see below*

There are two different environments that can be developed in, one using a fake user (aka without [HangarAuth]), or with [HangarAuth].
Most of the time you won't need to run Hangar with [HangarAuth] unless you are working with a feature that requires multiple user interactions.

## Fake User environment (recommended, no [HangarAuth])

Fork the project and pull it in your IDE.
### Prerequisites

* Docker is required in order to run the PostgreSQL database.
* Java 11 or higher.
* [Yarn]
### Setting up
To get the project running locally you need to follow a few steps:
1. To get the dummy database up and running move to the docker folder `cd docker` then run `docker-compose -f dev-db.yml up -d` (`-d` as an optional parameter to run the containers in the background).
   Alternatively if you are using IntelliJ you can press the green arrow in the `docker/dev-db.yml` file.
2. Run the Spring Boot application. You can do it in the CLI with `mvn spring-boot:run` or if you're using IntelliJ, it's included in the run configurations.
3. Move to the `frontend` directory: `cd ../frontend`. In that directory, run `yarn install`. This will install all the needed Node modules.
5. After the installation, run `yarn run dev` in the frontend directory to initiate the build and launch. Changes you do to the frontend will be reloaded automatically.
6. After that browse to http://localhost:3000 and if all went well, Hangar should be up and running.

### Notes
* The Spring Boot configuration file that is used by this environment is located at `Hangar/src/main/resources/application.yml`
* The fake user settings are located in the application.yml file under `fake-user`.

## Hangar Auth
Fork this project and fork/clone [HangarAuth]. Ensure they are sibling directories in your file system.
```
Projects/
   Hangar/
      ...
   HangarAuth/
      ...
```

### Prerequisites
* Docker is required for all parts of this environment except the frontend
* [Yarn]
### Setting up
To get both Hangar and HangarAuth running locally:
1. To start all the docker services move into Hangar's docker folder `cd Hangar/docker` the run `docker-compose up -d`. 
   If you are using IntelliJ, you can also add `Hangar/docker/docker-compose.yml` as a Run Configuration and use that to start up the services.
2. Move to Hangar's frontend directory `Hangar/frontend`. In that directory run `yarn install` followed by `yarn dev`.
3. Now you need to create a super user in HangarAuth.
   1. In `Hangar/docker` run `docker-compose run auth /env/bin/python spongeauth/manage.py createsuperuser`.
   2. Fill out the prompts. Note that the email service is not setup yet, so it really doesn't matter what email you enter when prompted.
   3. Navigate to http://localhost:8000 and sign in with the credentials you just entered. Agree to the TOS.
4. Now that the super user is created, you must create an Api Key so Hangar and HangarAuth can communicate with verification. Click the Admin button on the top right of the page
   1. Navigate to Api keys under the Api key table name.
   2. Click `ADD API KEY +` in the top right and add `changeme` as the api key.
5. Navigate to http://localhost:3000 and login. 


### Notes
* If using IntelliJ, you can view logs from each service in the Services tab (ALT+8).
* The Spring Boot configuration file that is used by this environment is located at `Hangar/docker/hangar/application.yml`
* This setup requires that the Hangar and [HangarAuth] projects be sibling directories.
* To rebuild changes to Hangar, just rebuild in IntelliJ `CTRL+F9`.
* To rebuild HangarAuth, run `docker-compose up -d --build` in `Hangar/docker`.

## Deployment

Deployment happens via Docker, checkout the stack in the docker folder. The Spring Boot configuration file used for deployment can be found at
`docker/deployment/hangar-backend/application.yml`.

## Contributing

There is a bunch of stuff to do, some of that is noted in the [**Roadmap Project**](https://github.com/PaperMC/Hangar/projects/1). 
Your best bet is joining #development on the [Hangar Discord](https://discord.gg/zvrAEbvJ4a) and just discussing with us.
All contributions are very welcome, we will not be able to finish this alone!

## License

Most of the frontend is a fork of Ore, licensed under MIT [here](https://github.com/SpongePowered/Ore/blob/staging/LICENSE.txt). 
The rest is new code (but created in reference of Ore) and is licensed under the MIT license too.

[Yarn]: https://yarnpkg.com/
[HangarAuth]: https://github.com/PaperMC/HangarAuth
