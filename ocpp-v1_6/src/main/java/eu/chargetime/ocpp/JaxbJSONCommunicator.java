package eu.chargetime.ocpp;

import java.lang.reflect.Type;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

public class JaxbJSONCommunicator extends JSONCommunicator {

  public JaxbJSONCommunicator(Radio radio) {
    super(radio);
  }

  @Override
  public <T> T unpackPayload(Object payload, Class<T> type) throws Exception {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Calendar.class, new JaxbCalendarDeserializer());
    Gson gson = builder.create();
    return gson.fromJson(payload.toString(), type);
  }

  private static class JaxbCalendarDeserializer implements JsonDeserializer<Calendar> {

    @Override
    public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
      String dateString = json.getAsJsonPrimitive().getAsString();
      return DatatypeConverter.parseDateTime(dateString);
    }

  }

}
