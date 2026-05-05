package SpringClass.shop.exceptions.order;

public class OrderCancelNotAllowedException extends RuntimeException {
    public OrderCancelNotAllowedException(String message) {
        super(message);
    }
}
