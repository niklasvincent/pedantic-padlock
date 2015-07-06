# spray-heroku-template

## Deploy to Heroku

Change the Heroku application name in `build.sbt`:

	herokuAppName in Compile := "pacific-meadow-xxxx"

Deploy using SBT:

	sbt stage deployHeroku

Make sure the application started correctly:

	heroku logs --app pacific-meadow-xxxx