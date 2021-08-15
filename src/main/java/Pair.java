public class Pair {
  private String key;
  private Value value;

  public Pair() {
    this.value = new Value();
  }

  public Pair(String key, Value value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Value getValue() {
    return value;
  }

  public void setValue(Value value) {
    this.value = value;
  }
}
