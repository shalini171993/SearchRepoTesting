package steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.en.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.*;


public class searchSteps {
    private Response response;
    private WireMockServer wireMockServer;

    @Given("The user sets the baseURL for the Search API")
    public void the_user_sets_the_base_url_for_the_search_api() {
        baseURI="https://api.github.com/search";
    }

//    @Given("the mock server is set up to return validation error for an invalid query")
//    public void the_mock_server_is_set_up_to_return_validation_error_for_an_invalid_query() {
//        wireMockServer =new WireMockServer();
//        wireMockServer.start();
//
//        WireMock.configureFor("localhost",wireMockServer.port());
//        stubFor(get(urlEqualTo("/search/repositories?q=invalid_query"))
//                .willReturn(aResponse().withStatus(422).withHeader("Content-Type","application/json")
//                        .withBody("{\"message\":\"Validation Failed\"}")));
//    }
//
//
//    @Given("The mock server is setup to set the rate limit value as {int}")
//    public void the_mock_server_is_setup_to_set_the_rate_limit_value_as(int rateLimit) {
//        wireMockServer =new WireMockServer();
//        wireMockServer.start();
//
//        WireMock.configureFor("localhost",wireMockServer.port());
//        stubFor(get(urlEqualTo("/search/repositories?q=octocat"))
//                .willReturn(aResponse().withHeader("X-RateLimit-Remaining","0")
//                        .withStatus(429)
//                        .withBody("{\"message\":\"Rate Limit is exceeded\"}")));
//    }

    @When("The user hits the Search Repository API with exceeded rate limit value")
    public void the_user_hits_the_search_repository_api_with_exceeded_rate_limit_value() {
        RestAssured.baseURI = "http://localhost:" + wireMockServer.port();
        this.response =RestAssured.given().when().get("/search/repositories?q=octocat");
    }

    @When("The user searches for Github repository with keyword {string}")
    public void the_user_searches_for_github_repository_with_keyword(String queryParam) {
        if(queryParam != null)
            this.response = RestAssured.given().queryParam("q", queryParam).get("/repositories");
        else
            this.response = RestAssured.given().queryParam("q","  ").get("/repositories");
    }

    @When("The user searches for {string} with sort key {string} in the order {string}")
    public void the_user_searches_for_with_sort_key_in_the_order(String queryParam, String sort, String order) {
        if(!order.equals(null))
            this.response = RestAssured.given().queryParam("q",queryParam).queryParam("sort",sort).queryParam("order",order).get("/repositories");
        else
            this.response = RestAssured.given().queryParam("q",queryParam).queryParam("sort",sort).get("/repositories");
    }
    @When("The user searches for Github repository with invalid keyword")
    public void searchRepoWithInvalidQuery(){
        RestAssured.baseURI = "http://localhost:" + wireMockServer.port();
        this.response =RestAssured.given().get("/search/repositories?q=invalid_query");
    }

    @When("The user searches for a repository with keyword {string} with page number {int} and page size {int}")
    public void the_user_searches_for_a_repository_with_keyword_forks_with_page_number_and_page_size(String queryParam, int pageNumber, int pageSize) {
        this.response = RestAssured.given()
                .queryParam("q",queryParam)
                .queryParam("page",pageNumber)
                .queryParam("per_page",pageSize)
                .when().get("/repositories");
    }
    @Then("The Search Repository API returns a list of repository within {int} with status {int}")
    public void the_search_repository_api_returns_a_list_of_repository_within_the_page_limit(int pageSize,int statusCode) {
        assertEquals(this.response.getStatusCode(),statusCode);
        assertNotNull(this.response.body());
        assertEquals(response.getBody().jsonPath().getList("items").size(),pageSize);
    }

    @Then("The Search Repository API returns successful response with status code {int} with expected {string} in the {string}")
    public void the_search_repository_api_returns_successful_response_with_status_code(Integer statusCode,String expectedValue,String keyPath) {
        assertEquals(this.response.getStatusCode(),statusCode.intValue());
        assertNotNull(this.response.body());
        assertEquals( this.response.path(keyPath).toString(),expectedValue);
    }

