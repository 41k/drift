package drift.repository;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Converter
public class ListOfDoublesToStringConverter implements AttributeConverter<List<Double>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<Double> entityAttributeValue) {
        if (entityAttributeValue == null) return null;
        return entityAttributeValue.stream().map(Object::toString).collect(Collectors.joining(DELIMITER));
    }

    @Override
    public List<Double> convertToEntityAttribute(String dbColumnValue) {
        if (dbColumnValue == null) return null;
        return Stream.of(dbColumnValue.split(DELIMITER)).map(Double::valueOf).collect(Collectors.toList());
    }

}
