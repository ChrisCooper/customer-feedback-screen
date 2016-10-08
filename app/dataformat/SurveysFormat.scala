package dataformat

/**
  * A stupid interface for you to implement to supply sensitive account data. Implementation stored in a git submodule.
  */
trait SurveysFormat {
  def API_KEY: String
  def USER_ACCESS_TOKEN: String

  def SURVEYS: Map[String, SurveyDetailsFormat]
}

/**
  * Individual survey information
  */
trait SurveyDetailsFormat {
  def surveyId: String
}