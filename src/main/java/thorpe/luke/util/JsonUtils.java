package thorpe.luke.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonUtils {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

  public static String toJson(Object object) throws JsonException {
    ObjectWriter objectWriter = OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
    try {
      return objectWriter.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new JsonException(e);
    }
  }

  public static <T> T fromJson(String json, Class<T> clazz) throws JsonException {
    ObjectReader objectReader = OBJECT_MAPPER.reader().forType(clazz);
    try {
      return objectReader.readValue(json);
    } catch (JsonProcessingException e) {
      throw new JsonException(e);
    }
  }
}
