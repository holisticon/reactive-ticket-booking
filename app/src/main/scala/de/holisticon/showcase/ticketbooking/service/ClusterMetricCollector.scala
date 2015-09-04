package de.holisticon.showcase.ticketbooking.service

import akka.actor.{ Props, ActorLogging, Actor, ActorRef }
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.metrics.StandardMetrics.{ Cpu, HeapMemory }
import akka.cluster.metrics.{ NodeMetrics, ClusterMetricsChanged, ClusterMetricsExtension }
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import ClusterMetricCollector.{ EventStream, Register }
import NodeMetricSubscription.{ Unsubscribe, NewSubscriber }

import scala.annotation.tailrec

private object NodeMetricSubscription {
  sealed trait Protocol
  case class NewSubscriber(subscriber: ActorRef, startOffset: Option[Long]) extends Protocol
  case class Unsubscribe(subscriber: ActorRef) extends Protocol
  def props(producer: ActorRef, startOffset: Option[Long]) = Props[NodeMetricSubscription](new NodeMetricSubscription(producer, startOffset))
}

private class NodeMetricSubscription private (producer: ActorRef, startOffset: Option[Long]) extends ActorPublisher[(Long, NodeMetrics)] {

  override def preStart() { producer ! NewSubscriber(self, startOffset) }
  override def postStop() { producer ! Unsubscribe(self) }

  val MaxBufferSize = 100
  var buf = Vector.empty[(Long, NodeMetrics)]

  override def receive: Receive = {

    case (l: Long, nm: NodeMetrics) =>
      if (buf.isEmpty && totalDemand > 0)
        onNext(l -> nm)
      else if (buf.size == MaxBufferSize) {
        buf = buf.drop(1) :+ l -> nm
        deliverBuf()
      } else {
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
  def clusterMetricCollectorRef: ActorRef
}

object ClusterMetricCollector {

  sealed trait Protocol
  final case class Register(startOffset: Option[Long]) extends Protocol
  final case class EventStream(stream: Source[(Long, NodeMetrics), ActorRef]) extends Protocol

}

class ClusterMetricCollector extends Actor with ActorLogging {

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

        subscribers foreach { _ ! currentOffset -> clusterMetric }
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
      sender() ! EventStream(Source.actorPublisher(NodeMetricSubscription.props(self, startOffset)))

  }

}
