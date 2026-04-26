package SpringClass.shop.exceptions;

public class InquiryNotFoundException extends RuntimeException {
    public InquiryNotFoundException(String message) {
        super(message);
    }
}
