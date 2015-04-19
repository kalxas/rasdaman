package org.rasdaman.rasnet;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import org.rasdaman.rasnet.exception.AlreadyRunningException;
import org.rasdaman.rasnet.exception.DuplicateService;
import org.rasdaman.rasnet.message.Test;
import org.rasdaman.rasnet.server.ServiceManager;
import org.rasdaman.rasnet.server.ServiceManagerConfig;

/**
 * Created by rasdaman on 06.04.15.
 */
public class IntegrationTest {
    private class SearchServiceImpl extends org.rasdaman.rasnet.message.Test.SearchService {

        @Override
        public void search(RpcController controller,
                           Test.TestRequest request,
                           RpcCallback<Test.TestReply> done) {

            if (request.getData().equals("hello")) {
                Test.TestReply reply = Test.TestReply.newBuilder().setData(request.getData()).build();
                done.run(reply);

            } else if (request.getData().equals("fail")) {
                throw new RuntimeException("runtime");
            } else {
                controller.setFailed("controller");
            }
        }
    }

    @org.junit.Test
    public void ServeData() {
        ServiceManagerConfig config = new ServiceManagerConfig();
        config.setAliveTimeout(100000);
        ServiceManager manager = new ServiceManager(config);
//
//        try {
//
//            Service service = new SearchServiceImpl();
//            manager.addService(service);
//
//            manager.serve("tcp://*:9000");
//
//            Thread.sleep(1000*1000*1000);
//
//        } catch (DuplicateService duplicateService) {
//            duplicateService.printStackTrace();
//        } catch (AlreadyRunningException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                manager.close();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
