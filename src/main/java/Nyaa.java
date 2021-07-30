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

  static Result parseString(Context context, Value value) {
    EXCEPT(context, '\"');
    int cursor = context.getCursor();

    StringBuilder stack = new StringBuilder();

    for (; ; ) {
      char ch = context.getChar(cursor++);
      switch (ch) {
        case '\"' -> {
          setString(value, stack.toString());
          context.setCursor(cursor);
          return Result.OK;
        }
        case '\\' -> {
          switch (context.getChar(cursor++)) {
            case '\"' -> stack.append('\"');
            case '\\' -> stack.append('\\');
            case '/' -> stack.append('/');
            case 'b' -> stack.append('\b');
            case 'f' -> stack.append('\f');
            case 'n' -> stack.append('\n');
            case 'r' -> stack.append('\r');
            case 't' -> stack.append('\t');
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

  static Result parseValue(Context context, Value value) {
    int cursor = context.getCursor();

    return switch (context.getChar(cursor)) {
      case 'n' -> parseLiteral(context, value, "null", Type.NULL);
      case 't' -> parseLiteral(context, value, "true", Type.TRUE);
      case 'f' -> parseLiteral(context, value, "false", Type.FALSE);
      default -> parseNumber(context, value);
      case '"' -> parseString(context, value);
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
}
