## Contributing to Hangar

Take a look at the [README building section](README.md#Building) for info on how to set up the project.\
A general list of todos can be found in the [Roadmap Project](https://github.com/PaperMC/Hangar/projects/1).

## General Policy
Don't make the code look like it comes from a one-time contributor,
but embed it sanely into the surrounding code.
Changes should be easy to maintain and understand (with telling names and comments),
and when in doubt, ask other contributors if your idea makes sense/nobody else is working on it already. 

## Formatting
Nothing crazy here:
* Use 4 spaces (not tabs) as indention
* Leave an empty line between class headers and their first field
* Always use braces for single line ifs, for loops, etc.

Other than that, we follow [Oracle's standard code conventions](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html) (e.g. IntelliJ's auto formatting).

## Frontend
* we use [windi.css](https://windicss.org/). that means we try to avoid writing css as much as possible. there are edge cases where it might be needed, sometimes they can be solved by adding stuff to the windi config, sometimes not, that can be handled on a case by case basis.
* all colors should come from our palette, defined in the windi config. no generic colors and no hex classes, please.
* always test both the dark and the white theme
* make sure ssr works, load the page with a breakpoint in main and it should still render. watch out for hydration warnings in the console.
* try to divide the code into multiple components where applicable, to avoid giant template blocks. similarly, use composables to split out code where applicable.
