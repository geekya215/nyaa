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

  static Result parseNull(Context context, Value value) {
    EXCEPT(context, 'n');

    int cursor = context.getCursor();

    if (context.getChar(cursor) != 'u' || context.getChar(cursor + 1) != 'l' || context.getChar(cursor + 2) != 'l') {
      return Result.INVALID_VALUE;
    }

    context.setCursor(cursor + 3);
    value.setType(Type.NULL);

    return Result.OK;
  }

  static Result parseTrue(Context context, Value value) {
    EXCEPT(context, 't');

    int cursor = context.getCursor();

    if (context.getChar(cursor) != 'r' || context.getChar(cursor + 1) != 'u' || context.getChar(cursor + 2) != 'e') {
      return Result.INVALID_VALUE;
    }

    context.setCursor(cursor + 3);
    value.setType(Type.TRUE);

    return Result.OK;
  }

  static Result parseFalse(Context context, Value value) {
    EXCEPT(context, 'f');

    int cursor = context.getCursor();

    if (context.getChar(cursor) != 'a' || context.getChar(cursor + 1) != 'l' || context.getChar(cursor + 2) != 's' || context.getChar(cursor + 3) != 'e') {
      return Result.INVALID_VALUE;
    }

    context.setCursor(cursor + 4);
    value.setType(Type.FALSE);

    return Result.OK;
  }

  static Result parseValue(Context context, Value value) {
    int cursor = context.getCursor();

    return switch (context.getChar(cursor)) {
      case 'n' -> parseNull(context, value);
      case 't' -> parseTrue(context, value);
      case 'f' -> parseFalse(context, value);
      case '\0' -> Result.EXCEPT_VALUE;
      default -> Result.INVALID_VALUE;
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
        result = Result.ROOT_NOT_SINGULAR;
      }
    }
    return result;
  }

  static Type getType(Value value) {
    assert value != null;
    return value.getType();
  }
}
