package unet.kad4.messages.inter;

public enum MessageType {

    REQ_MSG {
        @Override
        public String innerKey(){
            return "a";
        }
        @Override
        public String getRPCTypeName(){
            return "q";
        }
    }, RSP_MSG {
        @Override
        public String innerKey(){
            return "r";
        }
        @Override
        public String getRPCTypeName(){
            return "r";
        }
    }, ERR_MSG {
        @Override
        public String innerKey(){
            return "e";
        }
        @Override
        public String getRPCTypeName(){
            return "e";
        }
    }, INVALID
    , ANY;

    public String innerKey(){
        return null;
    }

    public String getRPCTypeName(){
        return name().toLowerCase();
        //return null;
    }

    public static MessageType fromRPCTypeName(String key){
        key = key.toLowerCase();

        for(MessageType t : values()){
            if(key.equals(t.getRPCTypeName())){
                return t;
            }
        }

        return INVALID;
    }

    public static final String TYPE_KEY = "y";
}