    @Then("The Search Repository API returns response with empty search item")
    public void verify_empty_list_for_nonexistent_repo(){
        assertEquals(this.response.getStatusCode(),200);
        assertEquals(this.response.path("total_count"),Integer.valueOf(0));
    }

    @Then("The Search Repository API returns successful response with status code {int} matching the repo size")
    public void verify_repo_with_size(int statusCode){
        assertEquals(this.response.getStatusCode(),statusCode);
        assertNotNull(this.response.body());
        assertTrue(this.response.jsonPath().getList("items").size() > 0);
    }

    @Then("The Search Repository API returns successful response with status code {int} matching the {string} and {string} count")
    public void the_search_repository_api_returns_successful_response_with_status_code_matching_the_criteria(int statusCode,String language,String starCount) {
        String count[] = starCount.split((","));
        int[] starCountArray = new int[count.length];
        for(int i =0;i< starCountArray.length;i++){
            starCountArray[i]= Integer.parseInt(count[i]);
        }
        assertEquals(this.response.getStatusCode(),statusCode);
        assertNotNull(this.response.body());
        this.response.prettyPrint();
        List<Map<String,Object>> items = this.response.jsonPath().getList("items");
        for(Map<String ,Object> item : items){
            String itemLanguage = (String) item.get("language");
            int stars = ((Number)item.get("stargazers_count")).intValue();
            assertEquals(itemLanguage,language);
            if(starCountArray.length ==1)
                assertTrue(stars > starCountArray[0]);
            else if(starCountArray.length ==2){
                assertTrue(stars >= starCountArray[0] && stars <=starCountArray[1]);
            }
        }
    }

    @Then("The Search Repository displays the filtered results with {string} and {string}")
    public void the_search_repository_displays_the_filtered_results_with_and(String date1,String date2) {
        List<Map<String,String>> items = this.response.jsonPath().getList("items");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDate create_date = LocalDate.parse(date1);
        LocalDate push_date = LocalDate.parse(date2);

        for(Map<String,String> item : items) {
            LocalDate createdDate = LocalDate.parse(item.get("created_at"), formatter);
            LocalDate pushedDate = LocalDate.parse(item.get("pushed_at"), formatter);
            assertTrue(createdDate.isAfter(create_date));
            assertTrue(pushedDate.isAfter(push_date));
        }
    }

    @Then("The Search Repository API returns successful response with status {int} and displays results in the given {string}")
    public void the_search_repository_api_returns_successful_response_with_status_and_displays_results_in_the_given_order(int statusCode,String order) {
        assertEquals(this.response.getStatusCode(),statusCode);
        assertNotNull(this.response.body());
        assertTrue(this.response.jsonPath().getString("items[0].owner.login").equals("shalini171993"));
        List<Integer> stars = this.response.jsonPath().getList("items.stargazers_count");

        if(!order.equals("asc")){
            for(int i =0;i<stars.size()-1;i++){
                assertTrue(stars.get(i) >= stars.get(i+1));
            }
        }
        else{
            for(int i =0;i<stars.size()-1;i++){
                assertTrue(stars.get(i) <= stars.get(i+1));
            }
        }
    }
    @Then("The user validates if the response body size is within the acceptable limit {int}")
    public void the_user_validates_if_the_response_body_size_is_within_the_acceptable_limit(int limit) {
            assertTrue(response.getBody().asByteArray().length < limit);
    }

//    @Then("The Search Repository API returns validation failed error message with status {int}")
//    public void the_search_repository_api_returns_validation_failed_error_message_with_status(int statusCode) {
//        assertEquals(this.response.getStatusCode(),statusCode);
//        assertTrue(this.response.getBody().asString().contains("Validation Failed"));
//        wireMockServer.stop();
//    }
//
//    @Then("The Search Repository API returns status code {int}")
//    public void the_search_repository_api_returns_status_code(int statusCode) {
//        int rateLimitRemaining = Integer.parseInt(this.response.getHeader("X-RateLimit-Remaining"));
//        assertEquals(rateLimitRemaining,0);
//        assertEquals(this.response.getStatusCode(),statusCode);
//        assertTrue(this.response.getBody().asString().contains("Rate Limit is exceeded"));
//        wireMockServer.stop();
//    }

}