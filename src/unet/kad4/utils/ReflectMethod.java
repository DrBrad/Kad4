package unet.kad4.utils;

import java.lang.reflect.Method;

public class ReflectMethod {

    private Object instance;
    private Method method;

    public ReflectMethod(Object instance, Method method){
        this.instance = instance;
        this.method = method;
    }

    public Object getInstance(){
        return instance;
    }

    public void setInstance(Object instance){
        this.instance = instance;
    }

    public Method getMethod(){
        return method;
    }

    public void setMethod(Method method){
        this.method = method;
    }
}
