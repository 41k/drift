package drift.functional

import drift.dto.*
import drift.model.User
import groovy.json.JsonOutput
import spock.lang.Unroll

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER
import static drift.util.TestConstants.*
import static io.restassured.RestAssured.given
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED

class SearchControllerFunctionalTest extends BaseFunctionalTest {

    // --- Users search

    @Unroll
    def 'should perform users search [#criteria] successfully'() {
        given:
        userRepository.saveAllAndFlush([
                User.builder().id('user-1').email('email-1').password('pwd').active(true).build(),
                User.builder().id('user-2').email('email-2').password('pwd').active(false).build(),
                User.builder().id('user-3').email('email-3').password('pwd').active(false).build(),
                User.builder().id('user-4').email('email-4').password('pwd').active(true).build()
        ])

        and:
        def requestBodyAsJson = JsonOutput.toJson(requestBody)

        when:
        def searchResult = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(requestBodyAsJson)
                .when()
                .post("$BASE_SEARCH_API_URL/users")
                .then()
                .statusCode(SC_OK)
                .extract().body().as(UserDto[])

        then:
        def ids = (searchResult as UserDto[]).collect({ it.id })
        ids.size() == expectedIds.size()
        expectedIds.forEach({ id -> ids.contains(id) })

        where:
        criteria            | requestBody                            | expectedIds
        'by user ids'       | ['userIds':['user-1','user-3']]        | ['user-1','user-3']
        'by active flag'    | ['active':true]                        | ['user-1','user-4']
        'by empty criteria' | [:]                                    | ['user-1','user-2','user-3','user-4']
    }

