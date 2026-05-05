package SpringClass.shop.exceptions.category;

public class CategoryNameAlreadyExistException extends RuntimeException {
    public CategoryNameAlreadyExistException(String message) {
        super(message);
    }
}
