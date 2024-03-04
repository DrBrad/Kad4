package unet.kad4.rpc;

public class RPCReceiver {



    /*
    private RPCServer server;

    public RPCReceiver(RPCServer server){
        this.server = server;
    }

    public void onRequest(MessageBase message){
        /*
        MessageBase response;

        switch(message.getMethod()){
            case PING:
                response = onPing((PingRequest) message);
                break;

            case FIND_NODE:
                response = onFindNode((FindNodeRequest) message);
                break;

            default:
                response = onError(message, ErrorMessage.ErrorType.METHOD);
                break;
        }

        server.send(new RPCCall(response));
        *./
    }

    private MessageBase onPing(PingRequest request){
        PingResponse response = new PingResponse(request.getTransactionID());
        response.setDestination(request.getOrigin());
        response.setPublic(request.getOrigin());
        return response;
    }

    private MessageBase onFindNode(FindNodeRequest request){
        if(request.getTarget() == null){
            return onError(request, ErrorMessage.ErrorType.PROTOCOL);
        }

        FindNodeResponse response = new FindNodeResponse(request.getTransactionID());
        response.setDestination(request.getOrigin());
        response.setPublic(request.getOrigin());

        response.addNodes(server.getRoutingTable().findClosest(request.getTarget(), KBucket.MAX_BUCKET_SIZE));
        return response;
    }

    private MessageBase onError(MessageBase request, ErrorMessage.ErrorType type){
        ErrorMessage response = new ErrorMessage(request.getTransactionID());
        response.setDestination(request.getOrigin());
        response.setPublic(request.getOrigin());
        response.setErrorType(type);
        return response;
    }
    */
}
