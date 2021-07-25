import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OwO {

  @Test
  void testParseNull() {
    Value value = new Value(Type.FALSE);
    assertEquals(Result.OK, Nyaa.parse(value, " null"));
    assertEquals(Type.NULL, value.getType());
  }

  @Test
  void testParseTrue() {
    Value value = new Value(Type.FALSE);
    assertEquals(Result.OK, Nyaa.parse(value, "true"));
    assertEquals(Type.TRUE, value.getType());
  }

  @Test
  void testParseFalse() {
    Value value = new Value(Type.TRUE);
    assertEquals(Result.OK, Nyaa.parse(value, " false "));
    assertEquals(Type.FALSE, value.getType());
  }

  @Test
  void testParseExceptValue() {
    Value value = new Value(Type.FALSE);
    assertEquals(Result.EXCEPT_VALUE, Nyaa.parse(value, ""));
    assertEquals(Type.NULL, Nyaa.getType(value));

    value.setType(Type.FALSE);
    assertEquals(Result.EXCEPT_VALUE, Nyaa.parse(value, " "));
    assertEquals(Type.NULL, Nyaa.getType(value));
  }

  @Test
  void testParseInvalidValue() {
    Value value = new Value(Type.FALSE);
    assertEquals(Result.INVALID_VALUE, Nyaa.parse(value, " nul"));
    assertEquals(Type.NULL, Nyaa.getType(value));

    value.setType(Type.FALSE);
    assertEquals(Result.INVALID_VALUE, Nyaa.parse(value, "?"));
    assertEquals(Type.NULL, Nyaa.getType(value));
  }

  @Test
  void testParseRootNotSingular() {
    Value value = new Value(Type.FALSE);
    assertEquals(Result.ROOT_NOT_SINGULAR, Nyaa.parse(value, " null x"));
    assertEquals(Type.NULL, Nyaa.getType(value));
  }
}
