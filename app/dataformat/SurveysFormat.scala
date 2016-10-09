package dataformat

/**
  * A stupid interface for you to implement to supply sensitive account data. Implementation stored in a git submodule.
  */
trait SurveysFormat {
  val API_KEY: String
  val USER_ACCESS_TOKEN: String

  val SURVEYS: Map[String, BannerSurvey]
}

/**
  * Individual survey information
  */
case class BannerSurvey(bannerName: String, monkeyId: String, structure: FeedbackStructure)

case class FeedbackStructure(feedbackId: String, npsId: String, npsZeroId: String, npsTenId: String)

