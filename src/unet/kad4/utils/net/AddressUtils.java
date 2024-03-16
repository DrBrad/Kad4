package unet.kad4.utils.net;

import java.net.*;
import java.util.Arrays;

public class AddressUtils {

    private static final byte[] LOCAL_BROADCAST = new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
    private static final NetMask V4_MAPPED;

    static {
        try{
            // ::ffff:0:0/96
            V4_MAPPED = new NetMask(Inet6Address.getByAddress(null, new byte[]{
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    (byte) 0xff,
                    (byte) 0xff,
                    0x00,
                    0x00,
                    0x00,
                    0x00
            }, null), 96);
        }catch(UnknownHostException e){
            throw new Error("Unable to set Global Unicast IPv4 static variable.");
        }
    }

    public static boolean isBogon(InetSocketAddress address){
        return !(address.getPort() > 0 && address.getPort() <= 0xffff && isGlobalUnicast(address.getAddress()));
    }

    public static boolean isBogon(InetAddress address, int port){
        return !(port > 0 && port <= 0xffff && isGlobalUnicast(address));
    }


    //MAYBE THIS IS UNNEEDED ATM...
    public static boolean isTeredo(InetAddress address){
        if(address instanceof Inet6Address){
            byte[] buf = address.getAddress();
            return buf[0] == 0x20 &&
                    buf[1] == 0x01 &&
                    buf[2] == 0x00 &&
                    buf[3] == 0x00;
        }
        return false;
    }

    public static boolean isGlobalUnicast(InetAddress address){
        if(address instanceof Inet4Address){
            if(address.getAddress()[0] == 0 || Arrays.equals(address.getAddress(), LOCAL_BROADCAST)){
                return false;
            }
        }else if(address instanceof Inet6Address){
            if((address.getAddress()[0] & 0xfe) == 0xfc || (V4_MAPPED.contains(address) || ((Inet6Address) address).isIPv4CompatibleAddress())){
                return false;
            }
        }

        return !(address.isAnyLocalAddress() || address.isLinkLocalAddress() || address.isLoopbackAddress() || address.isMulticastAddress() || address.isSiteLocalAddress());
    }

    public static byte[] packAddress(InetSocketAddress address){
        byte[] buf = address.getAddress().getAddress();

        if(address.getAddress() instanceof Inet4Address){
            return new byte[]{
                    buf[0],
                    buf[1],
                    buf[2],
                    buf[3],

                    (byte) ((address.getPort() & 0xff00) >> 8),
                    (byte) (address.getPort() & 0xff)
            };

        }else if(address.getAddress() instanceof Inet6Address){
            return new byte[]{
                    buf[0],
                    buf[1],
                    buf[2],
                    buf[3],

                    buf[4],
                    buf[5],
                    buf[6],
                    buf[7],

                    buf[8],
                    buf[9],
                    buf[10],
                    buf[11],

                    buf[12],
                    buf[13],
                    buf[14],
                    buf[15],

                    (byte) ((address.getPort() & 0xff00) >> 8),
                    (byte) (address.getPort() & 0xff)
            };
        }

        return null;
    }

    public static InetSocketAddress unpackAddress(byte[] buf){
        try{
            if(buf.length == 6){
                InetAddress address = InetAddress.getByAddress(new byte[]{
                        buf[0],
                        buf[1],
                        buf[2],
                        buf[3]
                });

                return new InetSocketAddress(address, ((buf[4] << 8) | buf[5] & 0xff));

            }else if(buf.length == 18){
                InetAddress address = InetAddress.getByAddress(new byte[]{
                        buf[0],
                        buf[1],
                        buf[2],
                        buf[3],

                        buf[4],
                        buf[5],
                        buf[6],
                        buf[7],

                        buf[8],
                        buf[9],
                        buf[10],
                        buf[11],

                        buf[12],
                        buf[13],
                        buf[14],
                        buf[15]
                });

                return new InetSocketAddress(address, ((buf[16] << 8) | buf[17] & 0xff));
            }
        }catch(UnknownHostException e){
            e.printStackTrace();
        }
        return null;
    }
}
