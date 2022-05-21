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
- [x] i18n + ssr, server needs locale for both logged in and anon user
- [x] date formatting needs to go thru i18n
- [x] investigate why eslint/prettier don't auto fix
- [x] actually implement page transitions (as opposed to popping up below the page)
- [ ] validation of forms/inputs etc (mostly done)
- [ ] check that we have loading states everywhere, on like buttons and whatever (mostly done)
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
  - [x] functionality
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
  - [ ] functionality (search without reloading the page, sorting, licence and version filter)
  - [x] design
  - [ ] qa
- linkout
  - [x] fetch
  - [x] layout
  - [x] functionality
  - [x] design
  - [x] qa
- new
  - [x] fetch
  - [x] layout
  - [x] functionality
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
  - [x] functionality
  - [x] design
  - [ ] qa
- organizations (empty)
  - new
    - [x] fetch
    - [x] layout
    - [ ] functionality (member editing doesn't work)
    - [x] design
    - [ ] qa
- tools (empty)
  - bbcode
    - [x] fetch
    - [x] layout
    - [x] functionality
    - [x] design
    - [ ] qa
- [user] (was filled in old but kinda empty in new)
  - [x] fetch
  - [x] layout
  - [x] functionality
  - [x] design
  - [ ] qa
  * index
    - [x] fetch
    - [x] layout
    - [x] functionality
    - [x] design
    - [ ] qa
  * settings (empty)
    - api-keys
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
      - [ ] qa
  * [project]
    - [x] fetch
    - [x] layout
    - [x] functionality
    - [x] design
    - [ ] qa
    * channels
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
      - [ ] qa
    * discuss (lets skip this for now)
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
      - [x] fetch
      - [x] layout
      - [ ] functionality (promoted versions, download latest)
      - [x] design
      - [ ] qa
    * notes
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
      - [ ] qa
    * settings
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [ ] design (needs styling the tabs)
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
        - [x] functionality
        - [x] design
        - [ ] qa
    * versions (empty)
      - index
        - [x] fetch
        - [x] layout
        - [ ] functionality (pagination)
        - [ ] design (don't like the version li yet)
        - [ ] qa
      - new
        - [ ] fetch
        - [ ] layout (rough draft)
        - [ ] functionality
        - [ ] design
        - [ ] qa
      - [version]
        - [x] fetch
        - [x] layout
        - [x] functionality
        - [x] design
        - [ ] qa
        * [platform] (empty)
          - index
            - [x] fetch
            - [x] layout
            - [ ] functionality (misses deleting, admin actions and other buttons)
            - [ ] design
            - [ ] qa
          - reviews
            - [x] fetch
            - [x] layout
            - [x] functionality
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
      - [x] functionality
      - [x] design
      - [ ] qa
  - user (empty)
    - [user]
      - [x] fetch
      - [x] layout
      - [x] functionality
      - [x] design
      - [ ] qa
