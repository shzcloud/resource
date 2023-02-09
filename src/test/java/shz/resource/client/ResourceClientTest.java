package shz.resource.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import shz.core.model.PageInfo;
import shz.jdbc.JdbcService;
import shz.resource.entity.SysResource;
import shz.resource.server.CheckResult;
import shz.resource.server.DefaultResourceServer;
import shz.resource.server.ResourceServer;
import shz.resource.vo.QueryResourceVo;
import shz.spring.model.PageVo;

class ResourceClientTest {
    @Test
    void upload() {
        //模拟资源服务
        JdbcService jdbcService = new JdbcService();
        jdbcService.setDataSource(
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://192.168.1.105:3306/resource?useUnicode=true&characterEncoding=utf8&useSSL=false&autoReconnect=true&allowMultiQueries=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true",
                "root",
                "root"
        );
        ResourceServer resourceServer = new DefaultResourceServer("D:/usr/local/sources", "/profile", jdbcService);

        //模拟客户端
        ResourceClient resourceClient = new DefaultResourceClient() {
            @Override
            protected CheckResult check(SysResource resource) {
                return resourceServer.check(resource);
            }

            @Override
            protected void save(SysResource resource) {
                resourceServer.save(resource);
            }

            @Override
            public PageInfo<SysResource> page(PageVo<QueryResourceVo, SysResource> pageVo) {
                return resourceServer.page(pageVo);
            }
        };

        //构建一个上传的资源
        SysResource resource = new SysResource();
        resource.setData("this is a test".getBytes());
        resource.setType("TEST_TYPE");
        resource.setUseSecondPath(true);
        resource.setFilename("resource-test.txt");
        resource.setExtension(".txt");
        resourceClient.upload(resource);

        //上传相同资源
        SysResource resource2 = new SysResource();
        resource2.setData("this is a test".getBytes());
        resource2.setType("TEST_TYPE");
        resource2.setUseSecondPath(true);
        resource2.setFilename("resource-test.txt");
        resource2.setExtension(".txt");
        resourceClient.upload(resource2);
        //检查上传相同资源id是否相同
        Assertions.assertEquals(resource.getId(), resource2.getId());

        SysResource resource3 = new SysResource();
        resource3.setData("this is a test3".getBytes());
        resource3.setType("TEST_TYPE");
        resource3.setUseSecondPath(true);
        resource3.setFilename("resource-test3.txt");
        resource3.setExtension(".txt");
        resourceClient.upload(resource3);

        PageVo<QueryResourceVo, SysResource> pageVo = new PageVo<>();
        PageInfo<SysResource> page = resourceClient.page(pageVo);
        page.getData().forEach(System.out::println);
    }
}