package shz.resource.client;

import org.springframework.web.multipart.MultipartFile;
import shz.core.Coder;
import shz.core.NullHelp;
import shz.core.PRException;
import shz.core.ToList;
import shz.core.hash.Hash;
import shz.core.model.PageInfo;
import shz.core.msg.ServerFailureMsg;
import shz.resource.entity.SysResource;
import shz.resource.server.CheckResult;
import shz.resource.server.ResourceServer;
import shz.resource.vo.QueryResourceVo;
import shz.spring.BeanContainer;
import shz.spring.model.PageVo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DefaultResourceClient implements ResourceClient {
    @Override
    public final void upload(SysResource resource) {
        if (resource == null || NullHelp.isEmpty(resource.getData())) return;
        if (resource.getMd5() == null) resource.setMd5(Coder.hexEncode(Hash.MD5.hash(resource.getData())));

        CheckResult checkResult = check(resource);

        if (!checkResult.isUpload()) {
            resource.setId(checkResult.getId());
            resource.setPath(checkResult.getPath());
            return;
        }

        resource.setId(checkResult.getId());
        long offset = checkResult.getOffset();
        resource.setOffset(offset);

        if (offset > 0L) {
            //偏移数据
            byte[] newData = new byte[(int) (resource.getData().length - offset)];
            System.arraycopy(resource.getData(), (int) offset, newData, 0, newData.length);
            resource.setData(newData);
        }

        save(resource);
    }

    protected CheckResult check(SysResource resource) {
        return nonNullServer().check(resource);
    }

    /**
     * 默认上传本地服务
     */
    private ResourceServer nonNullServer() {
        ResourceServer resourceServer = BeanContainer.get(ResourceServer.class);
        ServerFailureMsg.requireNonNull(resourceServer, "缺少ResourceServer实例");
        return resourceServer;
    }

    protected void save(SysResource resource) {
        nonNullServer().save(resource);
    }

    @Override
    public final SysResource upload(ResourceType type, MultipartFile file, boolean useSecondPath) {
        if (file == null) return null;
        byte[] data;
        try {
            data = file.getBytes();
        } catch (Exception e) {
            throw PRException.of(e);
        }
        if (NullHelp.isEmpty(data)) return null;

        SysResource resource = new SysResource();
        resource.setData(data);
        resource.setMd5(Coder.hexEncode(Hash.MD5.hash(data)));
        resource.setType(type.getCode());
        resource.setUseSecondPath(useSecondPath);

        String filename = file.getOriginalFilename();
        if (NullHelp.isBlank(filename)) filename = file.getName();
        resource.setFilename(filename);

        int idx = filename.lastIndexOf('.');
        if (idx != -1) resource.setExtension(filename.substring(idx + 1));

        type.check(resource);
        upload(resource);
        return resource;
    }

    @Override
    public final List<SysResource> upload(ResourceType type, MultipartFile[] files, boolean useSecondPath) {
        if (NullHelp.isEmpty(files)) return Collections.emptyList();
        return ToList.explicitCollect(Arrays.stream(files).map(file -> upload(type, file, useSecondPath)), files.length);
    }

    @Override
    public SysResource getById(Long id) {
        return nonNullServer().getById(id);
    }

    @Override
    public SysResource getByMd5(String md5) {
        return nonNullServer().getByMd5(md5);
    }

    @Override
    public List<SysResource> getByIds(Collection<Long> ids) {
        return nonNullServer().getByIds(ids);
    }

    @Override
    public void download(Long id, boolean useSecondPath, Long offset) {
        nonNullServer().download(id, useSecondPath, offset);
    }

    @Override
    public void deleteByIds(Collection<Long> ids) {
        nonNullServer().deleteByIds(ids);
    }

    @Override
    public void clearTable(Collection<Long> ids) {
        nonNullServer().clearTable(ids);
    }

    @Override
    public void clearFile(Collection<String> types) {
        nonNullServer().clearFile(types);
    }

    @Override
    public PageInfo<SysResource> page(PageVo<QueryResourceVo, SysResource> pageVo) {
        return nonNullServer().page(pageVo);
    }
}
