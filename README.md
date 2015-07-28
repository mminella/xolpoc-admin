# XD on Lattice PoC

## A: Initial Setup

1: Launch lattice with vagrant as described [here](http://lattice.cf/docs/getting-started/).

2: Run a private Docker registry, and configure Lattice to use that as described [here](http://lattice.cf/docs/private-docker-registry/).

3: Build the receptor-client and install the JAR into your local Maven repository:

````
$ git clone https://github.com/markfisher/receptor-client.git
$ cd receptor-client
$ ./gradlew install
$ cd ..
````

4: Build the module-launcher project and its Docker image:

````
$ git clone https://github.com/markfisher/module-launcher.git
$ cd module-launcher
$ ./gradlew build
$ ./dockerize.sh
$ cd ..
````

5: Build this project and its Docker image:

````
$ git clone https://github.com/markfisher/xolpoc-admin.git
$ cd xolpoc-admin
$ ./gradlew build
$ ./dockerize.sh
$ cd ..
````

6: Push the Docker images to the private registry (if necessary, run `$(boot2docker shellinit)` first):

````
$ docker push 192.168.59.103:5000/module-launcher
$ docker push 192.168.59.103:5000/xd-admin
````

## B: Starting XD Admin

1: Create a Redis instance on Lattice (running as root):

````
$ ltc create redis redis -r
````

2: Run the xd-admin as a long-running process (LRP) on Lattice:

````
$ ltc create xd-admin 192.168.59.103:5000/xd-admin
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
ltc list                        # now shows 2 instances of xd-ticktock-log-1
````

5: Delete the ticktock stream:

````
$ curl -X DELETE http://xd-admin.192.168.11.11.xip.io/streams/ticktock
````

6: Verify no streams are still running:

````
$ curl http://xd-admin.192.168.11.11.xip.io/streams
````

## D: Deploying XD Jobs

1: View the current jobs (should be empty):

````
$ curl http://xd-admin.192.168.11.11.xip.io/tasks
````

2: Deploy timestampfile job:

````
$ curl -X POST -H "Content-Type: text/plain" --data "timestampfile" http://xd-admin.192.168.11.11.xip.io/tasks/timestampfile
````

3: View the jobs again (should see *timestampfile* this time):

````
$ curl http://xd-admin.192.168.11.11.xip.io/tasks
````

5: Verify no jobs are still running:

````
$ curl http://xd-admin.192.168.11.11.xip.io/tasks
````
