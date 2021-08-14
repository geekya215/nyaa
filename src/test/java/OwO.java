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

  void testString(String except, String json) {
    Value value = new Value(Type.NULL);
    assertEquals(Result.OK, Nyaa.parse(value, json));
    assertEquals(Type.STRING, Nyaa.getType(value));
    assertEquals(except, Nyaa.getString(value));
  }

  @Test
  void testParseString() {
    testString("", "\"\"");
    testString("Hello", "\"Hello\"");
    testString("Hello\nWorld", "\"Hello\\nWorld\"");
    testString("Hello\0World", "\"Hello\\u0000World\"");
    testString("\" \\ / \b \f \n \r \t", "\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t\"");
    testString("\u0024", "\"\\u0024\"");         /* Dollar sign U+0024 */
    testString("\u00C2\u00A2", "\"\\u00A2\"");     /* Cents sign U+00A2 */
    testString("\u00E2\u0082\u00AC", "\"\\u20AC\""); /* Euro sign U+20AC */
    testString("\u00F0\u009D\u0084\u009E", "\"\\uD834\\uDD1E\"");  /* G clef sign U+1D11E */
    testString("\u00F0\u009D\u0084\u009E", "\"\\ud834\\udd1e\"");  /* G clef sign U+1D11E */
  }

  @Test
  void testParseArray() {
    Value v1 = new Value();
    assertEquals(Result.OK, Nyaa.parse(v1, "[ ]"));
    assertEquals(Type.ARRAY, Nyaa.getType(v1));
    assertEquals(0, Nyaa.getArraySize(v1));

    Value v2 = new Value();
    assertEquals(Result.OK, Nyaa.parse(v2, "[ null , false , true , 123 , \"abc\" ]"));
    assertEquals(Type.ARRAY, Nyaa.getType(v2));
    assertEquals(5, Nyaa.getArraySize(v2));
    assertEquals(Type.NULL, Nyaa.getType(Nyaa.getArrayElement(v2, 0)));
    assertEquals(Type.FALSE, Nyaa.getType(Nyaa.getArrayElement(v2, 1)));
    assertEquals(Type.TRUE, Nyaa.getType(Nyaa.getArrayElement(v2, 2)));
    assertEquals(Type.NUMBER, Nyaa.getType(Nyaa.getArrayElement(v2, 3)));
    assertEquals(Type.STRING, Nyaa.getType(Nyaa.getArrayElement(v2, 4)));
    assertEquals(123.0, Nyaa.getNumber(Nyaa.getArrayElement(v2, 3)));
    assertEquals("abc", Nyaa.getString(Nyaa.getArrayElement(v2, 4)));

    Value v3 = new Value();
    assertEquals(Result.OK, Nyaa.parse(v3, "[ [ ] , [ 0 ] , [ 0 , 1 ] , [ 0 , 1 , 2 ] ]"));
    assertEquals(Type.ARRAY, Nyaa.getType(v3));
    assertEquals(4, Nyaa.getArraySize(v3));
    for (int i = 0; i < 4; i++) {
      Value v = Nyaa.getArrayElement(v3, i);
      assertEquals(Type.ARRAY, Nyaa.getType(v));
      assertEquals(i, Nyaa.getArraySize(v));
      for (int j = 0; j < i; j++) {
        Value e = Nyaa.getArrayElement(v, j);
        assertEquals(Type.NUMBER, Nyaa.getType(e));
        assertEquals(j, Nyaa.getNumber(e));
      }
    }
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
  void testParseNumberTooBig() {
    testError(Result.NUMBER_TOO_BIG, "1e309");
    testError(Result.NUMBER_TOO_BIG, "-1e309");
  }

  @Test
  void testParseMissQuotationMark() {
    testError(Result.MISS_QUOTATION_MARK, "\"");
    testError(Result.MISS_QUOTATION_MARK, "\"abc");
  }

  @Test
  void testParseInvalidStringEscape() {
    testError(Result.INVALID_STRING_ESCAPE, "\"\\v\"");
    testError(Result.INVALID_STRING_ESCAPE, "\"\\'\"");
    testError(Result.INVALID_STRING_ESCAPE, "\"\\0\"");
    testError(Result.INVALID_STRING_ESCAPE, "\"\\x12\"");
  }

  @Test
  void testParseInvalidStringChar() {
    testError(Result.INVALID_STRING_CHAR, "\"\u0001\"");
    testError(Result.INVALID_STRING_CHAR, "\"\u001F\"");
  }

  @Test
  void testParseInvalidUnicodeHex() {
    testError(Result.INVALID_UNICODE_HEX, "\"\\u\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\u0\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\u01\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\u012\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\u/000\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\uG000\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\u0/00\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\u0G00\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\u00/0\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\u00G0\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\u000/\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\u000G\"");
    testError(Result.INVALID_UNICODE_HEX, "\"\\u 123\"");
  }

  @Test
  void testParseInvalidUnicodeSurrogate() {
    testError(Result.INVALID_UNICODE_SURROGATE, "\"\\uD800\"");
    testError(Result.INVALID_UNICODE_SURROGATE, "\"\\uDBFF\"");
    testError(Result.INVALID_UNICODE_SURROGATE, "\"\\uD800\\\\\"");
    testError(Result.INVALID_UNICODE_SURROGATE, "\"\\uD800\\uDBFF\"");
    testError(Result.INVALID_UNICODE_SURROGATE, "\"\\uD800\\uE000\"");
  }
}
