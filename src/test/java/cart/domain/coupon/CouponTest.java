package cart.domain.coupon;

import static cart.domain.coupon.CouponType.FIXED;
import static cart.domain.coupon.CouponType.NONE;
import static cart.domain.coupon.CouponType.RATE;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cart.domain.Money;
import cart.exception.CouponException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class CouponTest {

    @ValueSource(ints = {-1, 0, 101})
    @ParameterizedTest
    void 비율에_따른_할인_쿠폰은_0이하이거나_100초과이면_예외가_발생한다(int value) {
        assertThatThrownBy(() -> new Coupon("쿠폰", RATE, valueOf(value), new Money(50000)))
                .isInstanceOf(CouponException.class);
    }

    @ValueSource(ints = {-1, 0})
    @ParameterizedTest
    void 금액에_따른_할인_쿠폰은_0이하면_예외가_발생한다(int value) {
        assertThatThrownBy(() -> new Coupon("쿠폰", FIXED, valueOf(value), new Money(50000)))
                .isInstanceOf(CouponException.class);
    }

    @ValueSource(ints = {-1, 1})
    @ParameterizedTest
    void 할인되지_않는_쿠폰은_0이_아니면_예외가_발생한다(int value) {
        assertThatThrownBy(() -> new Coupon("쿠폰", NONE, valueOf(value), new Money(50000)))
                .isInstanceOf(CouponException.class);
    }


    @Test
    void 금액을_할인할_때_최소_주문_가능_금액보다_작으면_예외가_발생한다() {
        Money minOrderPrice = new Money(50000);
        Coupon coupon = new Coupon("쿠폰", FIXED, valueOf(10000), minOrderPrice);

        assertThatThrownBy(() -> coupon.discountPrice(new Money(4000)))
                .isInstanceOf(CouponException.class);
    }

    @Test
    void 비율에_따른_할인_쿠폰을_사용한_가격을_반환한다() {
        Coupon coupon = new Coupon("쿠폰", RATE, valueOf(10), new Money(5000));

        Money result = coupon.discountPrice(new Money(10000));

        assertThat(result).isEqualTo(new Money(9000));
    }

    @Test
    void 금액에_따른_할인_쿠폰을_사용한_가격을_반환한다() {
        Coupon coupon = new Coupon("쿠폰", FIXED, valueOf(1000), new Money(5000));

        Money result = coupon.discountPrice(new Money(10000));

        assertThat(result).isEqualTo(new Money(9000));
    }

    @Test
    void 할인되지_않는_쿠폰을_사용한_가격을_반환한다() {
        Coupon coupon = new Coupon("쿠폰", NONE, valueOf(0), new Money(5000));

        Money result = coupon.discountPrice(new Money(10000));

        assertThat(result).isEqualTo(new Money(10000));
    }
}
