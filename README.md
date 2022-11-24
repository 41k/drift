## Local setup and run

1. Install and launch docker. 
2. Install IntelliJ IDEA, open the project and wait for completion of dependencies download.
3. Run `src/test/groovy/drift/LocalDevApplicationRunner.groovy`
4. Open API docs: `http://localhost:8080/swagger-ui.html`

## Dev dataset

Dataset for local development can be found in `src/main/resources/db-migration/changes/dev-dataset.yml`
Password for all predefined users from the dataset is `pwd`

## Before commit

Run `mvn clean verify` command on the project's root folder and make sure that all tests passed and build is successful.