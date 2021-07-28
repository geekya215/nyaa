public class Value {
  private double number;
  private Type type;

  public Value(Type type) {
    this.type = type;
  }

  public double getNumber() {
    return number;
  }

  public void setNumber(double number) {
    this.number = number;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
}