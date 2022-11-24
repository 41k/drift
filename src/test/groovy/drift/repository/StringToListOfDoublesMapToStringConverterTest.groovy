package drift.repository

import spock.lang.Specification

class StringToListOfDoublesMapToStringConverterTest extends Specification {

    def 'should convert correctly'() {
        given:
        def converter = new StringToListOfDoublesMapToStringConverter()

        expect:
        converter.convertToDatabaseColumn(entityAttributeValue) == dbColumnValue

        and:
        converter.convertToEntityAttribute(dbColumnValue) == entityAttributeValue

        where:
        entityAttributeValue                              || dbColumnValue
        ['1':[1.2d,5.5d],'2':[3.35d],'3':[9d,6.3d,7.45d]] || '1:1.2,5.5__2:3.35__3:9.0,6.3,7.45'
        null                                              || null
    }
}
