package shz.resource.server;

import shz.core.NullHelp;
import shz.core.ToList;
import shz.core.ToMap;
import shz.core.ToSet;
import shz.core.constant.CommonConstant;
import shz.core.io.FileHelp;
import shz.core.io.IOHelp;
import shz.core.model.PageInfo;
import shz.core.msg.ClientFailureMsg;
import shz.core.msg.ServerFailureMsg;
import shz.jdbc.JdbcService;
import shz.orm.enums.Condition;
import shz.resource.entity.SysResource;
import shz.resource.vo.QueryResourceVo;
import shz.spring.DownloadHelp;
import shz.spring.model.PageVo;

import java.io.*;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;

public class DefaultResourceServer implements ResourceServer {
    /**
     * 资源保存基础路径(非win系统首字符设为/,尾字符不为/)
     */
    protected final String basePath;
    /**
     * 映射可访问的基础路径
     */
    protected final String secondPath;
    protected final JdbcService jdbcService;

    public DefaultResourceServer(String basePath, String secondPath, JdbcService jdbcService) {
        if (NullHelp.isBlank(basePath) || (CommonConstant.isWindows && !basePath.contains(":\\") && !basePath.contains(":/")))
            this.basePath = FileHelp.formatPath(new File(System.getProperty("user.home"), "resources").getAbsolutePath());
        else {
            String s = FileHelp.formatPath(basePath);
            if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
            if (!CommonConstant.isWindows && !s.startsWith("/")) s = "/" + s;
            this.basePath = s;
        }

        if (NullHelp.isBlank(secondPath)) this.secondPath = "";
        else {
            secondPath = FileHelp.formatPath(secondPath);
            if (secondPath.endsWith("/")) secondPath = secondPath.substring(0, secondPath.length() - 1);
            if (!secondPath.startsWith("/")) secondPath = "/" + secondPath;
            this.secondPath = secondPath;
        }

        this.jdbcService = jdbcService;
    }

    @Override
    public final CheckResult check(SysResource resource) {
        ClientFailureMsg.requireNonBlank(resource.getType(), "文件类型为空");
        ClientFailureMsg.requireNonBlank(resource.getFilename(), "文件名为空");
        ClientFailureMsg.requireNonBlank(resource.getMd5(), "文件md5值为空");

        SysResource select = getByMd5(resource.getMd5());

        CheckResult result = new CheckResult();
        if (select == null) {
            result.upload = true;
            return result;
        }
        select.setUseSecondPath(resource.isUseSecondPath());
        File file = getFile(select);

        //文件已经被删除
        if (file == null) {
            result.upload = true;
            return result;
        }

        result.id = select.getId();

        Long offset = select.getOffset();
        if (offset != 0L) {
            //文件偏移量不为0,需要上传
            result.upload = true;
            result.offset = offset;
        }

        result.path = select.getPath();
        return result;
    }

    /**
     * 获取资源文件
     */
    private File getFile(SysResource resource) {
        //获取资源目录
        File folder = resourceFolder(resource);
        File file = null;
        if (folder.exists() && folder.isDirectory()) {
            file = resourceFile(folder, resource);
            if (!file.exists() || !file.isFile()) file = null;
        }

        //资源已删除,删除资源表中对应的数据
        if (file == null) jdbcService.deleteById(SysResource.class, resource.getId());
        return file;
    }

    /**
     * 资源目录
     */
    private File resourceFolder(SysResource resource) {
        return resource.isUseSecondPath() ? new File(basePath + secondPath, resource.getType()) : new File(basePath, resource.getType());
    }

    /**
     * 资源文件
     */
    private File resourceFile(File folder, SysResource resource) {
        return resource.getExtension() == null ? new File(folder, resource.getMd5()) : new File(folder, resource.getMd5() + resource.getExtension());
    }

