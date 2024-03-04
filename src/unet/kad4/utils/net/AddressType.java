package unet.kad4.utils.net;

public enum AddressType {

    IPv4 {
        public int getAddressLength(){
            return 4;
        }
    },
    IPv6 {
        public int getAddressLength(){
            return 16;
        }
    };

    public int getAddressLength(){
        return 0;
    }
}
