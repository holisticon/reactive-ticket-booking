package de.holisticon.example.ticketmonster.service

import akka.actor.{ Props, ActorLogging, Actor, ActorRef }
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.metrics.StandardMetrics.{ Cpu, HeapMemory }
import akka.cluster.metrics.{ NodeMetrics, ClusterMetricsChanged, ClusterMetricsExtension }
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import de.holisticon.example.ticketmonster.service.ClusterService.{ EventStream, Register }
import de.holisticon.example.ticketmonster.service.NotificationSubscription.{ Unsubscribe, NewSubscriber }

import scala.annotation.tailrec

object NotificationSubscription {
  case class NewSubscriber(subscriber: ActorRef, startOffset: Option[Long])
  case class Unsubscribe(subscriber: ActorRef)
}

class NotificationSubscription(producer: ActorRef, startOffset: Option[Long]) extends ActorPublisher[(Long, NodeMetrics)] {

  override def preStart() { producer ! NewSubscriber(self, startOffset) }
  override def postStop() { producer ! Unsubscribe(self) }

  val MaxBufferSize = 100
  var buf = Vector.empty[(Long, NodeMetrics)]

  override def receive: Receive = {
    case evt @ (_: Long, _: NodeMetrics) if buf.size == MaxBufferSize => //drop
    case (l: Long, nm: NodeMetrics) =>
      if (buf.isEmpty && totalDemand > 0)
        onNext(l -> nm)
      else {
        buf :+= l -> nm
        deliverBuf()
      }
  }

  @tailrec private def deliverBuf(): Unit =
    if (totalDemand > 0) {
      /*
       * totalDemand is a Long and could be larger than
       * what buf.splitAt can accept
       */
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buf.splitAt(totalDemand.toInt)
        buf = keep
        use foreach onNext
      } else {
        val (use, keep) = buf.splitAt(Int.MaxValue)
        buf = keep
        use foreach onNext
        deliverBuf()
      }
    }
}

trait ClusterServiceAware {
  def clusterServiceRef: ActorRef
}

object ClusterService {

  sealed trait Protocol
  final case class Register(startOffset: Option[Long])
  final case class EventStream(stream: Source[(Long, NodeMetrics), ActorRef])

}

class ClusterService extends Actor with ActorLogging {

  val maxBufferSize = 1000
  var subscribers = Set.empty[ActorRef]
  val selfAddress = Cluster(context.system).selfAddress
  val extension = ClusterMetricsExtension(context.system)

  var currentOffset = 0L
  var buffer = Vector.empty[NodeMetrics]

  override def preStart(): Unit = extension.subscribe(self)
  override def postStop(): Unit = extension.unsubscribe(self)

  override def receive = {
    case ClusterMetricsChanged(clusterMetrics) =>
      for (clusterMetric <- clusterMetrics) {
        if (buffer.size == maxBufferSize) {
          buffer = buffer.drop(1) :+ clusterMetric
        } else {
          buffer = buffer :+ clusterMetric
        }
        currentOffset += 1

        subscribers.foreach { _ ! currentOffset -> clusterMetric }
      }

    case state: CurrentClusterState => // Ignore.
    case NewSubscriber(subscriber, startOffsetOpt) =>
      subscribers += subscriber
      val startOffset = startOffsetOpt.getOrElse(0L)
      if (startOffset < currentOffset) {
        val available = buffer.takeRight((currentOffset - startOffset).toInt).zipWithIndex
        available.foreach {
          case (metric, idx) =>
            subscriber ! currentOffset - available.size + idx -> metric
        }
      }
    case Unsubscribe(subscriber) =>
      subscribers -= subscriber
    case Register(startOffset) =>
      sender() ! EventStream(Source.actorPublisher(Props[NotificationSubscription](new NotificationSubscription(self, startOffset))))

  }

  def logHeap(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case HeapMemory(address, timestamp, used, committed, max) =>
      log.debug("Used heap: {} MB", used.doubleValue / 1024 / 1024)
    case _ => // No heap info.
  }

  def logCpu(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case Cpu(address, timestamp, Some(systemLoadAverage), cpuCombined, cpuStolen, processors) =>
      log.debug("Load: {} ({} processors)", systemLoadAverage, processors)
    case _ => // No cpu info.
  }
}