    @Override
    public final void save(SysResource resource) {
        ClientFailureMsg.requireNonEmpty(resource.getData(), "上传资源为空");

        if (resource.getId() != null) {
            //获取资源偏移
            Long offset = resource.getOffset();
            ClientFailureMsg.requireNonNull(offset, "非法操作,缺少文件偏移");

            SysResource select = getById(resource.getId());
            ClientFailureMsg.requireNonNull(select, "文件已被删除,请重新上传全部数据");
            ClientFailureMsg.requireNon(!select.getOffset().equals(offset), "非法操作,文件偏移异常,请重新检查资源");
            select.setUseSecondPath(resource.isUseSecondPath());

            //资源表存在该资源
            //检查磁盘文件
            File file = getFile(select);
            ClientFailureMsg.requireNonNull(file, "文件已被删除,请重新上传全部数据");

            select.setOffset(copyTo(resource.getData(), file, offset));
            if (!select.getOffset().equals(offset)) jdbcService.updateById(select);
            checkAfterSave(select);
            return;
        }

        ClientFailureMsg.requireNonBlank(resource.getType(), "文件类型为空");
        ClientFailureMsg.requireNonBlank(resource.getFilename(), "文件名为空");
        ClientFailureMsg.requireNonBlank(resource.getMd5(), "文件md5值为空");

        //资源表不存在该资源,此时data应该是文件全部数据(正常操作情况下)
        //获取资源目录
        File folder = resourceFolder(resource);
        ServerFailureMsg.requireNon(!folder.mkdirs() && !folder.exists(), "创建资源目录失败:%s", folder.getAbsoluteFile());

        //获取资源文件
        File file = resourceFile(folder, resource);
        String path = FileHelp.formatPath(file.getAbsolutePath());
        //设置资源相对路径
        resource.setPath(path.substring(basePath.length()));
        //设置资源大小
        resource.setSize((long) resource.getData().length);

        long offset;
        if (file.exists()) {
            //资源文件存在,在上传成功但保存资源表失败的情况
            offset = file.length();
            ClientFailureMsg.requireNon(resource.getData().length < offset, "非法操作,文件数据异常");
            //文件完整
            if (resource.getData().length == offset) offset = 0L;
            else if (offset == 0L) offset = copyTo(resource.getData(), file, 0L);
            else {
                //续传,特殊情况截取数据保存
                byte[] newData = new byte[(int) (resource.getData().length - offset)];
                System.arraycopy(resource.getData(), (int) offset, newData, 0, newData.length);
                offset = copyTo(newData, file, offset);
            }
        } else offset = copyTo(resource.getData(), file, 0L);

        //设置资源偏移
        resource.setOffset(offset);
        //保存资源信息
        jdbcService.insert(resource);
        checkAfterSave(resource);
    }

