public class Context {
  private String json;
  private int cursor;

  public Context(String json) {
    this.json = json;
    this.cursor = 0;
  }

  // to avoid string index out of bounds exception
  public Character getChar(int index) {
    if (index >= json.length()) {
      return '\0';
    } else {
      return json.charAt(index);
    }
  }

  public Character getChar() {
    return this.getChar(this.cursor);
  }

  public Character getCharAndIncrement() {
    return this.getChar(this.cursor++);
  }

  public void incrementCursor() {
    this.cursor++;
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