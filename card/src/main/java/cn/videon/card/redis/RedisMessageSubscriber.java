package cn.videon.card.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
public class RedisMessageSubscriber implements MessageListener {


    /**
     * @description: 接收redis中的消息
     * @param:
     * @return:
     * @author: Limy
     * @date: 18/3/23
     */
    @Override
    public void onMessage(final Message message, final byte[] bytes) {

    }


}
