package de.holisticon.example.ticketmonster.rest

import akka.actor.{ AddressFromURIString, Address }
import akka.cluster.metrics.{ EWMA, Metric, NodeMetrics }
import spray.json._

trait NodeMetricsProtocol extends DefaultJsonProtocol {

  implicit object AddressProtocol extends RootJsonFormat[Address] {
    override def write(obj: Address): JsValue = JsString(obj.toString)

    override def read(json: JsValue): Address = json match {
      case JsString(AddressFromURIString(address)) => address
      case x => deserializationError("Expected actor address, but got " + x)
    }
  }
  implicit def EWMAProtocol = jsonFormat2(EWMA.apply)

  implicit object NumberJsonFormat extends JsonFormat[Number] {
    def write(x: Number) = x match {
      case i: Integer => JsNumber(i)
      case i: java.lang.Long => JsNumber(i)
      case i: java.lang.Double => JsNumber(i)
      case i: java.lang.Float => JsNumber(i.floatValue())
      case i: java.lang.Short => JsNumber(i.shortValue())
    }
    def read(value: JsValue) = value match {
      case JsNumber(x) => x.doubleValue()
      case JsNull => Double.NaN
      case x => deserializationError("Expected JsNumber, but got " + x)
    }
  }

  implicit object MetricProtocol extends RootJsonFormat[Metric] {
    override def write(obj: Metric): JsValue = JsObject(
      "name" -> JsString(obj.name),
      "value" -> NumberJsonFormat.write(obj.value),
      "average" -> optionFormat(EWMAProtocol).write(obj.average)
    )

    override def read(json: JsValue): Metric = ???
  }

  implicit def NodeMetricsProtocol = jsonFormat3(NodeMetrics.apply)

}

object NodeMetricsProtocol extends NodeMetricsProtocol
