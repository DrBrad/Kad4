package unet.kad4.routing.kb;

import unet.kad4.utils.Node;

import java.util.ArrayList;
import java.util.List;

public class KBucket {

    private ArrayList<Node> nodes, cache;
    public static final int MAX_BUCKET_SIZE = 5, MAX_STALE_COUNT = 1;

    public KBucket(){
        nodes = new ArrayList<>(MAX_BUCKET_SIZE);
        cache = new ArrayList<>(MAX_BUCKET_SIZE);
    }

    public synchronized void insert(Node n){
        if(nodes.contains(n)){
            nodes.get(nodes.indexOf(n)).setSeen();
            nodes.sort(new LSComparetor());

        }else if(nodes.size() >= MAX_BUCKET_SIZE){
            if(cache.contains(n)){
                cache.get(cache.indexOf(n)).setSeen();

            }else if(cache.size() >= MAX_BUCKET_SIZE){
                Node stale = null;
                for(Node s : cache){
                    if(s.getStale() >= MAX_STALE_COUNT){
                        if(stale == null || s.getStale() > stale.getStale()){
                            stale = s;
                        }
                    }
                }

                if(stale != null){
                    cache.remove(stale);
                    cache.add(n);
                }

            }else{
                cache.add(n);
            }
        }else{
            nodes.add(n);
            nodes.sort(new LSComparetor());
        }
    }

    public synchronized boolean containsIP(Node n){
        return nodes.contains(n) || cache.contains(n);
    }

    public synchronized boolean containsUID(Node n){
        return nodes.stream().anyMatch(c -> c.verify(n)) || cache.stream().anyMatch(c -> c.verify(n));
    }

    public List<Node> getAllNodes(){
        //nodes.sort(new LSComparetor());
        return nodes;
    }

    /*
    public List<Node> getAllNodesIncludingCache(){
        List<Node> q = new ArrayList<>();
        q.addAll(this.nodes);
        q.addAll(cache);
        return q;
    }
    */

    public List<Node> getUnQueriedNodes(long now){
        List<Node> q = new ArrayList<>();
        //long now = System.currentTimeMillis();

        for(Node n : nodes){
            if(!n.hasQueried(now)){
                q.add(n);
            }
        }

        return q;
    }


    public int size(){
        return nodes.size();
    }

    public int csize(){
        return cache.size();
    }

    /*

    public synchronized void markStale(Node c){
        if(contacts.contains(c)){
            contacts.get(contacts.indexOf(c)).markStale();
        }
    }

    public synchronized void removeSpoof(Node c){
        if(contacts.contains(c)){
            contacts.remove(c);

            if(!cache.isEmpty()){
                contacts.remove(c);
                contacts.add(cache.get(0));
                cache.remove(0);
            }

        }else if(cache.contains(c)){
            cache.remove(c);
        }
    }

    public synchronized boolean contains(Node c){
        return contacts.contains(c) || cache.contains(c);
    }

    public synchronized boolean verify(Node c){
        return contacts.stream().filter(o -> o.verify(c)).findFirst().isPresent() ||
                cache.stream().filter(o -> o.verify(c)).findFirst().isPresent();
    }

    public List<Node> getContacts(){
        contacts.sort(new LSComparetor());
        return contacts;
    }
    */
}
