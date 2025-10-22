package domain.aggregate;

import vo.Quantity;

import java.util.UUID;

public class Inventory {
    private String inventoryId;
    private String productId;
    public Quantity quantity;

    public static Inventory create(
            String productId,
            int quantity
    ) {
        Inventory inventory = new Inventory();
        inventory.inventoryId = String.valueOf(UUID.randomUUID());
        inventory.productId = productId;
        inventory.quantity = new Quantity(quantity);

        return inventory;
    }

    public void restock(int quantity) throws Exception {
        this.quantity = this.quantity.add(quantity);
    }

    public void consume(int quantity) throws Exception {
        this.quantity = this.quantity.minus(quantity);
    }
}
