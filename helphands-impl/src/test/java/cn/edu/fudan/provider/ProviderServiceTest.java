package cn.edu.fudan.provider;

import cn.edu.fudan.api.ProviderService;
import cn.edu.fudan.domain.ProviderDTO;
import cn.edu.fudan.domain.ProviderParam;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertNotNull;

/**
 * @author fuwuchen
 * @date 2022/5/21 20:43
 */
public class ProviderServiceTest {

    private static ServiceTest.TestServer server;

    @BeforeClass
    public static void setUp() {
        server = ServiceTest.startServer(
                defaultSetup()
                        .withCassandra()
//                        .withCluster(true)
        );
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void shouldStoreNewProvider() throws Exception {
        ProviderService service = server.client(ProviderServiceImpl.class);

        ProviderParam request = new ProviderParam();
        request.setName("test");
        request.setMobile("123456");
        request.setRating(3.0f);
        request.setSince(20200L);

        ServiceCall<ProviderParam, ProviderDTO> add = service.add();
        ProviderDTO result = add.invoke(request)
                .toCompletableFuture()
                .get(5, SECONDS);
        assertNotNull(result.getId());
    }
}
