package SpringClass.shop.exceptions.coupon;

public class CouponAlreadyRegisteredException extends RuntimeException {
    public CouponAlreadyRegisteredException(String message) {
        super(message);
    }
}
