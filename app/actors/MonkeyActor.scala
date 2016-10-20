package actors


import javax.inject.Inject

import actors.FeedbackAggregatorActor.{Feedback, StoreInfo}
import akka.actor.{Actor, ActorLogging}
import data.Surveys
import dataformat.{BannerSurvey, MonkeyClient, Respondent}
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Json, _}

import scala.concurrent.{ExecutionContext, Future}

object MonkeyActor {

  case class GiveFeedbackHistory(numFeedbacks: Int)

  case class FeedbackHistory(feedbacks: Seq[Feedback])

  case class GiveNewFeedbacks(lastFeedbackTime: DateTime)

}

/**
  * Limits the rate of requests to survey monkey
  */
class MonkeyActor @Inject()(monkeyClient: MonkeyClient)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  import MonkeyActor._

  def receive = {
    case GiveFeedbackHistory(numFeedbacks: Int) =>
      // Save the current sender so the future still has the right one
      val s = sender

      // When we get the list of feedbacks, send them to the sender as a history message
      getRecentAnswers("loblaws", numFeedbacks).map { feedbacks => s ! FeedbackHistory(feedbacks) }

    case GiveNewFeedbacks(lastFeedbackTime) =>
      println(s"Sender: $sender time: $lastFeedbackTime")
  }

  def getRecentAnswers(bannerId: String, numFeedbacks: Int): Future[Seq[Feedback]] = getAnswersSince(bannerId, numFeedbacks, new DateTime)

  def getAnswersSince(bannerId: String, numFeedbacks: Int, sinceDate: DateTime): Future[Seq[Feedback]] = {

    val banner = Surveys.SURVEYS(bannerId)

    val x: Future[Seq[Feedback]] = for {
      respondents <- getRespondentsSince(banner, numFeedbacks, sinceDate)
      answersJson <- getAnswersForRespondents(banner, respondents)
    } yield answersJson

    x
  }

  /**
    * Get all the respondent that have occurred for the banner since the given date
    */
  def getRespondentsSince(banner: BannerSurvey, numFeedbacks: Int, sinceDate: DateTime): Future[Seq[Respondent]] = {

    val request = monkeyClient.request("/surveys/get_respondent_list")

    val requestData = Json.obj(
      "survey_id" -> banner.monkeyId,
      "page_size" -> numFeedbacks,
      "fields" -> Json.arr(
        "date_modified"
      )
    )

    request.post(requestData).map(response => {

      val json = response.json

      implicit val yourJodaDateReads = Reads.jodaDateReads("yyyy-MM-dd HH:mm:ss")

      implicit val respondentReads = (
        (JsPath \ "respondent_id").read[String] ~
          (JsPath \ "date_modified").read[DateTime](yourJodaDateReads)
        ) (Respondent.apply _)

      implicit val respondentListReads = (JsPath \ "data" \ "respondents").read[Seq[Respondent]]

      json.validate[Seq[Respondent]] match {
        case success: JsSuccess[Seq[Respondent]] =>
          val respondents: Seq[Respondent] = success.get
          respondents
        case error: JsError => {
          println(s"Fuuuuuuuuck. Error: $error")
          // error handling flow
          ???
        }
      }
    })
  }

  case class RawAnswer(respondentId: String, questions: Seq[RawQuestionAnswer])
  case class RawQuestionAnswer(questionId: String, answers: Seq[Map[String, String]])

  /**
    * Get the answers corresponding to the given respondents
    */
  def getAnswersForRespondents(banner: BannerSurvey, respondents: Seq[Respondent]): Future[Seq[Feedback]] = {
    println(s"Got some fuckin respondents: ${respondents.size}")

    val request = monkeyClient.request("/surveys/get_responses")

    val requestData = Json.obj(
      "survey_id" -> banner.monkeyId,
      "respondent_ids" -> Json.arr(respondents.map(r => r.respondentId))
    )

    request.post(requestData).map(response => {
      val json = response.json

      implicit val rawQuestionAnswerReads = (
        (JsPath \ "question_id").read[String] ~
          (JsPath \ "answers").read[Seq[Map[String, String]]]
        ) (RawQuestionAnswer.apply _)

      implicit val rawAnswerReads = (
        (JsPath \ "respondent_id").read[String] ~
          (JsPath \ "questions").read[Seq[RawQuestionAnswer]]
        ) (RawAnswer.apply _)

      implicit val answerListReads = (JsPath \ "data").read[Seq[RawAnswer]]

      json.validate[Seq[RawAnswer]] match {
        case success: JsSuccess[Seq[RawAnswer]] =>
          val rawAnswers: Seq[RawAnswer] = success.get

          val rawAnswersMap = rawAnswers.map(rawAnswer => rawAnswer.respondentId -> rawAnswer).toMap

          convertToFeedback(rawAnswersMap, respondents)
        case error: JsError => {
          println(s"Fuuuuuuuuck. Error: $error")
          // error handling flow
          ???
        }
      }
    }
    )

    ???
  }

  def convertToFeedback(rawAnswers: Map[String, RawAnswer], respondents: Seq[Respondent]): Seq[Feedback] = {
    rawAnswers.map{ case (respondentId, rawAnswer) =>


      //Feedback(orderNumber, npsScore, feedbackText, StoreInfo(storeId, storeName), timestamp)
      Feedback("1234", 10, "So amazing", StoreInfo("1234", "Best Loblaw's"), new DateTime())
    }
  }.toSeq

}


/*
{
  "status" : 0,
  "data" : {
    "respondents" : [ {
      "date_modified" : "2016-10-19 18:58:12",
      "respondent_id" : "5049017751"
    }, {
      "date_modified" : "2016-10-19 17:12:58",
      "respondent_id" : "5048780024"
    }, {
 */


/*
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

implicit val locationReads: Reads[Location] = (
  (JsPath \ "lat").read[Double](min(-90.0) keepAnd max(90.0)) and
  (JsPath \ "long").read[Double](min(-180.0) keepAnd max(180.0))
)(Location.apply _)

implicit val residentReads: Reads[Resident] = (
  (JsPath \ "name").read[String](minLength[String](2)) and
  (JsPath \ "age").read[Int](min(0) keepAnd max(150)) and
  (JsPath \ "role").readNullable[String]
)(Resident.apply _)

implicit val placeReads: Reads[Place] = (
  (JsPath \ "name").read[String](minLength[String](2)) and
  (JsPath \ "location").read[Location] and
  (JsPath \ "residents").read[Seq[Resident]]
)(Place.apply _)


val json = { ... }

json.validate[Place] match {
  case s: JsSuccess[Place] => {
    val place: Place = s.get
    // do something with place
  }
  case e: JsError => {
    // error handling flow
  }
}
 */


/*
 implicit val bsnsRds = (
      (JsPath \ "business" \ "name").read[String] ~
      (JsPath \ "business" \ "preferredUrl").read[String] ~
      (JsPath \ "business" \ "businessPhone").read[String] ~
      (JsPath \ "business" \ "retailer").read[Retailer](rltRds)
    )(Business)
 */

/*
implicit val bsnsRds = ({
  val business = (__ \ "business")
  (business \ "name").read[String] ~
  (business \ "preferredUrl").read[String] ~
  (business \ "businessPhone").read[String] ~
  (business \ "retailer").read[Retailer](rltRds)
})(Business)
 */