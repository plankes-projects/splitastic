# Splitastic

Splitastic is an open source self hosted chores/expenses splitting app, an alternative to those ad cluttered apps like Flatastic, HomeSlice or FlatMate.

The app can be used to track expenses and chores of multiple groups. In a household or vacation. It is actively used and improved by the repository owner.

Mobile phones are first class citizens in this app. This means you should use this app on a mobile phone for the best experience.

See it in action here: https://splitastic.epicnerf.com/

## Why another chores/expenses splitting app you ask?
There is no free (no ads), open source or self hosted alternative to all those apps out there.
Splitastic is here to change that!

## Getting Started
The project consists of 2 parts:
* Java Spring server which connects to a MySQL database (needs to be MySQL because of raw queries). See server readme for more information.
* TypeScript Vue PWA client. See client readme for more information.

##### Quick start guide for self hosting
https://github.com/plankes-projects/splitastic/wiki/Quick-start-guide-for-self-hosting

##### Quick start guide for development
https://github.com/plankes-projects/splitastic/wiki/Quick-start-guide-for-development

## Version convention
MAJOR.MINOR.PATCH - Each of the parts is incremented according to:

* MAJOR, when a change is NOT backwards-compatible (an upgrade to a new MAJOR version will break stuff)
* MINOR, when new functionality is added in a backwards-compatible manner
* PATCH, when bug fixes are made in a backward-compatible manner
 
## Contribute
2 ways to contribute:
* Coding: https://github.com/plankes-projects/splitastic/wiki/Quick-start-guide-for-development
* Donate: https://epicnerf.com/donate/
