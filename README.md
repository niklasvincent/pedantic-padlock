[![Build Status](https://travis-ci.org/nlindblad/pedantic-padlock.svg)](https://travis-ci.org/nlindblad/pedantic-padlock)

# Pedantic Padlock

SSL status badges powered by Qualys SSL Labs

A small microservice for generating SVG badges for your SSL terminating end-points.

![Example of badges](https://raw.githubusercontent.com/nlindblad/pedantic-padlock/master/doc/badge-example.png)

## How is the grading done?

1. Is the current JVM running Pedantic Padlock capable of connecting without certificate warnings?

2. Is the expiration date more than 14 days into the future?

3. Use result from Qualys SSL Labs scan.

## Deploy to Heroku

Change the Heroku application name in `build.sbt`:

	herokuAppName in Compile := "pacific-meadow-xxxx"

Deploy using SBT:

	sbt stage deployHeroku

Make sure the application started correctly:

	heroku logs --app pacific-meadow-xxxx

## Future Work

- [ ] Improve SSL Labs background processing
- [ ] Tune actor systems
- [ ] Deploy to Heroku button (if possible)
- [ ] Add dashboard as root page