package drift.repository;

import org.apache.commons.lang3.tuple.Pair;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Converter
public class StringToListOfDoublesMapToStringConverter implements AttributeConverter<Map<String, List<Double>>, String> {

    private static final String LIST_VALUES_DELIMITER = ",";
    private static final String KEY_VALUE_DELIMITER = ":";
    private static final String ENTRIES_DELIMITER = "__";

    @Override
    public String convertToDatabaseColumn(Map<String, List<Double>> entityAttributeValue) {
        if (entityAttributeValue == null) return null;
        return entityAttributeValue.entrySet().stream()
                .map(entry -> entry.getKey() +
                        KEY_VALUE_DELIMITER +
                        entry.getValue().stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(LIST_VALUES_DELIMITER)))
                .collect(Collectors.joining(ENTRIES_DELIMITER));
    }

    @Override
    public Map<String, List<Double>> convertToEntityAttribute(String dbColumnValue) {
        if (dbColumnValue == null) return null;
        return Stream.of(dbColumnValue.split(ENTRIES_DELIMITER))
                .map(entryAsString -> entryAsString.split(KEY_VALUE_DELIMITER))
                .map(entry -> Pair.of(
                        entry[0],
                        Stream.of(entry[1].split(LIST_VALUES_DELIMITER)).map(Double::valueOf).collect(Collectors.toList())
                ))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
}
