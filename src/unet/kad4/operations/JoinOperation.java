package unet.kad4.operations;

public class JoinOperation {/*} implements Operation {

    private RPCServer server;
    private RefreshHandler refresh;
    private InetSocketAddress address;

    public JoinOperation(RPCServer server, RefreshHandler refresh, InetSocketAddress address){
        this.server = server;
        this.refresh = refresh;
        this.address = address;
    }

    @Override
    public void run(){
        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(address);
        request.setTarget(server.getRoutingTable().getDerivedUID());

        server.send(new Call(request, new MessageCallback(){
            @Override
            public void onResponse(MessageBase message){
                FindNodeResponse r = (FindNodeResponse) message;

                server.getRoutingTable().insert(new Node(r.getUID(), message.getOrigin()));
                System.out.println("SEEN FN "+message.getOrigin());

                new PingOperation(server, r.getAllNodes()).run();

                if(!refresh.isRunning()){
                    refresh.start();
                }
            }

            @Override
            public void onErrorResponse(ErrorMessage message){
                System.err.println("Unable to join node, node sent error message: "+message.getErrorType().getCode()+" - "+message.getErrorType().getDescription());
            }

            /*
            @Override
            public void onException(MessageException exception){
                exception.printStackTrace();
            }
            *./

            @Override
            public void onStalled(){
                System.err.println("Unable to join node, node never responded.");
            }
        }));
    }*/
}
