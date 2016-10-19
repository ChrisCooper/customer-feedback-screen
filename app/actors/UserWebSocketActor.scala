package actors

import actors.FeedbackAggregatorActor.{Feedback, StoreInfo, UnwatchFeedback, WatchFeedback}
import akka.actor._
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{JsValue, Json, Reads, Writes}

object UserWebSocketActor {
  def props(outActorRef: ActorRef, feedbackActor: ActorRef) = Props(new UserWebSocketActor(outActorRef, feedbackActor))
}

class UserWebSocketActor(outActorRef: ActorRef, feedbackActor: ActorRef) extends Actor {

  println("Making UserVoiceActor actor")

  // Subscribe to feedback
  feedbackActor ! WatchFeedback()

  def receive = {

    //Receive JSON from the client
    case msg: JsValue =>
      println(s"Got message: $msg")
      val surveyResponse = Feedback("1234", 9, "Pretty good", StoreInfo("1028AA", "99 Atlantic"), new DateTime(DateTimeZone.UTC))

    //Send feedback to the user when we receive it
    case feedback@Feedback(orderNumber, _, _, _, _) =>
      println(s"yo, feedback from order: $orderNumber")

      // TODO these belong in a companion object for SurveyResponse and StoreInfo
      implicit val yourJodaDateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
      implicit val yourJodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")
      implicit val storeInfoFormat = Json.format[StoreInfo]
      implicit val surveyResponseFormat = Json.format[Feedback]

      outActorRef ! Json.toJson(feedback)
  }

  /**
    * Called when the websocket has closed.
    */
  override def postStop() = {
    feedbackActor ! UnwatchFeedback()
    println("UserVoiceActor actor stopped")
  }
}

