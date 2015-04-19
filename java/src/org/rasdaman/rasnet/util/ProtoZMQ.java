//package org.rasdaman.rasnet.util;
//
//import com.google.protobuf.Descriptors;
//import com.google.protobuf.InvalidProtocolBufferException;
//import com.google.protobuf.Message;
//import org.zeromq.ZMQ;
//import org.rasdaman.rasnet.message.Base.BaseMessage;
//
//
//public class ProtoZMQ {
//    public static final int ZMQ_NO_FLAG = 0;
//
//    public static boolean zmqSend(ZMQ.Socket socket, Message message) {
//        BaseMessage envelope = BaseMessage.newBuilder()
//                .setType(message.getDescriptorForType().getFullName())
//                .setData(message.toByteString())
//                .build();
//
//        byte[] bytes = envelope.toByteArray();
//        return socket.send(bytes, ZMQ_NO_FLAG);
//    }
//
//    public static BaseMessage zmqRecv(ZMQ.Socket socket) throws InvalidProtocolBufferException {
//        byte[] message = socket.recv();
//        return BaseMessage.parseFrom(message);
//    }
//
//    public static boolean zmqRawSend(ZMQ.Socket socket, BaseMessage message) {
//        return socket.send(message.toByteArray(), ZMQ_NO_FLAG);
//    }
//
//    public static String getMethodName(Descriptors.MethodDescriptor methodDescriptor) {
//        StringBuilder sb = new StringBuilder();
//        sb.append(methodDescriptor.getService().getName());
//        sb.append('.');
//        sb.append(methodDescriptor.getName());
//
//        return sb.toString();
//    }
//}
