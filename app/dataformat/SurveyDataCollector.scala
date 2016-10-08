package dataformat

import javax.inject._

import data.Surveys
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class SurveyDataCollector @Inject()(ws: WSClient) extends Controller {

  /**
    * Get all the surveys
    *
    * @return
    */
  def allSurveys = {
    val surveysRequest: WSRequest = ws
      .url("https://api.surveymonkey.net/v2/surveys/get_survey_list")
      .withHeaders(
        "Authorization" -> s"bearer ${Surveys.USER_ACCESS_TOKEN}",
        "Content-Type" -> "application/json"
      )
      .withQueryString("api_key" -> Surveys.API_KEY)
      .withRequestTimeout(10000.millis)

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
}
