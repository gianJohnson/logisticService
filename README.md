To solve the problem in assessment_backend_java.pdf_

I've created a SpringBoot Application with a service called APiQueuingService.
The application uses a mock Server (MockRestServiceServer) and rest clients to contact the endpoints.
In the test package ApiQueuingServiceApplicationTests has few cases
to test the service process() against the mocked api.
In the setUp() method the shipments api is mocked as 503, printing and track with 200 (with mocked request).
Api Urls and Cap configured.

The test prints on the standard output the responses.

RUNNING

run the test class in your IDE.
(or just mvn clean install)


