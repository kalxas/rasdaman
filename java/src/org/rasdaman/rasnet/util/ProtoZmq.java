//package org.rasdaman.rasnet.util;
//
//import com.google.protobuf.InvalidProtocolBufferException;
//import com.google.protobuf.Message;
//import org.rasdaman.rasnet.common.Constants;
//import org.rasdaman.rasnet.exception.NetworkingException;
//import org.rasdaman.rasnet.message.Base;
//import org.zeromq.ZMQ;
//
///**
// * Created by rasdaman on 2/19/15.
// */
//public class ProtoZmq {
//    /**
//     * @param destination
//     * @param peerId
//     * @param message
//     */
//    public static final int ZMQ_NO_FLAG = 0;
//
//    public static void sendToPeer(ZMQ.Socket destination, String peerId, Message message) throws NetworkingException {
//        boolean success = true;
//        Base.BaseMessage envelope = Base.BaseMessage.newBuilder()
//                .setData(message.toByteString())
//                .setType(ProtoZmq.getType(message))
//                .build();
//
//        success = success && destination.send(peerId.getBytes(), ZMQ.SNDMORE);
//        success = success && destination.send(envelope.toByteArray(), ZMQ_NO_FLAG);
//
//        if (!success) {
//            throw new NetworkingException("Failed to send message to peer.");
//        }
//    }
//
//    public static void send(ZMQ.Socket destination, Message message) throws NetworkingException {
//        Base.BaseMessage envelope = Base.BaseMessage.newBuilder()
//                .setData(message.toByteString())
//                .setType(ProtoZmq.getType(message))
//                .build();
//
//        boolean success = destination.send(envelope.toByteArray(), ZMQ_NO_FLAG);
//
//        if (!success) {
//            throw new NetworkingException("Failed to send message to peer.");
//        }
//    }
//
//    public static String receiveFromPeer(ZMQ.Socket source, MessageContainer outMessage) throws InvalidProtocolBufferException, NetworkingException {
//        boolean success = true;
//        String peerId = source.recvStr(Constants.DEFAULT_ENCODING);
//
//        if (source.hasReceiveMore()) {
//            byte[] data = source.recv();//source.recvStr(Constants.DEFAULT_ENCODING);
//            Base.BaseMessage envelope = Base.BaseMessage.parseFrom(data);//tr.getBytes(Constants.DEFAULT_ENCODING)
//
//            outMessage.setData(envelope.getData());
//            outMessage.setType(envelope.getType());
//
//            if (source.hasReceiveMore()) {
//                success = false;
//            }
//        } else {
//            success = false;
//        }
//
//        if (!success) {
//            throw new NetworkingException("Failed to receive message from peer.");
//        }
//
//        return peerId;
//    }
//
//    public static void receive(ZMQ.Socket source, MessageContainer outMessage) throws InvalidProtocolBufferException, NetworkingException {
//        Base.BaseMessage envelope = Base.BaseMessage.parseFrom(source.recv());
//
//        outMessage.setData(envelope.getData());
//        outMessage.setType(envelope.getType());
//
//        if (source.hasReceiveMore()) {
//            throw new NetworkingException("Failed to receive message.");
//        }
//    }
//
//    public static String getType(Message message) {
//        return message.getDescriptorForType().getFullName();
//    }
//}
