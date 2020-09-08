# The prime numbers application implementation

The application has 2 components: the proxy-service and the prime-number-server.

The proxy has 2 main roles: 
 * it exposes an HTTP endpoint responding to GET /prime/<number> requests
 * it dispatches the request to the server to determine the list of prime numbers
 
The server determines the list of prime numbers up to the given input number and then it returns it as 
a response to the proxy, which will send it to the original requester.

The communication between the proxy and the server uses Finagle and Thrift.

The Scrooge plugin is used for generating the Scala files from the thrift files (prime_numbers_service.thrift).

Validations: when receiving the request, the proxy validates that the integer provided is not less than 2 
(as 2 is the first prime number). In case the validation is removed, the server will still be able to handle the request, as it will return an empty list for any parameter less than 2.

In case the processing on the server takes more than expected, then there's a request timeout of 10 seconds that will be applied. While testing, I noticed that for large numbers, when the processing took a few seconds, then the server
was no longer capable of serving other requests. For this reason, the serverThreadPool and the withExecutionOffloaded
configurations are used in PrimeNumbersServer.
 
#To use the app:
1. sbt publish-local
2. sbt "runMain primenumbers.PrimeNumbersServer -port 8082  -admin.port :8085" (to start the prime numbers server)
3. sbt "runMain primenumbers.PrimeNumbersApi -port 8081" (to start the proxy service)
4. send requests to GET http://localhost:8081/prime/{number}
