import java.util.ArrayList;
import java.util.List;

public class Value {
  private List<Pair> pairs = new ArrayList<>();
  private List<Value> values = new ArrayList<>();
  private double number;
  private String string;
  private Type type;

  public Value(Type type) {
    this.type = type;
  }

  public Value() {
    this.type = Type.NULL;
  }

  public List<Pair> getPairs() {
    return pairs;
  }

  public void setPairs(List<Pair> pairs) {
    this.pairs = pairs;
  }

  public List<Value> getValues() {
    return values;
  }

  public void setValues(List<Value> values) {
    this.values = values;
  }

  public double getNumber() {
    return number;
  }

  public void setNumber(double number) {
    this.number = number;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
}