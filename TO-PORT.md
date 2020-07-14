## Templates to port
### -
* [ ] createOrganization
* [ ] swagger

### Projects
* [ ] admin\flags
* [ ] admin\notes
* [ ] channels\helper\modalManage
* [ ] channels\helper\popoverColorPicker
* [ ] channels\list
* [ ] helper\alertFile
* [ ] helper\btnHide
* [ ] helper\createSteps
* [ ] helper\inputSettings
* [ ] helper\modalDownload
* [ ] pages\modalPageCreate
* [ ] pages\view
* [ ] versions\create
* [ ] versions\list
* [ ] versions\log
* [ ] versions\unsafeDownload
* [ ] versions\view
* [ ] create
* [ ] discuss
* [ ] settings
* [ ] tag
* [ ] userGrid
* [ ] view

### Users
* [ ] admin\activity
* [ ] admin\flags
* [ ] admin\health
* [ ] admin\log
* [ ] admin\queue
* [ ] admin\reviews
* [ ] admin\stats
* [ ] admin\userAdmin
* [ ] admin\visibility
* [ ] invite\form
* [ ] invite\roleSelect
* [ ] invite\userSearch
* [ ] apiKeys
* [ ] authors
* [ ] memberList
* [ ] notifications
* [ ] projects
* [ ] view

### Utils
* [ ] editor
* [ ] email
* [ ] modal
* [ ] prompt
* [ ] userAvatar

---
Notes to self (or anyone if they want to jump at it)
* Search for `.isDefined`/`.get`/`.orElse` optional usage (left some behind, `x.orElse(y)` -> `x!y`)
* `prettifyDateAndTime(x)` -> `x?string.long`