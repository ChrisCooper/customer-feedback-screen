package actors

import javax.inject.{Inject, Named}

import actors.MonkeyActor.{GiveFeedbackHistory, GiveNewFeedbacks}
import akka.actor._
import org.joda.time.DateTime

import scala.collection.immutable.HashSet
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object FeedbackAggregatorActor {

  case class WatchFeedback()

  case class UnwatchFeedback()

  case class GiveNextFeedback()

  case class Feedback(orderNumber: String, npsScore: Int, feedbackText: String, store: StoreInfo, timestampUtc: DateTime)

  case class StoreInfo(storeId: String, name: String)

}

/**
  * Queues the latest feedback to send to clients
  */
class FeedbackAggregatorActor @Inject()(@Named("monkeyActor") monkeyActor: ActorRef)(implicit ec: ExecutionContext) extends Actor {

  import FeedbackAggregatorActor._

  val maxFeedbacks = 10
  val queue: mutable.Queue[Feedback] = mutable.Queue()

  var listeners: HashSet[ActorRef] = HashSet.empty[ActorRef]

  // Start by sending a request for the most recent feedback
  monkeyActor ! GiveFeedbackHistory(maxFeedbacks) // Try for more feedback

  var lastFeedbackTime = new DateTime()

  // Collect new feedback periodically
  context.system.scheduler.schedule(3.seconds, 2.seconds, monkeyActor, GiveNewFeedbacks(lastFeedbackTime))

  // Send next feedback to listeners periodically
  context.system.scheduler.schedule(3.seconds, 7.seconds, self, GiveNextFeedback())

  def receive = {
    case WatchFeedback =>
      listeners = listeners + sender

    case UnwatchFeedback =>
      listeners = listeners - sender

    // A new feedback has been collected
    case feedback@Feedback(orderNumber, _, _, _, _) =>
      queue.enqueue(feedback)

      // Throw out the oldest feedback if the queue is full
      if (queue.length > maxFeedbacks) {
        queue.dequeue()
      }

    case GiveNextFeedback() if queue.isEmpty =>
      print("No feedback in queue to give")

    // Time to update clients
    case GiveNextFeedback() =>
      print("Giving next feedback")

      val feedback = queue.dequeue()
      queue.enqueue(feedback)

      // Send the feedback to listeners
      listeners.foreach(_ ! feedback)
  }
}