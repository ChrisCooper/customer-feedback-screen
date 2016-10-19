package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.{FeedbackAggregatorActor, MonkeyActor, UserWebSocketActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._

@Singleton
class VoiceController @Inject()(@Named("feedbackActor") feedbackActor: ActorRef)
                               (implicit system: ActorSystem, materializer: Materializer) extends Controller {


  // Render basic page
  def index = Action { implicit request =>
    Ok(views.html.voice.the_voice())
  }

  def openWebSocket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef(outActorRef => UserWebSocketActor.props(outActorRef, feedbackActor))
  }
}

