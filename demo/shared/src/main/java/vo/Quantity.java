package vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Quantity {
    private int value;

    public Quantity add(int quantity) throws Exception {
        if(quantity <= 0) throw new Exception("수량은 1 이상");

        return new Quantity(this.value + quantity);
    }

    public Quantity minus(int quantity) throws Exception {
        if(this.value < quantity) throw new Exception("수량 부족");

        return new Quantity(this.value - quantity);
    }

    public void checkQuantity() throws Exception {
        if(this.value < 1) throw new Exception("수량 확인");
    }
}
