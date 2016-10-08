package controllers

import javax.inject._

import dataformat.SurveyDataCollector
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * This class gives raw survey monkey data
  */
@Singleton
class MonkeyController @Inject()(ec: ExecutionContext, dataCollector: SurveyDataCollector) extends Controller {

  /**
    * Gets metadata for all surveys in the account
    */
  def allSurveys = Action.async { implicit request =>
    dataCollector.allSurveys.map(surveyData =>
      Ok(Json.prettyPrint(surveyData))
    )
  }
}
