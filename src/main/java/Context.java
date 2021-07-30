import java.util.Stack;

public class Context {
  private String json;
  private int cursor;
  private String stack;

  public String getStack() {
    return stack;
  }

  public void setStack(String stack) {
    this.stack = stack;
  }

  public Context(String json) {
    this.json = json;
    this.cursor = 0;
    this.stack = "";
  }

  // to avoid string index out of bounds exception
  public Character getChar(int index) {
    if (index >= json.length()) {
      return '\0';
    } else {
      return json.charAt(index);
    }
  }

  public String getJson() {
    return json;
  }

  public void setJson(String json) {
    this.json = json;
  }

  public int getCursor() {
    return cursor;
  }

  public void setCursor(int cursor) {
    this.cursor = cursor;
  }
}