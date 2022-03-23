Stuff that needs to be done before I consider this a successful POC

## general stuff

- [x] initial setup (pages, layout, autoimport, basically the vitesse stuff)
- [x] windi/tailwind
- [x] store (with ssr)
- [x] async data replacement
- [x] i18n
- [x] api calls
- [x] seo
- [x] route perms
- [x] error pages (needs design but what doesnt?)
- [ ] snackbar
- [ ] maybe deployment alongside the existing frontend? (server is working now)
- [ ] figure out why vite isn't serving the manifest
- [x] cors?
- [ ] i18n + ssr, server needs local for both logged in and anon user
- [x] date formatting needs to go thru i18n
- [x] investigate why eslint/prettier don't auto fix
- [x] actually implement page transitions (as opposed to popping up below the page)
- [ ] validation of forms/inputs etc

## Big list of pages!

All pages need to be check that they

1. fetch all data
2. implement layout to display all data and click all buttons etc
3. functionality (including posting to server if needed)
4. have the design done
5. passed QA by somebody else other than the implementor

once QA has passed, the checkboxes can be removed and the page can be ~~striked out~~

- api
  - [x] fetch
  - [x] layout
  - [ ] functionality (auth is missing)
  - [ ] design
  - [ ] qa
- authors
  - [x] fetch
  - [x] layout
  - [ ] functionality (sorting is missing)
  - [x] design
  - [ ] qa
- error
  - [ ] fetch
  - [ ] layout
  - [ ] functionality
  - [ ] design
  - [ ] qa
- index
  - [x] fetch
  - [x] layout
  - [ ] functionality (search, sorting)
  - [ ] design
  - [ ] qa
- linkout
  - [x] fetch
  - [x] layout
  - [x] functionality
  - [ ] design
  - [ ] qa
- logged-out
  - [x] fetch (doesn't apply)
  - [ ] layout
  - [x] functionality (nothing here)
  - [ ] design
  - [ ] qa
- new
  - [ ] fetch
  - [ ] layout
  - [ ] functionality
  - [ ] design
  - [ ] qa
- notifications
  - [x] fetch
  - [x] layout
  - [x] functionality (cors error)
  - [ ] design
  - [ ] qa
- staff
  - [x] fetch
  - [x] layout
  - [ ] functionality (sorting is missing)
  - [x] design
  - [ ] qa
- organizations (empty)
  - new
    - [ ] fetch
    - [ ] layout
    - [ ] functionality
    - [ ] design
    - [ ] qa
- tools (empty)
  - bbcode
    - [x] fetch
    - [x] layout
    - [ ] functionality (pending CORS)
    - [ ] design
    - [ ] qa
- [user]
  - [ ] fetch
  - [ ] layout
  - [ ] functionality
  - [ ] design
  - [ ] qa
  * index
    - [ ] fetch
    - [ ] layout
    - [ ] functionality
    - [ ] design
    - [ ] qa
  * settings (empty)
    - api-keys
      - [x] fetch
      - [x] layout
      - [ ] functionality (loading animations, validation)
      - [x] design
      - [ ] qa
  * [project]
    - [ ] fetch
    - [ ] layout
    - [ ] functionality
    - [ ] design
    - [ ] qa
    * channels
      - [ ] fetch
      - [ ] layout
      - [ ] functionality
      - [ ] design
      - [ ] qa
    * discuss
      - [ ] fetch
      - [ ] layout
      - [ ] functionality
      - [ ] design
      - [ ] qa
    * flags
      - [ ] fetch
      - [ ] layout
      - [ ] functionality
      - [ ] design
      - [ ] qa
    * index
      - [ ] fetch
      - [ ] layout
      - [ ] functionality
      - [ ] design
      - [ ] qa
    * notes
      - [ ] fetch
      - [ ] layout
      - [ ] functionality
      - [ ] design
      - [ ] qa
    * settings
      - [ ] fetch
      - [ ] layout
      - [ ] functionality
      - [ ] design
      - [ ] qa
    * stars
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
      - [ ] qa
    * watchers
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
      - [ ] qa
    * pages (empty)
      - [...all]
        - [ ] fetch
        - [ ] layout
        - [ ] functionality
        - [ ] design
        - [ ] qa
    * versions (empty)
      - index
        - [ ] fetch
        - [ ] layout
        - [ ] functionality
        - [ ] design
        - [ ] qa
      - new
        - [ ] fetch
        - [ ] layout
        - [ ] functionality
        - [ ] design
        - [ ] qa
      - [version]
        - [ ] fetch
        - [ ] layout
        - [ ] functionality
        - [ ] design
        - [ ] qa
        * [platform] (empty)
          - index
            - [ ] fetch
            - [ ] layout
            - [ ] functionality
            - [ ] design
            - [ ] qa
          - reviews
            - [ ] fetch
            - [ ] layout
            - [ ] functionality
            - [ ] design
            - [ ] qa
- admin (empty)
  - flags
    - [x] fetch
    - [x] layout
    - [x] functionality
    - [x] design
    - [ ] qa
  - health
    - [x] fetch
    - [x] layout
    - [x] functionality
    - [x] design
    - [ ] qa
  - log
    - [x] fetch
    - [x] layout
    - [ ] functionality (pagination)
    - [x] design
    - [ ] qa
  - stats
    - [x] fetch
    - [x] layout
    - [x] functionality
    - [x] design
    - [ ] qa
  - versions
    - [x] fetch
    - [x] layout
    - [x] functionality (cors error)
    - [x] design
    - [ ] qa
  - activities (empty)
    - [user]
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
      - [ ] qa
  - approval (empty)
    - projects
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
      - [ ] qa
    - versions
      - [ ] fetch
      - [ ] layout
      - [ ] functionality
      - [ ] design
      - [ ] qa
  - user (empty)
    - [user]
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
      - [ ] qa
