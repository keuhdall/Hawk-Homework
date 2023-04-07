# Hawk-Homework
Homework for Hawk interview

## Specifications

The API should:
- Allow us to add movies to the database
- Allow us to query movies and filter by genre
- Allow us to know the amount of movies in the database and filter by year of production

## Starting the project

- Make sure you have installed [JDK >= 11](https://www.oracle.com/java/technologies/downloads/), [sbt](https://www.scala-sbt.org/) as well as [docker](https://docs.docker.com/engine/install/)
- Open the sbt prompt from the project directory using `sbt` and run `Docker / publishLocal`
- Run the stack using `docker-compose up`

## Calling the different routes

3 routes are available:
- POST /movie
> **NOTE**: here is an example payload:
> ```
> {
>     "title": "test",
>     "country": "USA",
>     "year": 2003,
>     "original_title": "2002 a space odyssey",
>     "french_release": "2012-12-12",
>     "synopsis": "un super film",
>     "genres": ["SFR", "Drama"],
>     "ranking": 9
> }
> ```
- GET /movies
> **NOTE**: query parameter to filter on genre can be set like go: `/movie?genre=drama`
- GET /count
> **NOTE**: query parameter to filter on year can be set like go: `/count?year=2001`

## Running the tests

Simply run `sbt test`

## Possible improvements

Quite a few things could be improved in this codebase.
First, no tests for the routes have been provided to cut corners, also,
repository tests should have been put under the `/it`
as they are very slow since they are relying on
[testcontainers](https://github.com/testcontainers/testcontainers-scala/tree/master), but I had some issues doing so.

Also, the requirements have not been fully met: the `Country` opaque type doesn't check
if its value actually comply to `ISO 3166-1 alpha-3`, rather, it checks for related properties
like string size, uppercase... It could have been easily done either reimplementing the ADT myself,
which would have been simple but time-consuming as at holds 247 different values,
or using [scala-iso](https://github.com/vitorsvieira/scala-iso),
but I wasn't feeling comfortable using a library that seemed to be unmaintained
(and probably not Scala3-compatible anyway).

Finally, I chose to use [opaque types](https://dotty.epfl.ch/docs/reference/other-new-features/opaques.html)
to refine types as discussed [here](https://contributors.scala-lang.org/t/poor-or-rich-mans-refinement-types-in-scala-3-x/4647),
but this ended up being a pretty bad idea in my opinion, as it made type derivation much more [tricky](https://github.com/circe/circe/issues/1829),
both for Circe and Doobie (as seen in `Country.scala:26` for example). A solution could have been using
simple value-classes or something like [monix-newtypes](https://newtypes.monix.io/).