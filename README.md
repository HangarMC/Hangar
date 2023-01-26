# Hangar - Paper's Plugin Repository

This is the repository for Hangar, a plugin repository used for Paper, Velocity, and Waterfall plugins and similar software.

Hangar is loosely based off of [Ore](https://github.com/SpongePowered/Ore), created by the Sponge project, but rebuilt from the ground up using the Spring Boot
Framework in Java for the backend and Nuxt (with UnoCSS) for the frontend. We would like to thank all Ore contributors. Without them, this project would never
have been possible.

On top of our production instance at https://hangar.papermc.io/, we have a staging instance for testing purposes: https://hangar.papermc.dev/.

## Contributing

The project consists of 4 main parts

* Frontend (Vue.js under Nuxt with UnoCSS)
* Backend (Spring Boot)
* Database (PostgreSQL)
* User Management ([HangarAuth]; optional, see below)

There are two different environments that can be developed in, one using a fake user (without [HangarAuth]), or with [HangarAuth]. Most of the time you won't
need to run Hangar with [HangarAuth] unless you are working with a feature that requires multiple user interactions.

## Fake User environment (recommended, easy, no [HangarAuth])

Fork the project and pull it in your IDE.

### Prerequisites

* Docker is required in order to run the PostgreSQL database
* Java 17 or higher
* [pnpm]
* git

### Setting up

To get the project running locally, you need to follow a few steps:

1. Run `git submodule update --init` to initialize the [HangarLib](https://github.com/HangarMC/HangarLib) submodule. If you want to commit code to the lib
   repository (found in `frontend/src/lib`) without cloning the repo separately, you also need to checkout a branch
   using `cd frontend/src/lib && git switch master`.
2. To get the dummy database up and running, move to the docker folder `cd docker` then run `docker-compose -f dev-db.yml up -d` (`-d` as an optional parameter
   to run the containers in the background).
   Alternatively, if you are using IntelliJ you can press the green arrow in the `docker/dev-db.yml` file.
3. Run the Spring Boot application. You can do this in the CLI with `mvn spring-boot:run`, or if you're using IntelliJ, it is included in the run
   configurations.
4. Move to the `frontend` directory: `cd ../frontend`. In that directory, run `pnpm install`. This will install all the needed Node modules.
5. After the installation, run `pnpm run dev` in the frontend directory to initiate the build and launch. Changes you do to the frontend will be reloaded
   automatically.
6. After that, open http://localhost:3333, and if all went well, Hangar should be up and running.

### Notes

* The Spring Boot configuration file that is used by this environment is located at `Hangar/src/main/resources/application.yml`.
* The fake user settings are located in the application.yml file under `fake-user`.
* Without HangarAuth, you should also disable sso under `use-sso`.

## Hangar Auth (not recommended, complicated)

Fork this project and fork/clone [HangarAuth]. Ensure they are sibling directories in your file system.

```
Projects/
   Hangar/
      ...
   HangarAuth/
      ...
```

### Prerequisites

* Follow the installation instructions from [the previous section](#fake-user-environment--recommended-easy-no-hangarauth-) and skip its notes (the fake-user
  and use-sso settings)

### Setting up

To get both Hangar and HangarAuth running locally:

1. Setup HangarAuth
    1. See [HangarAuth README](https://github.com/HangarMC/HangarAuth/blob/master/README.md)
    2. Start HangarAuth's Docker services
    3. Create HangarAuth's Hydra client
2. Move to Hangar's frontend directory `Hangar/frontend`. In that directory, run `pnpm install` followed by `pnpm run dev`.
3. Set up the Hangar client in Hydra (see [HangarAuth README](https://github.com/HangarMC/HangarAuth/blob/master/README.md))
4. Navigate to http://localhost:3333 and login.

### Notes

* If using IntelliJ, you can view logs from each service in the Services tab (`ALT+8`).
* The Spring Boot configuration file that is used by this environment is located at `Hangar/docker/hangar/application.yml`.
* This setup requires the Hangar and [HangarAuth] projects to be sibling directories.
* To rebuild changes to Hangar, just rebuild in IntelliJ via `CTRL+F9`.
* To rebuild HangarAuth, run `docker-compose up -d --build` in `Hangar/docker`.

## Deployment

Deployment happens via Docker, see the stack in the Docker folder. The Spring Boot configuration file used for deployment can be found at
`docker/deployment/hangar-backend/application.yml`.

## Translations [![Crowdin](https://badges.crowdin.net/e/b13e6a1c05002365ee9031712112bd63/localized.svg)](https://hangar.crowdin.com/hangar)

Hangar uses Crowdin for translations. If you want to contribute to translations, create a Crowdin account at https://hangar.crowdin.com/hangar and simply add
new translations or comment/vote on existing translations. You can learn more about navigating the Crowdin UI here: https://support.crowdin.com/online-editor/.

#### Getting translations locally (mostly for developers, requires Crowdin CLI, ran in root folder):

`crowdin pull --skip-untranslated-strings -b master -T <PAT>`

You might want to set the env var `TRANSLATION_MODE` to true in order to get warnings about untranslated strings.

## Contributing

Most of our current and future plans can be found in the [**Hangar Roadmap Project**](https://github.com/PaperMC/Hangar/projects/1). Your best bet is joining
the #development channel on the [Hangar Discord](https://discord.gg/zvrAEbvJ4a) to start discussing potential contributions and ideas. With Hanagr being such a
massive project, any contributions are welcome!

Updating the frontend dependencies can be done best by running `npx npm-check -u` and going through the changelogs. Note that package.json might contain hints
to which dependencies are broken.

## Tracing

If you want to have traces available locally, you can run zipkin via Docker like this:

```shell
docker run -d -p 9411:9411 openzipkin/zipkin
```

Then just enable it in the `application.yml` under `management.tracing`.

## License

Hangar is licensed under the permissive [MIT License](LICENSE).

A large part of the backend is based on [Ore](https://github.com/SpongePowered/Ore/), also licensed under MIT:

```
Copyright (c) SpongePowered <https://www.spongepowered.org>
Copyright (c) contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
```

[pnpm]: https://pnpm.io/installation

[HangarAuth]: https://github.com/HangarMC/HangarAuth
