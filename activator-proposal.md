Dear Typesafe team,

we propose the following activator-template: 
When we are talking to our customers about reactive applications, a common question is how those applications are really different from _modern_ JavaEE (6) applications. To give them a non-trivial showcase, we want to take an open source JavaEE reference application, the [JBoss Ticket Monster](http://www.jboss.org/ticket-monster/whatisticketmonster/), and transform its backend into an reactive application using scala and akka. Ticket Monster is kind of a a standard CRUD application using recent JavaEE technologies such as JAX-RS, CDI, JPA (+ classic relational database) and BeanValidation with little _business logic_ plus a rich javascript frontend.

With both backends in our hands, we want to show how both applications behave to different runtime scenarios including:
- Increasing number of concurrent users
- Increasing amount of data
- Partial failures in the system (dying and resurrected frontend nodes)

Our goals for the activator template are:

- Scalable deployment to EC2 as docker container (using sbt-native-packager)
- Async non-blocking REST interface using spray or akka-http
- Machine fault tolerance using akka-cluster
- Event-Sourcing and state replication through akka-persistence
- Optional: A feature to demonstrate near realtime interactivity by providing live-updates of available tickets/seats using _HTML5 Server Sent Events_.


Open issues:

- We are in contact with the developers of JBoss Ticket Monster to get it proper OSS licensed or at least a usage permission (this may require a rebranding of the user-interface).

