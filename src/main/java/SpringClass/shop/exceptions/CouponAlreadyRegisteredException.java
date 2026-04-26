package SpringClass.shop.exceptions;

public class CouponAlreadyRegisteredException extends RuntimeException {
    public CouponAlreadyRegisteredException(String message) {
        super(message);
    }
}
