package SpringClass.shop.exceptions.common;

public class AdminPermissionRequiredException extends RuntimeException {
    public AdminPermissionRequiredException(String message) {
        super(message);
    }
}
