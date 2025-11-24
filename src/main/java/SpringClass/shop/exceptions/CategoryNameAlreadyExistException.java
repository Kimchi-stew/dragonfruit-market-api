package SpringClass.shop.exceptions;

public class CategoryNameAlreadyExistException extends RuntimeException {
    public CategoryNameAlreadyExistException(String message) {
        super(message);
    }
}
