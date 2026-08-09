package parking.model.loyalty;

import lombok.Getter;

/**
 * Хранит информацию о накопленной скидке автомобиля.
 */
@Getter
public class LoyaltyAccount {

    private static final int MAX_DISCOUNT = 10;

    private int currentDiscount;

    /**
     * Увеличивает скидку на 1 процент,
     * но не позволяет превысить максимальную скидку.
     */
    public void upDiscount() {
        if (currentDiscount < MAX_DISCOUNT) {
            currentDiscount++;
        }
    }
}