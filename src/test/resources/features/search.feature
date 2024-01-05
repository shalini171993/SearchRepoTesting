Feature: Search

  Scenario Outline: Search repo with different query parameters

    Given The user sets the baseURL for the Search API
    When The user searches for Github repository with keyword "<queryParam>"
    Then The Search Repository API returns successful response with status code 200 with expected "<searchResult>" in the "<responseKey>"

    Examples:
    |queryParam                   |responseKey                 |searchResult |
    |Java                         |items[0].language           |Java         |
    |PyTHOn                       |items[0].language           |Python       |
    |user:shalini171993           |total_count                 |13           |
    |size:1024                    |items[0].size               |1024         |

  Scenario Outline: Search repo with repository size query parameters and validating the entire list

    Given The user sets the baseURL for the Search API
    When The user searches for Github repository with keyword "<queryParam>"
    Then The Search Repository API returns successful response with status code 200 matching the repo size

    Examples:
      |queryParam                   |
      |size:100000                  |

  Scenario Outline: Search repo with multiple query parameters

    Given The user sets the baseURL for the Search API
    When The user searches for Github repository with keyword "<queryParam>"
    Then The Search Repository API returns successful response with status code 200 matching the "<language>" and "<star>" count

    Examples:
      |queryParam                     |language| star|
      |language:assembly stars:>10000 |Assembly|10000|

  Scenario Outline: Search repo with query parameters having repository language,starred count and timestamps

    Given The user sets the baseURL for the Search API
    When The user searches for Github repository with keyword "<queryParam>"
    Then The Search Repository API returns successful response with status code 200 matching the "<language>" and "<star>" count
    And The Search Repository displays the filtered results with "<createDate>" and "<pushDate>"

    Examples:
      |queryParam                                                          |createDate | pushDate |language|star|
      |language:assembly stars:1..5 created:>2024-01-01 pushed:>2024-01-01 |2024-01-01 |2024-01-01|Assembly|1,5 |

  Scenario Outline: Search with queries and sort
    Given The user sets the baseURL for the Search API
    When The user searches for "<queryParam>" with sort key "<key>" in the order "<order>"
    Then The Search Repository API returns successful response with status <code> and displays results in the given "<order>"

    Examples:
    |queryParam                |key  |order|code|
    |user:shalini171993        |stars|asc  |200 |
    |user:shalini171993        |stars|desc |200 |
    |user:shalini171993        |stars|null |200 |

  Scenario Outline: Search Repository API with pagination parameters

    Given The user sets the baseURL for the Search API
    When The user searches for a repository with keyword "<queryParam>" with page number <page> and page size <pageSize>
    Then The Search Repository API returns a list of repository within <pageSize> with status <statusCode>

    Examples:
      |queryParam|page|pageSize|statusCode|
      |forks:>100|1   |   5    |200       |

  Scenario Outline: Validate if the response body size is within certain limits
    Given The user sets the baseURL for the Search API
    When The user searches for Github repository with keyword "<queryParam>"
    Then The user validates if the response body size is within the acceptable limit 100000

    Examples:
      |queryParam         |
      |user:shalini171993 |

  Scenario Outline:  Search repo that don't exist

    Given The user sets the baseURL for the Search API
    When The user searches for Github repository with keyword "<queryParam>"
    Then The Search Repository API returns response with empty search item
    Examples:
    |queryParam           |
    |sedrfgypoiu          |
    |!@#$%^&*()           |

#    Scenario: Search Repository with invalid query and verify the validation error
#      Given the mock server is set up to return validation error for an invalid query
#      When The user searches for Github repository with invalid keyword
#      Then The Search Repository API returns validation failed error message with status 422
#
#
#  Scenario: Search Repository API exceeded the ratelimit
#    Given The mock server is setup to set the rate limit value as 0
#    When The user hits the Search Repository API with exceeded rate limit value
#    Then The Search Repository API returns status code 429


