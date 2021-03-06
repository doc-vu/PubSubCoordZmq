package edu.vanderbilt.chuilian.clients.publisher;

/**
 * Created by Killian on 5/24/17.
 */

import edu.vanderbilt.chuilian.util.MsgBufferMap;
import edu.vanderbilt.chuilian.util.ZkConnect;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * topic --> corresponding data sender ref
 */
public class TopicSenderMap {
    final DefaultSender defaultSender;
    ConcurrentHashMap<String, DataSender> map;

    // this data structure should only be initialized given a default receiver address
    public TopicSenderMap(DefaultSender defaultSender) {
        this.defaultSender = defaultSender;
        this.map = new ConcurrentHashMap<>();
    }

    /**
     * get corresponding data sender for given topic
     *
     * @param topic
     * @return null if no such data sender exist
     */
    public DataSender get(String topic) {
        return map.get(topic);
    }

    /**
     * register a topic with given receiver address, this will register the topic to the TopicSenderMap and return the reference to newly created DataSender
     *
     * @param topic
     * @return return newly created sender, if the sender for the topic already exist, return null
     */
    public DataSender register(String topic, String address, MsgBufferMap msgBufferMap, ExecutorService executor, ZkConnect zkConnect, String ip) {
        if (map.containsKey(topic)) return null;
        else {
            DataSender newSender = new DataSender(topic, address, msgBufferMap, executor, zkConnect, ip);
            map.put(topic, newSender);
            return newSender;
        }
    }

    /**
     * delete a topic along with the sender that corresponding to it
     *
     * @param topic
     * @return the previous sender associated with topic, or null if there was no mapping for topic
     */
    public DataSender unregister(String topic) {
        return map.remove(topic);
    }

    /**
     * get default data sender
     *
     * @return default data sender should always exist as long as broker and publisher started properly.
     */
    public DefaultSender getDefault() {
        return this.defaultSender;
    }

    /**
     * Returns a Set view of the mappings contained in this map. for iterating the map
     *
     * @return
     */
    public Set<Map.Entry<String, DataSender>> entrySet() {
        return map.entrySet();
    }

}
