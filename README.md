# medicalchain

### Quick start

cd medicalchain
mvn clean install
java -jar medicalchain.jar 8080 7001
java -jar medicalchain.jar 8081 7002 ws://localhost:7001

```


### HTTP API

- query blocks

  ```
  curl http://localhost:8080/blocks

  ```

- mine block

  ```
  curl -H "Content-type:application/json" --data '{"data" : "Some data to the first block"}' http://localhost:8080/addBlock

  ```

- add peer

  ```
  curl -H "Content-type:application/json" --data '{"peer" : "ws://localhost:7001"}' http://localhost:8080/addPeer

  ```

- query peers

  ```
  curl http://localhost:8080/peers
  ```
