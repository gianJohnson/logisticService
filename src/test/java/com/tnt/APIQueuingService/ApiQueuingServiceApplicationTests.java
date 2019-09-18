package com.tnt.APIQueuingService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.APIQueuingService.service.ApiQueuingService;
import com.tnt.APIQueuingService.service.api.client.PriceApiClient;
import com.tnt.APIQueuingService.service.api.client.ShipmentApiClient;
import com.tnt.APIQueuingService.service.api.client.TrackApiClient;
import com.tnt.APIQueuingService.service.model.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiQueuingServiceApplicationTests {

	@Autowired
	private TrackApiClient trackApiClient;

	@Autowired
	private PriceApiClient priceApiClient;

	@Autowired
	private ShipmentApiClient shipmentApiClient;

	@Autowired
	private ApiQueuingService apiQueuingService;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${trackApiUri}")
	String trackApiUri;

	@Value("${pricingApiUri}")
	String pricingApiUri;

	private MockRestServiceServer mockServerPrice;
	private MockRestServiceServer mockServerTrack;
	private MockRestServiceServer mockServerShipment;

	@Before
	public void setUp() throws Exception {
		mockServerPrice = MockRestServiceServer.createServer(priceApiClient.getRestTemplate());
		mockServerTrack = MockRestServiceServer.createServer(trackApiClient.getRestTemplate());
		mockServerShipment = MockRestServiceServer.createServer(shipmentApiClient.getRestTemplate());

		//for simplicity the requests are mocked in groups of cap=5.
		String jsonReturnforTrackApi = "{\"109347263\":\"COLLECTING\", \"109347264\":\"COLLECTING\", " +
				"\"109347265\":\"NEW\", \"109347266\":\"NEW\", \"109347267\":\"COLLECTING\"}";

		mockServerTrack.expect(ExpectedCount.manyTimes(),
				requestTo("/track/109347263,109347264,109347265,109347266,109347267"))
				.andRespond(withSuccess(
						objectMapper.writeValueAsString(
								objectMapper.readValue(jsonReturnforTrackApi, Map.class)), MediaType.APPLICATION_JSON));

		String jsonReturnforPriceApi = "{\"CA\":\"14.24\", \"CN\":\"14.10\", " +
				"\"NL\":\"20.30\", \"NO\":\"13.12\", \"PE\":\"55.55\"}";

		mockServerPrice.expect(ExpectedCount.manyTimes(),
				requestTo("/pricing/CA,CN,NL,NO,PE"))
				.andRespond(withSuccess(
						objectMapper.writeValueAsString(
								objectMapper.readValue(jsonReturnforPriceApi, Map.class)), MediaType.APPLICATION_JSON));

		mockServerShipment.expect(ExpectedCount.manyTimes(),
				requestTo("/shipments/109347263"))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.SERVICE_UNAVAILABLE));

	}

	@Test
	public void apiQueueServiceResponseReturnTest() {
		//Call service process
		List<Request> requestCollection = new ArrayList<Request>();
		requestCollection.add(new Request(trackApiUri, "109347263"));
		requestCollection.add(new Request(trackApiUri, "109347264"));
		requestCollection.add(new Request( trackApiUri, "109347265"));
		requestCollection.add(new Request( trackApiUri, "109347266"));
		requestCollection.add(new Request( trackApiUri, "109347267"));
		requestCollection.add(new Request( pricingApiUri, "CA"));
		requestCollection.add(new Request( pricingApiUri, "CN"));
		requestCollection.add(new Request( pricingApiUri, "NL"));
		requestCollection.add(new Request( pricingApiUri, "NO"));
		requestCollection.add(new Request( pricingApiUri, "PE"));

		Optional<List<Map<String, String>>> response = apiQueuingService.process(requestCollection);
		assertTrue(response.isPresent());
		System.out.println("\nresponse: apiQueueServiceResponseReturnTest\n");
		response.get().forEach(map->map.
				forEach((k,v)->System.out.println(k + " : " + v))
		);
		System.out.print("\n");
	}

	@Test
	public void apiQueueServiceNoResponseReturnTest() {
		//Call service process
		List<Request> requestCollection = new ArrayList<Request>();
		requestCollection.add(new Request(trackApiUri, "109347263"));
		requestCollection.add(new Request(trackApiUri, "109347264"));
		requestCollection.add(new Request( trackApiUri, "109347267"));
		requestCollection.add(new Request( pricingApiUri, "CA"));
		requestCollection.add(new Request( pricingApiUri, "CN"));
		requestCollection.add(new Request( pricingApiUri, "NL"));
		requestCollection.add(new Request( pricingApiUri, "NO"));
		requestCollection.add(new Request( pricingApiUri, "PE"));

		assertNull(apiQueuingService.process(requestCollection).orElse(null));
	}

	@Test
	public void test503ForShipmentsApi() {
		try{
			shipmentApiClient.getShipmentsJson("109347263");
		}catch(HttpServerErrorException ex){
			assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
		}
	}

	@Test
	public void apiQueueServiceResponseReturnTestMixedline() {
		//Call service process
		List<Request> requestCollection = new ArrayList<Request>();
		requestCollection.add(new Request(trackApiUri, "109347263"));
		requestCollection.add(new Request(trackApiUri, "109347264"));
		requestCollection.add(new Request( pricingApiUri, "NL"));
		requestCollection.add(new Request( trackApiUri, "109347266"));
		requestCollection.add(new Request( trackApiUri, "109347267"));
		requestCollection.add(new Request( pricingApiUri, "CA"));
		requestCollection.add(new Request( pricingApiUri, "CN"));
		requestCollection.add(new Request( trackApiUri, "109347265"));
		requestCollection.add(new Request( pricingApiUri, "NO"));
		requestCollection.add(new Request( pricingApiUri, "PE"));

		Optional<List<Map<String, String>>> response = apiQueuingService.process(requestCollection);
		assertTrue(response.isPresent());
		System.out.println("\nresponse: apiQueueServiceResponseReturnTestMixedline\n");
		response.get().forEach(map->map.
				forEach((k,v)->System.out.println(k + " : " + v))
		);
		System.out.print("\n");
	}

	@Test
	public void apiQueueServiceResponseReturnTest2Batch() {
		//Call service process
		List<Request> requestCollection = new ArrayList<Request>();
		requestCollection.add(new Request(trackApiUri, "109347263"));
		requestCollection.add(new Request(trackApiUri, "109347264"));
		requestCollection.add(new Request( trackApiUri, "109347265"));
		requestCollection.add(new Request( trackApiUri, "109347266"));
		requestCollection.add(new Request( trackApiUri, "109347267"));
		requestCollection.add(new Request( pricingApiUri, "CA"));
		requestCollection.add(new Request( pricingApiUri, "CN"));
		requestCollection.add(new Request( pricingApiUri, "NL"));
		requestCollection.add(new Request( pricingApiUri, "NO"));
		requestCollection.add(new Request( pricingApiUri, "PE"));
		//same ids just in order to keep the mock server simple
		requestCollection.add(new Request(trackApiUri, "109347263"));
		requestCollection.add(new Request(trackApiUri, "109347264"));
		requestCollection.add(new Request( trackApiUri, "109347265"));
		requestCollection.add(new Request( trackApiUri, "109347266"));
		requestCollection.add(new Request( trackApiUri, "109347267"));

		Optional<List<Map<String, String>>> response = apiQueuingService.process(requestCollection);
		assertTrue(response.isPresent());

		System.out.println("\nresponse apiQueueServiceResponseReturnTest2Batch\n");
		response.get().forEach(map->map.
				forEach((k,v)->System.out.println(k + " : " + v))
		);
		System.out.print("\n");
	}


}
