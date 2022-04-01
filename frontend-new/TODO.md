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
- [x] snackbar/success notifications/whatever
- [x] maybe deployment alongside the existing frontend?
- [ ] figure out why vite isn't serving the manifest
- [x] cors?
- [ ] i18n + ssr, server needs local for both logged in and anon user
- [x] date formatting needs to go thru i18n
- [x] investigate why eslint/prettier don't auto fix
- [x] actually implement page transitions (as opposed to popping up below the page)
- [ ] validation of forms/inputs etc
- [ ] check that we have loading states everywhere, on like buttons and whatever
- [x] add header calls to all pages

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
  - [x] functionality
  - [x] design
  - [ ] qa
- authors
  - [x] fetch
  - [x] layout
  - [ ] functionality (sorting is missing)
  - [x] design
  - [ ] qa
- error
  - [x] fetch
  - [x] layout
  - [x] functionality
  - [ ] design
  - [ ] qa
- index
  - [x] fetch
  - [x] layout
  - [ ] functionality (sorting, licence and version filter)
  - [x] design
  - [ ] qa
- linkout
  - [x] fetch
  - [x] layout
  - [x] functionality
  - [x] design
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
  - [x] functionality
  - [x] design
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
    - [x] functionality
    - [ ] design (ugly text area)
    - [ ] qa
- [user] (was filled in old but kinda empty in new)
  - [x] fetch
  - [x] layout
  - [x] functionality
  - [x] design
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
    - [x] fetch
    - [x] layout
    - [x] functionality
    - [ ] design (nav might need some work)
    - [ ] qa
    * channels
      - [x] fetch
      - [x] layout
      - [ ] functionality (modal is missing)
      - [x] design
      - [ ] qa
    * discuss
      - [ ] fetch
      - [ ] layout
      - [ ] functionality
      - [ ] design
      - [ ] qa
    * flags
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
      - [ ] qa
    * index
      - [ ] fetch
      - [ ] layout
      - [ ] functionality
      - [ ] design
      - [ ] qa
    * notes
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
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
        - [x] fetch
        - [x] layout
        - [ ] functionality (page editing, page creation, page deleting)
        - [x] design
        - [ ] qa
    * versions (empty)
      - index
        - [x] fetch
        - [x] layout
        - [ ] functionality (channel editing, pagination)
        - [ ] design (don't like the version li yet)
        - [ ] qa
      - new
        - [ ] fetch
        - [ ] layout
        - [ ] functionality
        - [ ] design
        - [ ] qa
      - [version]
        - [x] fetch
        - [ ] layout (do we wanna do tabs again?)
        - [x] functionality
        - [ ] design
        - [ ] qa
        * [platform] (empty)
          - index
            - [x] fetch
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
    - [x] functionality
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
      - [x] fetch
      - [x] layout
      - [ ] functionality (expansion missing)
      - [x] design
      - [ ] qa
  - user (empty)
    - [user]
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
      - [ ] qa
