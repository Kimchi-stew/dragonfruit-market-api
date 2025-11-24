package SpringClass.shop.exceptions;

public class AdminPermissionRequiredException extends RuntimeException {
    public AdminPermissionRequiredException(String message) {
        super(message);
    }
}
