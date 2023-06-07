package cart.domain.coupon;

import cart.domain.Money;
import cart.exception.CouponException;
import cart.exception.ExceptionType;
import java.math.BigDecimal;
import java.util.Objects;

public class Coupon {

    public static final Coupon NONE = new Coupon("NONE", CouponType.NONE, BigDecimal.ZERO, Money.ZERO);

    private final Long id;
    private final String name;
    private final CouponType couponType;
    private final BigDecimal discountValue;
    private final Money minOrderPrice;

    public Coupon(String name, CouponType couponType, BigDecimal discountValue, Money minOrderPrice) {
        this(null, name, couponType, discountValue, minOrderPrice);
    }

    public Coupon(Long id, String name, CouponType couponType, BigDecimal discountValue, Money minOrderPrice) {
        this.id = id;
        this.name = name;
        this.couponType = couponType;
        validateDiscountValue(couponType, discountValue);
        this.discountValue = discountValue;
        this.minOrderPrice = minOrderPrice;
    }

    private void validateDiscountValue(CouponType couponType, BigDecimal discountValue) {
        if (!couponType.isValid(discountValue)) {
            throw new CouponException(ExceptionType.INVALID_DISCOUNT_VALUE);
        }
    }

    public Money discountPrice(Money totalCartsPrice) {
        if (totalCartsPrice.isLessThan(minOrderPrice.getValue())) {
            throw new CouponException(ExceptionType.INVALID_MIN_ORDER);
        }
        return couponType.discount(totalCartsPrice, discountValue);
    }

    public boolean isCoupon() {
        return couponType != CouponType.NONE;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CouponType getCouponType() {
        return couponType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public Money getMinOrderPrice() {
        return minOrderPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Coupon coupon = (Coupon) o;
        return Objects.equals(id, coupon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
