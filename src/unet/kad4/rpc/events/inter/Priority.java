package unet.kad4.rpc.events.inter;

public enum Priority {

    LOWEST {
        public int getValue(){
            return 0;
        }
    },
    LOW {
        public int getValue(){
            return 1;
        }
    },
    DEFAULT {
        public int getValue(){
            return 2;
        }
    },
    HIGH {
        public int getValue(){
            return 3;
        }
    },
    HIGHEST {
        public int getValue(){
            return 4;
        }
    };

    public int getValue(){
        return 0;
    }
}
