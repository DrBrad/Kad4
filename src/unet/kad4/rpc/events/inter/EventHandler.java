package unet.kad4.rpc.events.inter;

import unet.kad4.messages.inter.MessageType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {

    String method() default "*";

    MessageType type() default MessageType.ANY;

    Priority priority() default Priority.DEFAULT;
}
