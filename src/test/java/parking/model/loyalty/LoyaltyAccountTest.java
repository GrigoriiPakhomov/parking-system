package parking.model.loyalty;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoyaltyAccountTest {

    @Test
    @DisplayName("Скидка должна увеличиваться на один процент")
    void testIncreaseDiscountByOnePercent() {
        LoyaltyAccount account = new LoyaltyAccount();

        account.upDiscount();

        assertThat(account.getCurrentDiscount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Скидка не должна превышать десять процентов")
    void testNotIncreaseDiscountAboveTenPercent() {
        LoyaltyAccount account = new LoyaltyAccount();

        for (int i = 0; i < 15; i++) {
            account.upDiscount();
        }

        assertThat(account.getCurrentDiscount()).isEqualTo(10);
    }
}