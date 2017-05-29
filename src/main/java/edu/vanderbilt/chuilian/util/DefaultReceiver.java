package edu.vanderbilt.chuilian.util;

import org.zeromq.ZMsg;

import java.util.concurrent.ExecutorService;

/**
 * Created by Killian on 5/24/17.
 */
public class DefaultReceiver extends DataReceiver {
    public DefaultReceiver(String address, MsgBufferMap msgBufferMap, ExecutorService executor, ZkConnect zkConnect) {
        super("", address, msgBufferMap, executor, zkConnect);
    }

    @Override
    // default receiver will not register itself to any topics on zookeeper
    public void start() throws Exception {
        // connect to the sender address
        recSocket.connect("tcp://" + address);
        // subscribe topic
        recSocket.subscribe(topic.getBytes());
        // register message buffer for this topic
        msgBuffer = msgBufferMap.register(topic);
        if (msgBuffer == null) {
            throw new IllegalStateException("message buffer with the topic name " + topic + " already exist!");
        }
        // execute receiver thread for this topic
        future = executor.submit(() -> {
            {
                //debug
                System.out.println("default receiver thread created: " + topic);
            }
            while (true) {
                receiver();
            }
        });
    }

    @Override
    // all message received by default receiver are stored in topic "", may need to change in the future
    public void receiver() {
        ZMsg receivedMsg = ZMsg.recvMsg(recSocket);
        msgBuffer.add(receivedMsg);
        {
            //debug
            System.out.println("Message Received (from default receiver):");
            System.out.println(new String(receivedMsg.getFirst().getData()));
            //System.out.println(DataSampleHelper.deserialize(receivedMsg.getLast().getData()).sampleId());
            System.out.println(new String(receivedMsg.getLast().getData()));
        }
    }

    @Override
    // will not unregister itself from zookeeper server since it never does
    public MsgBuffer stop() throws Exception {
        {
            //debug
            System.out.println("stopping default receiver: " + topic);
        }
        // stop the receiver thread
        future.cancel(false);
        // shutdown zmq socket and context
        recSocket.close();
        recContext.term();
        {
            //debug
            System.out.println("default receiver stopped: " + topic);
        }
        // unregister the message buffer, the return value is the old buffer, which may have some old message left
        // return them to subscriber for properly handling.
        return msgBufferMap.unregister(topic);
    }
}
