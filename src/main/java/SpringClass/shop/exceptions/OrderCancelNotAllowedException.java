package SpringClass.shop.exceptions;

public class OrderCancelNotAllowedException extends RuntimeException {
    public OrderCancelNotAllowedException(String message) {
        super(message);
    }
}
