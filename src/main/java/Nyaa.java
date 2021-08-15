public class Nyaa {

  static void EXCEPT(Context context, char c) {
    int cursor = context.getCursor();

    do {
      assert context.getChar(cursor) == c;
      context.setCursor(cursor + 1);
    } while (false);
  }

  static void parseWhiteSpace(Context context) {
    int cursor = context.getCursor();

    char c = context.getChar(cursor);

    while (c == ' ' || c == '\n' || c == '\t' || c == '\r') {
      cursor++;
      c = context.getChar(cursor);
    }
    context.setCursor(cursor);
  }

  static Result parseLiteral(Context context, Value value, String literal, Type type) {
    EXCEPT(context, literal.charAt(0));
    int cursor = context.getCursor();

    int i;
    for (i = 0; i < literal.length() - 1; ++i) {
      if (context.getChar(cursor + i) != literal.charAt(i + 1)) {
        return Result.INVALID_VALUE;
      }
    }

    context.setCursor(cursor + i);
    value.setType(type);
    return Result.OK;
  }

  static Result parseNumber(Context context, Value value) {
    String json = context.getJson();
    int cursor = context.getCursor();
    int start = cursor;

    if (context.getChar(cursor) == '-')
      cursor++;
    if (context.getChar(cursor) == '0')
      cursor++;
    else {
      char c = context.getChar(cursor);
      if (!(c >= '1' && c <= '9'))
        return Result.INVALID_VALUE;
      for (cursor++; Character.isDigit(context.getChar(cursor)); cursor++) ;
    }

    if (context.getChar(cursor) == '.') {
      cursor++;
      if (!Character.isDigit(context.getChar(cursor)))
        return Result.INVALID_VALUE;
      for (cursor++; Character.isDigit(context.getChar(cursor)); cursor++) ;
    }

    if (context.getChar(cursor) == 'e' || context.getChar(cursor) == 'E') {
      cursor++;

      if (context.getChar(cursor) == '+' || context.getChar(cursor) == '-')
        cursor++;
      if (!Character.isDigit(context.getChar(cursor)))
        return Result.INVALID_VALUE;
      for (cursor++; Character.isDigit(context.getChar(cursor)); cursor++) ;
    }

    try {
      double res = Double.parseDouble(json.substring(start, cursor));
      if (Double.isInfinite(res)) {
        return Result.NUMBER_TOO_BIG;
      } else {
        value.setNumber(res);
        context.setCursor(cursor);
        value.setType(Type.NUMBER);
        return Result.OK;
      }
    } catch (NumberFormatException e) {
      return Result.INVALID_VALUE;
    }
  }

  static int parseHex4(Context context) throws Exception {
    int u = 0;
    for (int i = 0; i < 4; i++) {
      char ch = context.getCharAndIncrement();
      u <<= 4;
      if (ch >= '0' && ch <= '9') u |= ch - '0';
      else if (ch >= 'A' && ch <= 'F') u |= ch - ('A' - 10);
      else if (ch >= 'a' && ch <= 'f') u |= ch - ('a' - 10);
      else throw new Exception();
    }
    return u;
  }

  static void encodeUTF8(StringBuilder c, int u) {
    if (u <= 0x7F)
      c.append((char) (u & 0xFF));
    else if (u <= 0x7FF) {
      c.append((char) (0xC0 | ((u >> 6) & 0xFF)));
      c.append((char) (0x80 | (u & 0x3F)));
    } else if (u <= 0xFFFF) {
      c.append((char) (0xE0 | ((u >> 12) & 0xFF)));
      c.append((char) (0x80 | ((u >> 6) & 0x3F)));
      c.append((char) (0x80 | (u & 0x3F)));
    } else {
      assert (u <= 0x10FFFF);
      c.append((char) (0xF0 | ((u >> 18) & 0xFF)));
      c.append((char) (0x80 | ((u >> 12) & 0x3F)));
      c.append((char) (0x80 | ((u >> 6) & 0x3F)));
      c.append((char) (0x80 | (u & 0x3F)));
    }
  }

  static Result parseString(Context context, Value value) {
    EXCEPT(context, '\"');

    StringBuilder stack = new StringBuilder();

    for (; ; ) {
      char ch = context.getCharAndIncrement();
      switch (ch) {
        case '\"' -> {
          setString(value, stack.toString());
          return Result.OK;
        }
        case '\\' -> {
          switch (context.getCharAndIncrement()) {
            case '\"' -> stack.append('\"');
            case '\\' -> stack.append('\\');
            case '/' -> stack.append('/');
            case 'b' -> stack.append('\b');
            case 'f' -> stack.append('\f');
            case 'n' -> stack.append('\n');
            case 'r' -> stack.append('\r');
            case 't' -> stack.append('\t');
            case 'u' -> {
              int u;
              try {
                u = parseHex4(context);
                if (u >= 0xD800 && u <= 0xDBFF) { /* surrogate pair */
                  if (context.getCharAndIncrement() != '\\')
                    return Result.INVALID_UNICODE_SURROGATE;
                  if (context.getCharAndIncrement() != 'u')
                    return Result.INVALID_UNICODE_SURROGATE;
                  int u2 = parseHex4(context);
                  if (u2 < 0xDC00 || u2 > 0xDFFF)
                    return Result.INVALID_UNICODE_SURROGATE;
                  u = (((u - 0xD800) << 10) | (u2 - 0xDC00)) + 0x10000;
                }
                encodeUTF8(stack, u);
              } catch (Exception e) {
                return Result.INVALID_UNICODE_HEX;
              }
            }
            default -> {
              return Result.INVALID_STRING_ESCAPE;
            }
          }
        }
        case '\0' -> {
          return Result.MISS_QUOTATION_MARK;
        }
        default -> {
          if (ch < 0x20) {
            return Result.INVALID_STRING_CHAR;
          }
          stack.append(ch);
        }
      }
    }
  }

  static Result parseArray(Context context, Value value) {
    Result result;
    EXCEPT(context, '[');
    parseWhiteSpace(context);
    if (context.getChar() == ']') {
      value.setType(Type.ARRAY);
      context.incrementCursor();
      return Result.OK;
    }

    for (; ; ) {
      Value v = new Value();
      if ((result = parseValue(context, v)) != Result.OK) {
        break;
      }
      value.getValues().add(v);
      parseWhiteSpace(context);

      if (context.getChar() == ',') {
        context.incrementCursor();
        parseWhiteSpace(context);
      } else if (context.getChar() == ']') {
        value.setType(Type.ARRAY);
        context.incrementCursor();
        return Result.OK;
      } else {
        result = Result.MISS_COMMA_OR_SQUARE_BRACKET;
        break;
      }
    }
    return result;
  }

  static Result parseObject(Context context, Value value) {
    Result result;
    EXCEPT(context, '{');
    parseWhiteSpace(context);
    if (context.getChar() == '}') {
      context.incrementCursor();
      value.setType(Type.OBJECT);
      return Result.OK;
    }

    for (; ; ) {
      Pair pair = new Pair();

      // parse key
      if (context.getChar() != '"') {
        result = Result.MISS_KEY;
        break;
      }

      if ((result = parseString(context, value)) != Result.OK) {
        break;
      }

      String key = getString(value);
      pair.setKey(key);

      parseWhiteSpace(context);

      if (context.getChar() != ':') {
        result = Result.MISS_COLON;
        break;
      }

      context.incrementCursor();
      parseWhiteSpace(context);

      // parse value
      if ((result = parseValue(context, pair.getValue())) != Result.OK) {
        break;
      }
      value.getPairs().add(pair);

      parseWhiteSpace(context);
      if (context.getChar() == ',') {
        context.incrementCursor();
        parseWhiteSpace(context);
      } else if (context.getChar() == '}') {
        context.incrementCursor();
        value.setType(Type.OBJECT);
        return Result.OK;
      } else {
        result = Result.MISS_COMMA_OR_CURLY_BRACKET;
        break;
      }

    }
    value.setType(Type.NULL);
    return result;
  }

  static Result parseValue(Context context, Value value) {
    int cursor = context.getCursor();

    return switch (context.getChar(cursor)) {
      case 'n' -> parseLiteral(context, value, "null", Type.NULL);
      case 't' -> parseLiteral(context, value, "true", Type.TRUE);
      case 'f' -> parseLiteral(context, value, "false", Type.FALSE);
      default -> parseNumber(context, value);
      case '"' -> parseString(context, value);
      case '[' -> parseArray(context, value);
      case '{' -> parseObject(context, value);
      case '\0' -> Result.EXCEPT_VALUE;
    };
  }

  static Result parse(Value value, String json) {
    assert value != null;

    Result result;
    Context context = new Context(json);
    value.setType(Type.NULL);
    parseWhiteSpace(context);
    if ((result = parseValue(context, value)) == Result.OK) {
      parseWhiteSpace(context);
      int cursor = context.getCursor();
      if (context.getChar(cursor) != '\0') {
        value.setType(Type.NULL);
        result = Result.ROOT_NOT_SINGULAR;
      }
    }
    return result;
  }

  static Type getType(Value value) {
    assert value != null;
    return value.getType();
  }

  static double getNumber(Value value) {
    assert value != null && value.getType() == Type.NUMBER;
    return value.getNumber();
  }

  static void setString(Value value, String string) {
    assert (value != null && string != null);
    value.setString(string);
    value.setType(Type.STRING);
  }

  static String getString(Value value) {
    assert value != null;
    return value.getString();
  }

  static int getArraySize(Value value) {
    assert value != null && value.getType() == Type.ARRAY;
    return value.getValues().size();
  }

  static Value getArrayElement(Value value, int index) {
    assert value != null && value.getType() == Type.ARRAY;
    assert index < value.getValues().size();
    return value.getValues().get(index);
  }

  static int getObjectSize(Value value) {
    assert value != null && value.getType() == Type.OBJECT;
    return value.getPairs().size();
  }

  static String getObjectKey(Value value, int index) {
    assert value != null && value.getType() == Type.OBJECT;
    assert index < value.getPairs().size();
    return value.getPairs().get(index).getKey();
  }

  static Value getObjectValue(Value value, int index) {
    assert value != null && value.getType() == Type.OBJECT;
    assert index < value.getPairs().size();
    return value.getPairs().get(index).getValue();
  }
}