    def 'should not perform users search in case of request without valid access token'() {
        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post("$BASE_SEARCH_API_URL/users")
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- Cars search

    @Unroll
    def 'should perform cars search [#criteria] successfully'() {
        given:
        carRepository.saveAllAndFlush([
                CAR.toBuilder().id('car-1').ownerId('user-1').active(true).build(),
                CAR.toBuilder().id('car-2').ownerId('user-1').active(false).build(),
                CAR.toBuilder().id('car-3').ownerId('user-2').active(false).build(),
                CAR.toBuilder().id('car-4').ownerId('user-2').active(true).build(),
                CAR.toBuilder().id('car-5').ownerId('user-3').active(false).build()
        ])

        and:
        def requestBodyAsJson = JsonOutput.toJson(requestBody)

        when:
        def searchResult = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(requestBodyAsJson)
                .when()
                .post("$BASE_SEARCH_API_URL/cars")
                .then()
                .statusCode(SC_OK)
                .extract().body().as(CarDto[])

        then:
        def ids = (searchResult as CarDto[]).collect({ it.id })
        ids.size() == expectedIds.size()
        expectedIds.forEach({ id -> ids.contains(id) })

        where:
        criteria            | requestBody                            | expectedIds
        'by car ids'        | ['carIds':['car-1','car-4','car-5']]   | ['car-1','car-4','car-5']
        'by owner ids'      | ['ownerIds':['user-1','user-3']]       | ['car-1','car-2','car-5']
        'by active flag'    | ['active':true]                        | ['car-1','car-4']
        'by empty criteria' | [:]                                    | ['car-1','car-2','car-3','car-4','car-5']
    }

    def 'should not perform cars search in case of request without valid access token'() {
        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post("$BASE_SEARCH_API_URL/cars")
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- Organisations search

    @Unroll
    def 'should perform organisations search [#criteria] successfully'() {
        given:
        organisationRepository.saveAllAndFlush([
                ORGANISATION.toBuilder().id('org-1').ownerId('user-1').name('org-1').active(true).build(),
                ORGANISATION.toBuilder().id('org-2').ownerId('user-1').name('org-2').active(false).build(),
                ORGANISATION.toBuilder().id('org-3').ownerId('user-2').name('org-3').active(false).build(),
                ORGANISATION.toBuilder().id('org-4').ownerId('user-2').name('org-4').active(true).build(),
                ORGANISATION.toBuilder().id('org-5').ownerId('user-3').name('org-5').active(false).build()
        ])

        and:
        def requestBodyAsJson = JsonOutput.toJson(requestBody)

        when:
        def searchResult = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(requestBodyAsJson)
                .when()
                .post("$BASE_SEARCH_API_URL/organisations")
                .then()
                .statusCode(SC_OK)
                .extract().body().as(OrganisationDto[])

        then:
        def ids = (searchResult as OrganisationDto[]).collect({ it.id })
        ids.size() == expectedIds.size()
        expectedIds.forEach({ id -> ids.contains(id) })

        where:
        criteria              | requestBody                                   | expectedIds
        'by organisation ids' | ['organisationIds':['org-1','org-4','org-5']] | ['org-1','org-4','org-5']
        'by owner ids'        | ['ownerIds':['user-1','user-3']]              | ['org-1','org-2','org-5']
        'by active flag'      | ['active':true]                               | ['org-1','org-4']
        'by empty criteria'   | [:]                                           | ['org-1','org-2','org-3','org-4','org-5']
    }

    def 'should not perform organisations search in case of request without valid access token'() {
        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post("$BASE_SEARCH_API_URL/organisations")
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- Championships search

    @Unroll
    def 'should perform championships search [#criteria] successfully'() {
        given:
        championshipRepository.saveAllAndFlush([
                CHAMPIONSHIP.toBuilder().id('champ-1').ownerId('user-1').organisationId('org-1').active(true).build(),
                CHAMPIONSHIP.toBuilder().id('champ-2').ownerId('user-1').organisationId('org-2').active(false).build(),
                CHAMPIONSHIP.toBuilder().id('champ-3').ownerId('user-2').organisationId('org-3').active(false).build(),
                CHAMPIONSHIP.toBuilder().id('champ-4').ownerId('user-2').organisationId('org-3').active(true).build(),
                CHAMPIONSHIP.toBuilder().id('champ-5').ownerId('user-3').organisationId('org-4').active(false).build()
        ])

        and:
        def requestBodyAsJson = JsonOutput.toJson(requestBody)

        when:
        def searchResult = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(requestBodyAsJson)
                .when()
                .post("$BASE_SEARCH_API_URL/championships")
                .then()
                .statusCode(SC_OK)
                .extract().body().as(ChampionshipDto[])

        then:
        def ids = (searchResult as ChampionshipDto[]).collect({ it.id })
        ids.size() == expectedIds.size()
        expectedIds.forEach({ id -> ids.contains(id) })

        where:
        criteria              | requestBody                                         | expectedIds
        'by championship ids' | ['championshipIds':['champ-1','champ-4','champ-5']] | ['champ-1','champ-4','champ-5']
        'by owner ids'        | ['ownerIds':['user-1','user-3']]                    | ['champ-1','champ-2','champ-5']
        'by organisation ids' | ['organisationIds':['org-2','org-3']]               | ['champ-2','champ-3','champ-4']
        'by discipline'       | ['discipline':DISCIPLINE]                           | ['champ-1','champ-2','champ-3','champ-4','champ-5']
        'by active flag'      | ['active':true]                                     | ['champ-1','champ-4']
        'by empty criteria'   | [:]                                                 | ['champ-1','champ-2','champ-3','champ-4','champ-5']
    }

    def 'should not perform championships search in case of request without valid access token'() {
        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post("$BASE_SEARCH_API_URL/championships")
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- Championship stages search

    @Unroll
    def 'should perform championship stages search [#criteria] successfully'() {
        given:
        championshipStageRepository.saveAllAndFlush([
                CHAMPIONSHIP_STAGE.toBuilder().id('stage-1').championshipId('champ-1').active(true).build(),
                CHAMPIONSHIP_STAGE.toBuilder().id('stage-2').championshipId('champ-1').active(false).build(),
                CHAMPIONSHIP_STAGE.toBuilder().id('stage-3').championshipId('champ-2').active(false).build(),
                CHAMPIONSHIP_STAGE.toBuilder().id('stage-4').championshipId('champ-2').active(true).build(),
                CHAMPIONSHIP_STAGE.toBuilder().id('stage-5').championshipId('champ-3').active(false).build()
        ])

        and:
        def requestBodyAsJson = JsonOutput.toJson(requestBody)

        when:
        def searchResult = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(requestBodyAsJson)
                .when()
                .post("$BASE_SEARCH_API_URL/championship-stages")
                .then()
                .statusCode(SC_OK)
                .extract().body().as(ChampionshipStageDto[])

        then:
        def ids = (searchResult as ChampionshipStageDto[]).collect({ it.id })
        ids.size() == expectedIds.size()
        expectedIds.forEach({ id -> ids.contains(id) })

        where:
        criteria                    | requestBody                                              | expectedIds
        'by championship stage ids' | ['championshipStageIds':['stage-1','stage-4','stage-5']] | ['stage-1','stage-4','stage-5']
        'by championship ids'       | ['championshipIds':['champ-2','champ-3']]                | ['stage-3','stage-4','stage-5']
        'by active flag'            | ['active':true]                                          | ['stage-1','stage-4']
        'by empty criteria'         | [:]                                                      | ['stage-1','stage-2','stage-3','stage-4','stage-5']
    }

    def 'should not perform championship stages search in case of request without valid access token'() {
        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post("$BASE_SEARCH_API_URL/championship-stages")
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }


    // --- Trainings search

    @Unroll
    def 'should perform trainings search [#criteria] successfully'() {
        given:
        trainingRepository.saveAllAndFlush([
                TRAINING.toBuilder().id('tr-1').ownerId('user-1').organisationId('org-1').active(true).build(),
                TRAINING.toBuilder().id('tr-2').ownerId('user-2').organisationId('org-2').active(false).build(),
                TRAINING.toBuilder().id('tr-3').ownerId('user-2').organisationId('org-3').active(false).build(),
                TRAINING.toBuilder().id('tr-4').ownerId('user-3').organisationId('org-4').active(true).build(),
                TRAINING.toBuilder().id('tr-5').ownerId('user-3').organisationId('org-4').active(false).build()
        ])

        and:
        def requestBodyAsJson = JsonOutput.toJson(requestBody)

        when:
        def searchResult = given()
                .header(ACCESS_TOKEN_HEADER, accessTokenForUser1)
                .contentType(JSON_CONTENT_TYPE)
                .body(requestBodyAsJson)
                .when()
                .post("$BASE_SEARCH_API_URL/trainings")
                .then()
                .statusCode(SC_OK)
                .extract().body().as(TrainingDto[])

        then:
        def ids = (searchResult as TrainingDto[]).collect({ it.id })
        ids.size() == expectedIds.size()
        expectedIds.forEach({ id -> ids.contains(id) })

        where:
        criteria              | requestBody                            | expectedIds
        'by training ids'     | ['trainingIds':['tr-1','tr-4','tr-5']] | ['tr-1','tr-4','tr-5']
        'by owner ids'        | ['ownerIds':['user-2','user-3']]       | ['tr-2','tr-3','tr-4','tr-5']
        'by organisation ids' | ['organisationIds':['org-2','org-3']]  | ['tr-2','tr-3']
        'by active flag'      | ['active':true]                        | ['tr-1','tr-4']
        'by empty criteria'   | [:]                                    | ['tr-1','tr-2','tr-3','tr-4','tr-5']
    }

    def 'should not perform trainings search in case of request without valid access token'() {
        expect:
        given()
                .contentType(JSON_CONTENT_TYPE)
                .body('{}')
                .when()
                .post("$BASE_SEARCH_API_URL/trainings")
                .then()
                .statusCode(SC_UNAUTHORIZED)
    }
}
