package drift.configuration.properties

import drift.model.ImageCategory
import spock.lang.Specification

import static drift.util.TestConstants.PATH

class StaticContentStoragePropertiesTest extends Specification {

    private properties = new StaticContentStorageProperties([
            (ImageCategory.USER.key): PATH
    ])

    def 'should retrieve image directory path successfully'() {
        expect:
        properties.getImageDirectoryPath(ImageCategory.USER) == PATH
    }

    def 'should throw exception during image directory path retrieval if image category is not configured'() {
        when:
        properties.getImageDirectoryPath(ImageCategory.CAR)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Images directory is not configured for key=$ImageCategory.CAR.key"
    }

    def 'should retrieve image url pattern successfully'() {
        expect:
        properties.getImageUrlPattern(ImageCategory.USER) == "$PATH**"
    }

    def 'should throw exception during image url pattern retrieval if image category is not configured'() {
        when:
        properties.getImageUrlPattern(ImageCategory.CAR)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Images directory is not configured for key=$ImageCategory.CAR.key"
    }
}
