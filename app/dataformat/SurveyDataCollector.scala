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

    val request = mc.request("/surveys/get_survey_details")

    val requestData = Json.obj(
      "survey_id" -> banner.monkeyId
    )

    val surveysResponse = request.post(requestData)

    surveysResponse.map(response =>
      response.json
    )
  }

  def respondents(bannerId: String) = {
    val banner = Surveys.SURVEYS(bannerId)

    val request = mc.request("/surveys/get_respondent_list")

    val requestData = Json.obj(
      "survey_id" -> banner.monkeyId,
      "fields" -> Json.arr(
        "date_modified"
      )
    )

    request.post(requestData).map(response =>
      response.json
    )
  }

  case class Respondent(respondentId: String, dateModified: String)

  def answers(bannerId: String) = {

    val banner = Surveys.SURVEYS(bannerId)

    /*val futureResponse: Future[WSResponse] = for {
      responseOne <- ws.url(urlOne).get()
      responseTwo <- ws.url(responseOne.body).get()
      responseThree <- ws.url(responseTwo.body).get()
    } yield responseThree

    futureResponse.recover {
      case e: Exception =>
        val exceptionData = Map("error" -> Seq(e.getMessage))
        ws.url(exceptionUrl).post(exceptionData)
    }*/



    val resps = respondents(bannerId).map(js => {
      val rs = (js \ "data" \ "respondents").as[Seq[Map[String, String]]]

      rs.map(resp =>
        Respondent(resp("respondent_id"), resp("date_modified"))
      )
    })

    val request = mc.request("/surveys/get_responses")

    val f: Future[Future[JsValue]] = resps.map(resps => {

      val requestData = Json.obj(
        "survey_id" -> banner.monkeyId,
        "respondent_ids" -> Json.arr(
          resps.map(r => r.respondentId)
        )
      )

      request.post(requestData).map(response =>
        response.json
      )
    })
  }

}


