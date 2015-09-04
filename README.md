# ReactiveTicketBooking

TBD

## Building TicketBooking

TicketMonster can be built from Maven, by runnning the following Maven command:

    $ sbt package
	
	
	
## Stress Test

With this module you can stress test a ticket booking application.

TODO: describe how to run against an arbitrary server

To run the recorder simulations:

    $ sbt it:test

To record your own simulations:

    $ sbt startRecorder

More documentation: [gatling.io](http://gatling.io/#/docs).	
	
	
## Run TicketBooking locally

You can run TicketBooking with sbt:
	
	$ sbt node1
	
You can start further nodes in a new shell using

	$ sbt node2
	$ sbt node3
	
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

Now you can run `sbt docker:deploy` to publish your docker image. Note that this may consume a bunch of bandwidth and take some time.

### Publish to AWS beanstalk


At first you need to change the `Dockerrun.aws.json` image according to your freshly deployed Image.

Then log into the AWS console and 
[create a new ElasticBeanStalk application for a multi docker container](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/create_deploy_docker_ecs.html).
If asked for the application file, simply upload the `Dockerrun.aws.json` file.

_Note: You need to slect a AWS region that supports EC2 Container Cloud._

Now give AWS some time and your Reactive Ticket Booking should soon be available on your selected `myapp.elastibeanstalk.com` address.

You may now play around with autoscaling modes and monitoring to render your Reactive Ticket Booking undestroyable!
