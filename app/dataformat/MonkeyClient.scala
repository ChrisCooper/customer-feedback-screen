package dataformat

import javax.inject._

import data.Surveys
import play.api.libs.ws._

import scala.concurrent.duration._

@Singleton
class MonkeyClient @Inject()(ws: WSClient) {

  /**
    * Get a request for Survey Monkey
    *
    * @param endpoint e.g. /surveys/get_survey_details
    */
  def request(endpoint: String): WSRequest = {
    ws.url(s"https://api.surveymonkey.net/v2$endpoint")
      .withHeaders(
        "Authorization" -> s"bearer ${Surveys.USER_ACCESS_TOKEN}",
        "Content-Type" -> "application/json"
      )
      .withQueryString("api_key" -> Surveys.API_KEY)
      .withRequestTimeout(10000.millis)
  }

}


