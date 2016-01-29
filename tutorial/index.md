> Reactive Ticket Booking is a web application showcase that demonstrates how you can migrate classic JavaEE web applications onto the akka stack.

## Quickstart

Run this template with `$ activator run` or `$ sbt run` and open your browser at [http://localhost:8080/](http://localhost:8080/). This starts your application as an single node cluster.

Package it as docker image and run it in a container with `sbt docker:run`.

## Project structure

This activator template contains three modules.

- __web__: Client web application in backbone.js/angular.js.
- __app__: Akka standalone application. Serves a RESTful backend and hosts the web application.
- __stress__: Contains test scenarios for [gatling](http://gatling.io/#/). These scenarios can be used to compare the runtime behavior of ReactiveTicketBooking and similar implementations.

## Scope

Reactive Ticket Booking is a web application showcase that demonstrates how you can migrate classic JavaEE web applications onto the akka stack.

It originates in the JBoss ticket monster and tries to stay API compatible such that we can compare both implementations in their runtime behaviour in the face of high load, partial failure and network partitions.

This showcase demonstrates the following things:

- How to build a reactive restful backend with [akka-http](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0/scala/http/)
- How to build an angular frontend with sbt using webjars
- How to deal with scalability, state and contention using [distributed-data](http://doc.akka.io/docs/akka/2.4.0/scala/distributed-data.html), [cluster-sharding](http://doc.akka.io/docs/akka/2.4.0/scala/cluster-sharding.html) and [persistence](http://doc.akka.io/docs/akka/2.4.0/scala/persistence.html).
- How to simulate stress scenarios against your backend with [gatling](http://gatling.io/#/).

## The Domain

This activator template contains a ticket booking web application.
It allows visitors to find events and book tickets for the happening of an event.

> TODO: Class/ER diagram here

An event happening at a venue is called a show. An event can happen at different venues.
Each show can happen at different dates. We call the _happening_ of a show at a time a __performance__.

A venue can hold a limited number of visitors at once. In this domain, each venue consists of _sections_.
Each section contains a number of rows with a fixed row size. Each row has a number of seats. Thus a seat can be identified
by the venue, section, row- and seat number.

It is a strict requirement that each seat can be booked for one performance once and only once.

Since not all seats in a venue are equally good, the can have different ticket prices.

There can also be discounts for different kind of visitors like children or students. Therefore we introduce __ticket categories__.
A show has __ticket prices__ for each section and supported ticket category.

A visitor can issue a __booking request__ for a performance by providing a set of ticket requests
(each for a section and ticket category and a desired seat count for that section and ticket category).

A successful booking request results in a _booking_ that contains __tickets__ and a total price. A ticket allows a visitor to take place on a seat at a venue at the date of the performance.

A booking request can fail if there is at least one ticket request that cannot be satisfied (because the desired number of seats is no longer
available in the section).

To ease visitor navigation, each event can be assigned to an __event category__. A visitor browses events grouped by this category.

The web application allows _administrators_ to create, edit and remove events, shows, sections, performances, ticket categories and event categories.

The model does not (yet) cover:

- Dealing with ticket cancellations by the visitor.
- Dealing with performance or show cancellations by the organizer.
They can be deleted by administrators but there is no compensation action for already booked tickets.

## Data representation

In a traditional enterprise application, the previously described model could be represented as relational database schema. This would have the nice property, that we can model integrity constraints (like referential integrity) and provide transactions we would need to satisfy the _book seat once_ constraint.

But this would also require our backend to talk to the database for each user request to fetch the most recent information about events, shows and so on.
This is necessary for bookings but maybe overkill for data that changes slowly (like all the data that can only be changed by administrators).
Our backend could also implement a cache - but then we would have the challenge to decide when and how this cache is invalidated - in the best case directly when an administrator changes the data - but these cache invalidation would have to be propagated back to the backend in a reliable fashion.

This also means for seat reservations that, if kept in one table, there would be a single serialization point in our application - the database table lock. This is what we really want to avoid.

So we basically have two sets of data:

- data that (normally) does not change and is added slowly. They should be visible to the user as soon as possible - but small delays are not critical. Example: If an administrator adds a new event "Flying spaghetti monster on stage", we want all our visitors to see this event eventually and as soon as possible. We further call these data __facts__ (even if not appropriate). The amount of facts may grow but is always expected to fit into a backend's ram.

- data whose access must be serialized and that is modified at the rate of user interactions. In our model this would be the bookings and seat reservations. This is the part where we want to be scalable. The amount of data grows steadily and growth of this data pretty much renders our application/business successful.

### Facts

Facts can be distributed as CRDTs via akka [distributed-data](http://doc.akka.io/docs/akka/2.4.0/scala/distributed-data.html). That means that each backend node can create or update a fact and this update will be automatically propagated to all other cluster members using akka's cluster gossip mechanism. We use LWWMap (last write wins map) as CRDT-structure - so our fact _database_ acts pretty much like a distributed cache. Thus we do not really make any guarantees over concurrent fact modification - only that all nodes will end in the same state. This is not so much a problem since the creation of facts can be coordinated between administrators.

This setup makes it possible that each backend node can answer any non-booking related user requests locally, without further network interaction. New facts will be distributed to all cluster nodes eventually and with low latency.

### Sharded persistent actors

Within a booking request, a visitor can book tickets for different sections of a venue for a performance. The _decision_ over an booking request must therefore be made sequentially by something that keeps track over all reserved seats of a single performance. This is the perfect use case for an actor: PerformanceBookingNode.

To ensure that there exists only one single PerformanceBookingNode for a single performance in the cluster we use [cluster-sharding](http://doc.akka.io/docs/akka/2.4.0/scala/cluster-sharding.html). This allows an equal distribution of load or _responsibility_ for performance bookings over the whole cluster.

> Idea: In a spatial distributed deployment, the location of the performances venue could be used to co-locate the actor to a node that is closer to the venues location. This would reduce latency in deployment scenarios with higher intra-cluster latency under the assumption that visitors rather book tickets for venues where there are close to.

We do not want our bookings to get lost when the cluster is, for whatever reason, going down. This is why whe use [akka persistence](http://doc.akka.io/docs/akka/2.4.0/scala/persistence.html) to persist the change of seat reservations using event sourcing.

Persisting the seat reservations also ensures that the cluster can easily recover from a partial failure. As soon as akka-cluster detects a defect node, it will relocate all PerformanceBookingNode from the failing node to another healthy cluster node. The revived PerformanceBookingNode can be recreated on the healthy node by simply replaying all persisted events (i.E. seat reservations).

Akka cluster sharding uses the same mechanism to deal with re-balancing if the number of cluster nodes changes.

Compared to our earlier RDMS implementation, our current serialization scope is no longer the table (the set of all seat reservations) but just a single performance. We are also prepared for elastic deployments where nodes come up and go offline at any speed.

## Implementation Notes

The single http entry point to the backend is the [`HttpApi` trait](../app/src/main/scala/de/holisticon/showcase/ticketbooking/api/HttpApi.scala). It serves all static content from the web module as well as from web jar dependencies and implements default exception handlers.

The [`RestApi` trait](../app/src/main/scala/de/holisticon/showcase/ticketbooking/api/RestApi.scala) defines the protocol for the frontend application and mimics the API of our original JavaEE backend application.


### Actors and Protocols

The protocols of most actors are defined on their companion object. If these protocol messages are meant to be used in a request/response fashion, they contain the senders actorRef as first parameter.

This structure is a good practice to ease a future migration to akka typed. Each senderRef will then be replaced by a typed actorRef with the type argument of the desired response message type.


## Stress Test

You can stress test a ticket booking application.

TODO: describe how to run against an arbitrary server

To run the recorder simulations:

    $ sbt it:test

To record your own simulations:

    $ sbt startRecorder

More documentation: [gatling.io](http://gatling.io/#/docs).	


## Run on AWS BeanStalk


### Deploy your docker image to a public docker registry

__you can skip this step if yo just want to deploy the unmodified demo__

ReactiveTicketBooking can be packaged as a docker container using `$sbt docker:publishLocal`

Please set up docker first!

To run it on AWS BeanStalk you need to publish it to a public docker registry. 

In this guide we use [BinTray as docker repository](https://bintray.com/docs/usermanual/docker/docker_workingwithdocker.html) 
but you may choose to publish it somewhere else.

After that you need to change the docker deploy repository in the `build.sbt`:

	dockerRepository := Some("my-public-docker-registry.io),

Now you can run `sbt docker:deploy` to publish your docker image. Note that this step may consume a bunch of bandwidth and take some time.

### Publish to AWS beanstalk


At first you need to change the `Dockerrun.aws.json` image according to your freshly deployed Image.

Then log into the AWS console and 
[create a new ElasticBeanStalk application for a multi docker container](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/create_deploy_docker_ecs.html).
If asked for the application file, simply upload the `Dockerrun.aws.json` file.

_Note: You need to slect a AWS region that supports EC2 Container Cloud._

Now give AWS some time and your Reactive Ticket Booking should soon be available on your selected `myapp.elastibeanstalk.com` address.

You may now play around with autoscaling modes and monitoring to render your Reactive Ticket Booking undestroyable!
