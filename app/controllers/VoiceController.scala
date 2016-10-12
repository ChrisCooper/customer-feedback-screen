package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.{JsValue, Json, Reads, Writes}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import org.joda.time.{DateTime, DateTimeZone}

@Singleton
class VoiceController @Inject()(implicit system: ActorSystem, materializer: Materializer) extends Controller {

  // Home page that renders template
  def index = Action { implicit request =>
    Ok(views.html.voice.the_voice())
  }

  def openWebSocket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef(outActorRef => MyWebSocketActor.props(outActorRef))
  }

}

import akka.actor._

object MyWebSocketActor {
  def props(outActorRef: ActorRef) = Props(new MyWebSocketActor(outActorRef))
}

class MyWebSocketActor(outActorRef: ActorRef) extends Actor {
  def receive = {
    case msg: JsValue =>
      //outActorRef ! Json.obj("sentMessage" -> msg)
      val surveyResponse = SurveyResponse("1234", 9, "Pretty good", StoreInfo("1028AA", "99 Atlantic"), new DateTime(DateTimeZone.UTC))

      // TODO these belong in a companion class for SurveyResponse and StoreInfo
      implicit val yourJodaDateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
      implicit val yourJodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")
      implicit val storeInfoFormat = Json.format[StoreInfo]
      implicit val surveyResponseFormat = Json.format[SurveyResponse]

      outActorRef ! Json.toJson(surveyResponse)
  }

  /**
    * Called when the websocket has closed.
    */
  override def postStop() = {
    println("Websocket actor stopped")
  }

  println("Made ws actor")
}



/*object SurveyResponse {
  implicit val yourJodaDateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val yourJodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val storeInfoFormat = Json.format[StoreInfo]
  implicit val surveyResponseFormat = Json.format[SurveyResponse]
}*/
case class SurveyResponse(
                         orderNumber: String,
                         npsScore: Int,
                         feedbackText: String,
                         store: StoreInfo,
                         timestampUtc: DateTime
                         )

case class StoreInfo(storeId: String, name: String)


