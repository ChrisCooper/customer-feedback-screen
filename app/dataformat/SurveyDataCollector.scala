package dataformat

import javax.inject._

import data.Surveys
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.Future

@Singleton
class SurveyDataCollector @Inject()(mc: MonkeyClient) extends Controller {
  /**
    * Get all the surveys
    *
    * @return
    */
  def allSurveys = {
    val surveysRequest = mc.request("/surveys/get_survey_list")

    // Ask for the fields you want
    val requestData = Json.obj(
      "fields" -> Json.arr(
        "title",
        "date_created",
        "num_responses"
      )
    )

    val surveysResponse: Future[WSResponse] = surveysRequest.post(requestData)

    surveysResponse.map(response =>
      response.json
    )
  }

  def surveyMetadata(bannerId: String) = {
    val banner = Surveys.SURVEYS(bannerId)

    val surveysRequest = mc.request("/surveys/get_survey_details")

    val requestData = Json.obj(
      "survey_id" -> banner.monkeyId
    )

    val surveysResponse = surveysRequest.post(requestData)

    surveysResponse.map(response =>
      response.json
    )
  }

  def respondents(bannerId: String) = {
    val banner = Surveys.SURVEYS(bannerId)

    val surveysRequest = mc.request("/surveys/get_respondent_list")

    val requestData = Json.obj(
      "survey_id" -> banner.monkeyId,
      "fields" -> Json.arr(
        "date_modified"
      )
    )

    surveysRequest.post(requestData).map(response =>
      response.json
    )
  }


}