    /**
     * 拷贝数据到指定文件
     */
    private long copyTo(byte[] data, File file, long offset) {
        BufferedOutputStream bos;
        if (offset > 0L)
            bos = IOHelp.newBufferedOutputStream(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        else bos = IOHelp.newBufferedOutputStream(file.toPath());
        return IOHelp.read(new ByteArrayInputStream(data), bos);
    }

    private void checkAfterSave(SysResource resource) {
        ClientFailureMsg.requireNon(resource.getOffset() == -1L || resource.getOffset() > 0L, "上传失败,请重新上传");
    }

    @Override
    public final SysResource getById(Long id) {
        return jdbcService.selectById(SysResource.class, id);
    }

    @Override
    public final SysResource getByMd5(String md5) {
        return md5 == null ? null : jdbcService.selectOneByColumn(SysResource.class, true, null, "md5", md5, Condition.EQ, Boolean.FALSE);
    }

    @Override
    public final List<SysResource> getByIds(Collection<Long> ids) {
        return jdbcService.selectByIds(SysResource.class, ids);
    }

    @Override
    public final void download(Long id, boolean useSecondPath, Long offset) {
        if (id == null) return;
        SysResource resource = getById(id);
        if (resource == null) return;
        resource.setUseSecondPath(useSecondPath);
        File file = getFile(resource);
        if (file == null) return;
        DownloadHelp.download(contentType(resource.getExtension()), resource.getFilename(), IOHelp.newBufferedInputStream(file.toPath()), offset == null ? 0 : offset);
    }

    protected String contentType(String extension) {
        if (extension != null) {
            switch (extension) {
                case ".doc":
                case ".docx":
                    return "application/msword";
                case ".xls":
                case ".xlsx":
                    return "application/vnd.ms-excel";
                case ".pdf":
                    return "application/pdf";
                case ".ppt":
                    return "application/vnd.ms-powerpoint";
                case ".bmp":
                    return "image/bmp";
                case ".jpg":
                    return "image/jpg";
                case ".jpeg":
                    return "image/jpeg";
                case ".gif":
                    return "image/gif";
                case ".png":
                    return "image/png";
                case ".svg":
                    return "text/html";
            }
        }
        return "application/octet-stream;charset=ISO8859-1";
    }

    @Override
    public final void deleteByIds(Collection<Long> ids) {
        List<SysResource> resources = getByIds(ids);
        if (NullHelp.isEmpty(resources)) return;
        List<Long> existIds = ToList.explicitCollect(resources.stream().map(SysResource::getId), resources.size());
        jdbcService.batchDeleteById(SysResource.class, existIds);
        idFile(resources).values().stream().filter(Objects::nonNull).forEach(File::delete);
    }

    /**
     * id-资源文件
     */
    private Map<Long, File> idFile(Collection<SysResource> resources) {
        if (NullHelp.isEmpty(resources)) return Collections.emptyMap();
        File baseFolder = new File(basePath);
        if (!baseFolder.exists() || !baseFolder.isDirectory()) return Collections.emptyMap();
        Map<String, Boolean> typeExistMap = new HashMap<>();
        Map<String, File> typeFolderMap = new HashMap<>();
        return ToMap.explicitCollect(
                resources.stream().filter(detail -> Objects.nonNull(detail.getId())),
                SysResource::getId,
                resource -> {
                    String type = resource.getType();
                    Boolean typeExist = typeExistMap.get(type);
                    File folder;
                    if (typeExist == null) {
                        folder = new File(basePath, type);
                        typeExist = folder.exists() && folder.isDirectory();
                        typeExistMap.put(type, typeExist);
                        typeFolderMap.put(type, folder);
                    }

                    if (!typeExist) return null;
                    else folder = typeFolderMap.get(type);

                    File file = resourceFile(folder, resource);
                    return !file.exists() || !file.isFile() ? null : file;
                },
                resources.size()
        );
    }

    @Override
    public final void clearTable(Collection<Long> ids) {
        List<SysResource> resources = getByIds(ids);
        if (NullHelp.isEmpty(resources)) return;
        Map<Long, File> idFileMap = idFile(resources);
        jdbcService.batchDeleteById(SysResource.class, ToList.collect(resources.stream().map(SysResource::getId).filter(id -> id != null && idFileMap.get(id) == null)));
    }

    @Override
    public final void clearFile(Collection<String> types) {
        if (NullHelp.isEmpty(types)) return;
        Set<String> allTypes = ToSet.explicitCollect(types.stream().filter(NullHelp::nonBlank), types.size());
        if (allTypes.isEmpty()) return;
        File baseFolder = new File(basePath);
        if (!baseFolder.exists() || !baseFolder.isDirectory()) return;
        List<SysResource> resources = jdbcService.selectListByColumn(SysResource.class, "type", allTypes, Condition.IN);
        Set<String> existTypes;
        if (NullHelp.isEmpty(resources)) existTypes = Collections.emptySet();
        else existTypes = ToSet.explicitCollect(resources.stream().map(SysResource::getType), allTypes.size());

        if (!existTypes.isEmpty()) allTypes.removeAll(existTypes);
        if (!allTypes.isEmpty()) deleteTypes(allTypes, type -> File::isFile);
        if (existTypes.isEmpty()) return;

        //资源表存在的路径
        Map<String, Set<String>> existPaths = ToMap.get(existTypes.size()).build();
        Map<Long, String> idTypeMap = ToMap.explicitCollect(
                resources.stream(),
                SysResource::getId,
                SysResource::getType,
                resources.size()
        );

        idFile(resources).forEach((id, file) -> {
            if (file == null) return;
            String type = idTypeMap.get(id);
            if (type == null) return;
            Set<String> set = existPaths.computeIfAbsent(type, k -> new HashSet<>());
            set.add(file.getAbsolutePath());
        });

        deleteTypes(existTypes, type -> {
            Set<String> paths = existPaths.get(type);
            if (NullHelp.isEmpty(paths)) return File::isFile;
            return f -> f.isFile() && !paths.contains(f.getAbsolutePath());
        });
    }

    /**
     * 删除指定类型的文件
     */
    private void deleteTypes(Set<String> types, Function<String, FileFilter> func) {
        Map<String, File> typeFolderMap = new HashMap<>();
        types.forEach(type -> {
            File folder = typeFolderMap.computeIfAbsent(type, k -> new File(basePath, k));
            if (!folder.isDirectory()) return;
            File[] files = folder.listFiles(func.apply(type));
            if (NullHelp.nonEmpty(files)) for (File file : files) file.delete();
        });
    }

    @Override
    public final PageInfo<SysResource> page(PageVo<QueryResourceVo, SysResource> pageVo) {
        return jdbcService.page(pageVo.map(), SysResource.class, null, jdbcService.whereSql(jdbcService.nonNullClassInfo(SysResource.class), pageVo.getData(), Boolean.FALSE, true));
    }
}
