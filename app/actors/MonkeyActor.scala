package actors


import javax.inject.Inject

import actors.FeedbackAggregatorActor.Feedback
import akka.actor.{Actor, ActorLogging}
import dataformat.MonkeyClient
import org.joda.time.DateTime

object MonkeyActor {

  case class GiveFeedbackHistory(numFeedbacks: Int)

  case class FeedbackHistory(feedbacks: List[Feedback])

  case class GiveNewFeedbacks(lastFeedbackTime: DateTime)
}

/**
  * Limits the rate of requests to survey monkey
  */
class MonkeyActor @Inject()(monkeyClient: MonkeyClient) extends Actor with ActorLogging {

  import MonkeyActor._

  def receive = {
    case GiveFeedbackHistory(numFeedbacks: Int) =>
      sender ! FeedbackHistory(List())
    case GiveNewFeedbacks(lastFeedbackTime) =>
      println(s"Sender: $sender() time: $lastFeedbackTime")
  }

  /*def receive = LoggingReceive {
    case watchStock@WatchStock(symbol) =>
      // get or create the StockActor for the symbol and forward this message
      context.child(symbol).getOrElse {
        context.actorOf(Props(new StockActor(symbol)), symbol)
      } forward watchStock
    case unwatchStock@UnwatchStock(Some(symbol)) =>
      // if there is a StockActor for the symbol forward this message
      context.child(symbol).foreach(_.forward(unwatchStock))
    case unwatchStock@UnwatchStock(None) =>
      // if no symbol is specified, forward to everyone
      context.children.foreach(_.forward(unwatchStock))
  }*/

}
