# Hangar - Paper's Plugin Repository
[![Crowdin](https://badges.crowdin.net/e/b13e6a1c05002365ee9031712112bd63/localized.svg)](https://hangar.crowdin.com/hangar)
[![Discord](https://img.shields.io/discord/855123416889163777?)](https://discord.gg/zvrAEbvJ4a)
![GitHub License](https://img.shields.io/github/license/hangarmc/hangar)
[![Swagger Validator](https://img.shields.io/swagger/valid/3.0?specUrl=https%3A%2F%2Fhangar.papermc.io%2Fv3%2Fapi-docs%2Fpublic)](https://hangar.papermc.io/api-docs)
[![BrowserStack Status](https://automate.browserstack.com/badge.svg?badge_key=OHFacEE0WmlHRldDajYrZFdsZUtDOFZBcmUyR1VWdWlUaStlQWJYS0xZVT0tLWRKODJVblZQblczRXMvejNQTGhEZ1E9PQ==--54e7c90dad3680579c945ff532d63909156aa024)](https://automate.browserstack.com/public-build/OHFacEE0WmlHRldDajYrZFdsZUtDOFZBcmUyR1VWdWlUaStlQWJYS0xZVT0tLWRKODJVblZQblczRXMvejNQTGhEZ1E9PQ==--54e7c90dad3680579c945ff532d63909156aa024)

This is the repository for Hangar, a plugin repository used for Paper, Velocity, and Waterfall plugins and similar software.

Hangar is loosely based off of [Ore](https://github.com/SpongePowered/Ore), created by the Sponge project, but rebuilt from the ground up using the Spring Boot
Framework in Java for the backend and Nuxt (with UnoCSS) for the frontend. We would like to thank all Ore contributors. Without them, this project would never
have been possible.

On top of our production instance at https://hangar.papermc.io/, we have a staging instance for testing purposes: https://hangar.papermc.dev/.

## Contributing

The project consists of 3 main parts

* Frontend (Vue.js under Nuxt with UnoCSS)
* Backend (Spring Boot)
* Database (PostgreSQL)

## Development Setup

Fork the project and pull it in your IDE.

### Prerequisites

* Docker is required in order to run the PostgreSQL database and the dummy email server
* Java 21 or higher
* [pnpm](https://pnpm.io/installation)
* mvn
* git
* we strongly recommend using IntelliJ IDEA (Ultimate) as your IDE

### Setting up

To get the project running locally, you need to follow a few steps:

#### Frontend only

1. Install the frontend deps:  
   **Using IntelliJ**: Right-click the package.json file in the frontend directory and click `Run 'pnpm install'` (or click the button in the `Update dependencies` toast)  
   **Manually**: Move to the frontend directory: `cd frontend` then run `pnpm install`.
2. Start the frontend.  
   **Using IntelliJ**: Run the `frontend without backend` run config (or click the green arrow in the gutter on this line)  
   **Manually**: Move to the frontend directory: `cd frontend` then run `pnpm run devStaging`.
3. After that, open http://localhost:3333, and if all went well, Hangar should be up and running.
4. You can open http://localhost:3333/onboarding and create an admin account, or once you logged in, generate fake data.

#### Frontend and Backend

1. Get the dummy database, storage and email server up and running.  
   **Using Intellij**: Run the `docker` run config (or click the green arrow in the gutter on this line)  
   **Manually**: Move to the docker folder `cd docker` then run `docker-compose -f dev.yml up -d` (`-d` as an optional parameter to run the containers in the background).
2. Start the backend.  
   **Using IntelliJ Ultimate**: Run the `backend` run config (or click the green arrow in the gutter on this line)  
   **Using IntelliJ Community**: Run the `backend-community` run config (or click the green arrow in the gutter on this line)  
   **Manually**: Move to the backend directory: `cd backend` then run `mvn spring-boot:run`. Alternatively you can start the `HangarApplication` class via your IDE.
3. Install the frontend deps:  
   **Using IntelliJ**: Right-click the package.json file in the frontend directory and click `Run 'pnpm install'` (or click the button in the `Update dependencies` toast)  
   **Manually**: Move to the frontend directory: `cd frontend` then run `pnpm install`.
4. Start the frontend.  
   **Using Intellij**: Run the `frontend` run config (or click the green arrow in the gutter on this line)  
   **Manually**: Move to the frontend directory: `cd frontend` then run `pnpm run dev`.
5. After that, open http://localhost:3333, and if all went well, Hangar should be up and running.
6. You can open http://localhost:3333/onboarding and create an admin account, or once you logged in, generate fake data.

### Notes

* The Spring Boot configuration file that is used by this environment is located at `backend/src/main/resources/application.yml`.
* You can view the emails hangar sends on http://localhost:4436/
* On staging/prod Hangar uses object storage, if you want to test that you can install minio and change the storage type to `object` in the `application.yml`

## Deployment

Deployment happens via a helm chart in the `/chart` folder. The Spring Boot configuration file used for deployment can be found at
`chart/templates/secret-hangar-backend.yaml` (however it will be overridden with the actual values via helm).

## Translations [![Crowdin](https://badges.crowdin.net/e/b13e6a1c05002365ee9031712112bd63/localized.svg)](https://hangar.crowdin.com/hangar)

Hangar uses Crowdin for translations. If you want to contribute to translations, create a Crowdin account at https://hangar.crowdin.com/hangar and simply add
new translations or comment/vote on existing translations. You can learn more about navigating the Crowdin UI here: https://support.crowdin.com/online-editor/.

#### Getting translations locally (mostly for developers, requires Crowdin CLI, ran in root folder):

`crowdin pull -b master -T <PAT>`

You might want to set the env var `TRANSLATION_MODE` to true in order to get warnings about untranslated strings.

## Contributing

Most of our current and future plans can be found in the [**Hangar Roadmap Project**](https://github.com/PaperMC/Hangar/projects/1). Your best bet is joining
the #development channel on the [Hangar Discord](https://discord.gg/zvrAEbvJ4a) to start discussing potential contributions and ideas. With Hangar being such a
massive project, any contributions are welcome!

Updating the frontend dependencies can be done best by running `npx npm-check -u` and going through the changelogs. Note that package.json might contain hints
to which dependencies are broken.

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
