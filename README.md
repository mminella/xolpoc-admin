# XD on Lattice PoC

## A: Initial Setup

1: Launch lattice with vagrant as described [here](http://lattice.cf/docs/getting-started/).

2: Run a private Docker registry, and configure Lattice to use that as described [here](http://lattice.cf/docs/private-docker-registry/).

3: Install Spring XD as described [here](http://docs.spring.io/spring-xd/docs/current/reference/html/#osx-homebrew-installation) for homebrew users (or see the link beneath that for alternatives).

*NOTE:* There is no need to start xd-singlenode. The installation is only needed for the modules that will be included in the Docker image you will build in an upcoming step.

4: Export XD_HOME, for example:

````
$ export XD_HOME=/usr/local/Cellar/springxd/1.1.2.RELEASE/libexec/xd
````

5: Create a top level directory for the various xolpoc repositories, for example:

````
$ mkdir ~/xolpoc-workspace
$ cd ~/xolpoc-workspace
````

6: Build the receptor-client and install the JAR into your local Maven repository:

````
$ git clone https://github.com/markfisher/receptor-client.git
$ cd receptor-client
$ ./gradlew install
$ cd ..
````

7: Build the container-less ModuleRunner project:

````
$ git clone https://github.com/markfisher/xolpoc.git
$ cd xolpoc
$ ./gradlew build
$ export XD_POC_MODULE=`pwd`
$ cd ..
````

8: Build the Admin project:

````
$ git clone https://github.com/markfisher/xolpoc-admin.git
$ cd xolpoc-admin
$ ./gradlew build
$ export XD_POC_ADMIN=`pwd`
$ cd ..
````

9: Build the Docker image in xolpoc-docker:

````
$ git clone https://github.com/markfisher/xolpoc-docker.git
$ cd xolpoc-docker
$ ./build
````

10: Push the docker image to the local registry. First, run `docker images` to get the image ID for springxd/xol-poc, then tag it for the local registry:

````
$ IMAGE_ID=`docker images | grep springxd/xol-poc | awk '{ print $3 }'`
$ docker tag -f $IMAGE_ID 192.168.59.103:5000/xol-poc
$ docker push 192.168.59.103:5000/xol-poc
````

## B: Starting XD Admin

1: Create a Redis instance on Lattice (running as root):

````
$ ltc create redis redis -r
````

2: Run the Admin jar to bootstrap an xd-admin LRP onto the Diego runtime:

````
$ java -jar $XD_POC_ADMIN/build/libs/xolpoc-admin-0.0.1-SNAPSHOT.jar
````

3: You should now see the xd-admin when you execute ltc list.

4: Run `ltc status xd-admin` to verify it has started (first time will require waiting for the docker image).


## C: Deploying XD Streams

1: View the current streams (should be empty):

````
$ curl http://xd-admin.192.168.11.11.xip.io/streams
````

2: Deploy ticktock:

````
$ curl -X POST -H "Content-Type: text/plain" --data "time | log" http://xd-admin.192.168.11.11.xip.io/streams/ticktock
````

3: View the streams again (should see *ticktock* this time):

````
$ curl http://xd-admin.192.168.11.11.xip.io/streams
````

4: (optional) Scale the log sink module using the ltc command line:

````
ltc scale xd-ticktock-log-1 2
ltc list      # now shows 2 instances of xd-ticktock-log-1
````

5: Delete the ticktock stream:

````
$ curl -X DELETE http://xd-admin.192.168.11.11.xip.io/streams/ticktock
````

6: Verify no streams are still running:

````
$ curl http://xd-admin.192.168.11.11.xip.io/streams
````
