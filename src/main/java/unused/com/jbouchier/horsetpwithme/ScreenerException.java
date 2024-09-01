package unused.com.jbouchier.horsetpwithme;

public class ScreenerException extends Exception {
    private final Language.MessageKey errorKey;
    public ScreenerException(Language.MessageKey errorKey) {
        super((Throwable) null);
        this.errorKey = errorKey;
    }
    public Language.MessageKey getErrorKey() {
        return errorKey;
    }
}