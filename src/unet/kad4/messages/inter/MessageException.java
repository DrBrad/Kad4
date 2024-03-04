package unet.kad4.messages.inter;

import unet.kad4.messages.ErrorMessage;

public class MessageException extends Exception {

    private ErrorMessage.ErrorType errorType;

    public MessageException(String message){
        super(message);
    }

    public MessageException(String message, Throwable cause){
        super(message,cause);
    }

    public MessageException(String message, Throwable cause, ErrorMessage.ErrorType errorType){
        super(message,cause);
        this.errorType = errorType;
    }

    public MessageException(String message, ErrorMessage.ErrorType errorType){
        super(message);
        this.errorType = errorType;
    }

    public ErrorMessage.ErrorType getErrorType(){
        return errorType;
    }
}
