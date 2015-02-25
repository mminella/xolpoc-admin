# XD on Lattice PoC

1: launch lattice with vagrant as described [here](https://github.com/cloudfoundry-incubator/lattice#launching-with-vagrant)

2a: (temporary) clone the [receptor-client](https://github.com/markfisher/receptor-client) repo and run `./gradlew install` to install that JAR into your local Maven repository 

2b: clone *this* repo and from the root directory run `./gradlew build`

3: bootstrap the xd-admin onto lattice by running (from the host machine):

````
$ java -jar build/libs/xolpoc-admin-0.0.1-SNAPSHOT.jar
````

4: run `ltc status xd-admin` to verify it has started (first time will require waiting for the image download)

5: view the current modules (should be empty):

````
$ curl http://xd-admin.192.168.11.11.xip.io
````

6: deploy ticktock:

````
$ curl -X POST -H "Content-Type: text/plain" --data "time | log" http://xd-admin.192.168.11.11.xip.io/ticktock
````

7: view the modules again (should see *xd-ticktock-time* and *xd-ticktock-log*):

````
$ curl http://xd-admin.192.168.11.11.xip.io
````

8: delete the ticktock stream:

````
$ curl -X DELETE http://xd-admin.192.168.11.11.xip.io/ticktock
````

9: verify no modules are still running:

````
$ curl http://xd-admin.192.168.11.11.xip.io
````
