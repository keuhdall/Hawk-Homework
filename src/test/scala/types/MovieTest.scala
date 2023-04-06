package types

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.*
import io.circe.syntax.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.Gen
import types.Country.VALID_COUNTRY_LENGTH
import types.Genres.GENRES_MAX_LENGTH

import java.time.LocalDate

class MovieTest extends AnyFlatSpec, Matchers, ScalaCheckPropertyChecks {

  extension (s: String) { def withQuotes: String = "\"" + s + "\"" }

  private val movie = Movie(
    title = Title("2001, l'Odyssée de l'espace"),
    country = Country("USA"),
    year = 1968,
    originalTitle = Some("2001 a space odyssey"),
    frenchRelease = Some(LocalDate.parse("1968-09-27")),
    synopsis = None,
    genres = Genres("SF" :: Nil),
    ranking = Ranking(8)
  )

  "Movie entity" should "generate expected json when serialized" in {
    val expectedJsonStr =
      s"""{"title":"2001, l'Odyssée de l'espace","country":"USA","year":1968,"original_title":"2001 a space odyssey","french_release":"1968-09-27","synopsis":null,"genres":["sf"],"ranking":8}"""
    movie.asJson.noSpaces shouldBe expectedJsonStr
  }

  it should "be deserialized when given valid json" in {
    val jsonGen = for {
      titleGen <- Gen
        .listOfN(Title.MAX_TITLE_LENGTH, Gen.alphaNumChar)
        .map(_.mkString.withQuotes)
      countryGen <- Gen
        .listOfN(VALID_COUNTRY_LENGTH, Gen.alphaUpperChar)
        .map(_.mkString.withQuotes)
      yearGen <- Gen.chooseNum(Int.MinValue, Int.MaxValue)
      originalTitleGen <- Gen.option(Gen.alphaNumStr.map(_.withQuotes))
      frenchRelease <- Gen.option(
        Gen.calendar.flatMap(calendar =>
          LocalDate
            .ofInstant(calendar.toInstant, calendar.getTimeZone.toZoneId)
            .toString
            .withQuotes
        )
      )
      synopsisGen <- Gen.option(Gen.alphaNumStr.map(_.withQuotes))
      genresGen <- Gen.nonEmptyListOf(
        Gen
          .listOfN(GENRES_MAX_LENGTH, Gen.alphaNumChar)
          .map(_.mkString.withQuotes)
      )
      rankingGen <- Gen.chooseNum(
        Ranking.RANKING_MIN_VALUE,
        Ranking.RANKING_MAX_VALUE
      )
    } yield s"""
         |{
         |    "title": $titleGen,
         |    "country": $countryGen,
         |    "year": $yearGen,
         |    "original_title": ${originalTitleGen.getOrElse("null")},
         |    "french_release": ${frenchRelease.getOrElse("null")},
         |    "synopsis": ${synopsisGen.getOrElse("null")},
         |    "genres": [${genresGen.mkString(",")}],
         |    "ranking": $rankingGen
         |}
         |""".stripMargin

    forAll(jsonGen) { jsonStr =>
      (for {
        json <- parser.parse(jsonStr)
        movie <- json.as[Movie]
      } yield movie).isRight shouldBe true
    }
  }

}
