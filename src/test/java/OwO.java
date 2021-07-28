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

  void testNumber(Double n, String json) {
    Value value = new Value(Type.FALSE);
    assertEquals(Result.OK, Nyaa.parse(value, json));
    assertEquals(Type.NUMBER, value.getType());
    assertEquals(n, value.getNumber());
  }

  @Test
  void testParseNumber() {
    testNumber(1.0, "1");
    testNumber(-1.0, "-1");
    testNumber(1.5, "1.5");
    testNumber(-1.5, "-1.5");
    testNumber(3.1416, "3.1416");
    testNumber(1E10, "1E10");
    testNumber(1e10, "1e10");
    testNumber(1E+10, "1E+10");
    testNumber(1E-10, "1E-10");
    testNumber(-1E10, "-1E10");
    testNumber(-1e10, "-1e10");
    testNumber(-1E+10, "-1E+10");
    testNumber(-1E-10, "-1E-10");
    testNumber(1.234E+10, "1.234E+10");
    testNumber(1.234E-10, "1.234E-10");
    testNumber(0.0, "1e-10000");
    testNumber(1.234E+10, "1.234E+10");
    testNumber(1.234E-10, "1.234E-10");

    testNumber(1.0000000000000002, "1.0000000000000002"); /* the smallest number > 1 */
    testNumber(4.9406564584124654e-324, "4.9406564584124654e-324"); /* minimum denormal */
    testNumber(-4.9406564584124654e-324, "-4.9406564584124654e-324");
    testNumber(2.2250738585072009e-308, "2.2250738585072009e-308");  /* Max subnormal double */
    testNumber(-2.2250738585072009e-308, "-2.2250738585072009e-308");
    testNumber(2.2250738585072014e-308, "2.2250738585072014e-308");  /* Min normal positive double */
    testNumber(-2.2250738585072014e-308, "-2.2250738585072014e-308");
    testNumber(1.7976931348623157e+308, "1.7976931348623157e+308");  /* Max double */
    testNumber(-1.7976931348623157e+308, "-1.7976931348623157e+308");
  }

  void testError(Result result, String json) {
    Value value = new Value(Type.FALSE);
    assertEquals(result, Nyaa.parse(value, json));
    assertEquals(Type.NULL, Nyaa.getType(value));
  }

  @Test
  void testParseExceptValue() {
    testError(Result.EXCEPT_VALUE, "");
    testError(Result.EXCEPT_VALUE, " ");
  }

  @Test
  void testParseInvalidValue() {
    testError(Result.INVALID_VALUE, "nul");
    testError(Result.INVALID_VALUE, "?");

    /* invalid number */
    testError(Result.INVALID_VALUE, ".123");
    testError(Result.INVALID_VALUE, "+0");
    testError(Result.INVALID_VALUE, "+1");
    testError(Result.INVALID_VALUE, ".123"); /* at least one digit before '.' */
    testError(Result.INVALID_VALUE, "1.");   /* at least one digit after '.' */
    testError(Result.INVALID_VALUE, "INF");
    testError(Result.INVALID_VALUE, "inf");
    testError(Result.INVALID_VALUE, "NAN");
    testError(Result.INVALID_VALUE, "nan");
  }

  @Test
  void testParseRootNotSingular() {
    testError(Result.ROOT_NOT_SINGULAR, "null x");

    /* invalid number */
    testError(Result.ROOT_NOT_SINGULAR, "0123");
    testError(Result.ROOT_NOT_SINGULAR, "0x0");
    testError(Result.ROOT_NOT_SINGULAR, "0x123");
  }

  @Test
  void testNumberTooBig() {
    testError(Result.NUMBER_TOO_BIG, "1e309");
    testError(Result.NUMBER_TOO_BIG, "-1e309");
  }
}
