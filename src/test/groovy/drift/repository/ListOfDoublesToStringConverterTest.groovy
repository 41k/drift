package drift.repository

import spock.lang.Specification
import spock.lang.Unroll

class ListOfDoublesToStringConverterTest extends Specification {

    @Unroll
    def 'should convert correctly'() {
        given:
        def converter = new ListOfDoublesToStringConverter()

        expect:
        converter.convertToDatabaseColumn(entityAttributeValue) == dbColumnValue

        and:
        converter.convertToEntityAttribute(dbColumnValue) == entityAttributeValue

        where:
        entityAttributeValue || dbColumnValue
        [9d,6.3d,7.45d]      || '9.0,6.3,7.45'
        null                 || null
    }
}
