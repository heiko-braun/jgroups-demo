package org.jboss;

import org.jboss.beans.metadata.api.annotations.*;
import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.GroupMembershipListener;
import org.jboss.ha.framework.server.ClusterPartition;
import org.jgroups.ChannelException;
import org.jgroups.util.DefaultSocketFactory;
import org.jgroups.util.Util;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Bela Ban
 * @version $Id$
 */
public class Chat implements GroupMembershipListener, ChatMBean {
    private ServerSocket     srv_sock;
    private Thread           runner;
    private volatile boolean running;
    private long             view_id=0;
    private ClusterPartition partition;
    private final Set<String> users=new HashSet<String>();

    private static final String SERVICE_NAME="ChatDemo";
    private static final int SERVER_PORT=7888;

    List<ChatCallback> listener = new ArrayList<ChatCallback>();

    public Chat() {
    }

    public void addListener(ChatCallback callback)
    {
        listener.add(callback);

        sendToAllClients(new Data(Data.VIEW, getMembers()));
    }

    public ClusterPartition getPartition() {
        return partition;
    }

    @Inject(bean="HAPartition")
    public void setPartition(ClusterPartition partition) {
        this.partition=partition;
    }

    public String getMembers() {
        List<String> members=partition != null? partition.getCurrentView() : null;
        return members != null? members.toString() : "n/a";
    }

    public String getLocalAddress() {
        ClusterNode me=partition != null? partition.getClusterNode() : null;
        return me != null? me.toString() : "n/a";
    }

    @Create
    public void create() throws Exception {
        if(partition == null)
            throw new NullPointerException("partition is null");

        partition.registerGroupMembershipListener(this);
        partition.registerRPCHandler(SERVICE_NAME, this);
        srv_sock=Util.createServerSocket(new DefaultSocketFactory(), SERVICE_NAME, SERVER_PORT);
        System.out.println("listening on " + srv_sock.getLocalSocketAddress());
        sendToAllClients(new Data(Data.VIEW, getMembers()));
    }


    @Start
    public void start() throws ChannelException {
    }

    @Stop
    public void stop() {
    }

    @Destroy
    public void destroy() {
        partition.unregisterRPCHandler(SERVICE_NAME, this);
        running=false;
        
        System.out.println("Closing socket " + srv_sock.getLocalSocketAddress());
        Util.close(srv_sock);
        users.clear();
    }

    public void receive(Data data) {
        switch(data.getType()) {
            case Data.MESSAGE:
                postMessage(data.getPayload());
                break;
            case Data.VIEW:
                break;
            case Data.JOIN:
                postMemberJoinedOrLeft(data.getPayload(), true);
                break;
            case Data.LEAVE:
                postMemberJoinedOrLeft(data.getPayload(), false);
                break;
        }
    }

    public void postMessage(String msg) {
        try {
            partition.callAsynchMethodOnCluster(SERVICE_NAME, "receiveMessage", new Object[]{msg}, new Class<?>[]{String.class}, false);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void postMemberJoinedOrLeft(String user, boolean joined) {
        try {
            partition.callAsynchMethodOnCluster(SERVICE_NAME, "memberJoinedOrLeft", new Object[]{user, joined},
                                                new Class<?>[]{String.class, boolean.class}, false);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * push chat messages to clients
     * @param msg
     */
    public void receiveMessage(String msg) {
        sendToAllClients(new Data(Data.MESSAGE, msg));        
    }

    public void memberJoinedOrLeft(String user, boolean joined) {
        Data data=new Data(joined? Data.JOIN : Data.LEAVE, user);
        if(joined)
            users.add(user);
        else
            users.remove(user);
        System.out.println("users = " + users);
        sendToAllClients(data);


    }

    public void membershipChanged(List<ClusterNode> clusterNodes, List<ClusterNode> clusterNodes1, List<ClusterNode> clusterNodes2) {
        if(partition != null && partition.getCurrentViewId() > view_id) {
            view_id=partition.getCurrentViewId();
            System.out.println("view change: " + getMembers());
            sendToAllClients(new Data(Data.VIEW, getMembers()));
        }
    }

    public void membershipChangedDuringMerge(List<ClusterNode> clusterNodes, List<ClusterNode> clusterNodes1, List<ClusterNode> clusterNodes2, List<List<ClusterNode>> lists) {
    }

    // swing client invocation
    protected void sendToAllClients(Data data) {
        if(data == null)
            return;

        for(ChatCallback c : listener)
        {
            switch (data.getType())
            {
                case Data.MESSAGE:
                    c.postMessage(data.getPayload());
                    break;
                case Data.VIEW:
                    c.postMemberJoinedOrLeft(data.getPayload(), false);
                    break;
                default:
                    System.out.println("Not processed -> " +data.getType() + ": "+data.getPayload());
            }
        }
    }
}
