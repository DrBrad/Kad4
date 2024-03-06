package unet.kad4.messages.inter;

import unet.kad4.messages.ErrorResponse;

public class MessageException extends Exception {

    //private ErrorResponse.ErrorType errorType;

    public MessageException(String message){
        super(message);
    }

    public MessageException(String message, Throwable cause){
        super(message,cause);
    }

    /*
    public MessageException(String message, Throwable cause, ErrorResponse.ErrorType errorType){
        super(message,cause);
        this.errorType = errorType;
    }

    public MessageException(String message, ErrorResponse.ErrorType errorType){
        super(message);
        this.errorType = errorType;
    }

    public ErrorResponse.ErrorType getErrorType(){
        return errorType;
    }
    */
}
