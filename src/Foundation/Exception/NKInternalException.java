package Foundation.Exception;

public class NKInternalException extends Exception {

    private String description;

    public NKInternalException(String description) {
        this.description = description;
    }

    public void describe() {
        System.out.println(this.description);
    }

}
