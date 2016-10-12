package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.ActorFlow
import play.api.mvc._

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
      outActorRef ! Json.obj("sentMessage" -> msg)
  }

  /**
    * Called when the websocket has closed.
    */
  override def postStop() = {
    println("Websocket actor stopped")
  }

  println("Made ws actor")
}

