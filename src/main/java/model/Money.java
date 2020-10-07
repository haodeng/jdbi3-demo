package model;

public class Money {
    private String amount;

    public Money(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return amount;
    }

    public static Money parse(String amount)
    {
        return new Money(amount);
    }
}
