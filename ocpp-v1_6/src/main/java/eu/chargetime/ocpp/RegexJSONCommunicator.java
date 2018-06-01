package eu.chargetime.ocpp;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Alternative implementation for the JSONCommunicator that replaces the Calendar deserialization with a regex-based
 * implementation. This can properly handle variable number of digits for the fraction-of-a-second part of a UTC
 * timestamp (ie. it doesn't assume it will be 3 digits), assuming the timestamp is otherwise valid.
 * @author Adam Bankeszi {@literal <abankeszi@monguz.hu>}
 */
public class RegexJSONCommunicator extends JSONCommunicator {

  public RegexJSONCommunicator(Radio radio) {
    super(radio);
  }

  @Override
  public <T> T unpackPayload(Object payload, Class<T> type) throws Exception {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Calendar.class, new RegexCalendarDeserializer());
    Gson gson = builder.create();
    return gson.fromJson(payload.toString(), type);
  }

  private static class RegexCalendarDeserializer implements JsonDeserializer<Calendar> {

    private static final Pattern UTC_DATE_PATTERN =
      Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2}).([0-9]+)Z");

    @Override
    public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
      String dateString = json.getAsJsonPrimitive().getAsString();

      try {
        Matcher matcher = UTC_DATE_PATTERN.matcher(dateString);

        if (!matcher.find()) {
          throw new IllegalArgumentException(dateString);
        }

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(matcher.group(1)));
        calendar.set(Calendar.MONTH, Integer.parseInt(matcher.group(2)) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(matcher.group(3)));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(matcher.group(4)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(matcher.group(5)));
        calendar.set(Calendar.SECOND, Integer.parseInt(matcher.group(6)));
        calendar.set(Calendar.MILLISECOND, Integer.parseInt(zeroPad(matcher.group(7))));

        return calendar;
      } catch (Exception e) {
        throw new JsonParseException("Failed to parse date: " + dateString, e);
      }
    }

    private String zeroPad(String msString) {
      char[] paddedBytes = { '0', '0', '0' };

      for (int i = 0; i < 3 && i < msString.length(); i++) {
        paddedBytes[i] = msString.charAt(i);
      }

      return String.valueOf(paddedBytes);
    }

  }

}
