package controllers

import javax.inject._

import data.Surveys
import dataformat.SurveyDataCollector
import play.api.http.ContentTypes
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * This class gives raw survey monkey data
  */
@Singleton
class MonkeyController @Inject()(ec: ExecutionContext, dataCollector: SurveyDataCollector) extends Controller {

  def monkeyHome = Action { implicit request =>
    Ok(views.html.monkey.index(Surveys.SURVEYS))
  }

  /**
    * Gets metadata for all surveys in the account
    */
  def allSurveys = Action.async { implicit request =>
    dataCollector.allSurveys.map(surveyData =>
      Ok(Json.prettyPrint(surveyData)).as(ContentTypes.JSON)
    )
  }

  /**
    * Gets deeper metadata for one survey
    */
  def surveyMetadata(bannerId: String) = Action.async { implicit request =>
    dataCollector.surveyMetadata(bannerId).map(surveyData =>
      Ok(Json.prettyPrint(surveyData)).as(ContentTypes.JSON)
    )
  }
}
