package com.defold.extender;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.yaml.snakeyaml.Yaml;

@RestController
public class ExtenderController {

    @Value("${extender.config-url}")
    private URL configUrl;

    @RequestMapping("/")
    public String index() {
        return "Extender";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/build/{platform}")
    public void buildEngine(MultipartHttpServletRequest req, HttpServletResponse resp,
                            @PathVariable("platform") String platform) throws IOException, InterruptedException {
        Set<String> keys = req.getMultiFileMap().keySet();

        File src = Files.createTempDirectory("engine").toFile();

        try {
            for (String k : keys) {
                MultipartFile mpf = req.getMultiFileMap().getFirst(k);
                File f = new File(src, mpf.getName());
                f.getParentFile().mkdirs();
                FileUtils.copyInputStreamToFile(mpf.getInputStream(), f);
            }

            Configuration config = new Yaml().loadAs(IOUtils.toString(configUrl), Configuration.class);
            Extender e = new Extender(config, platform, src);
            File exe = e.buildEngine();
            FileUtils.copyFile(exe, resp.getOutputStream());
            e.dispose();
        } finally {
            FileUtils.deleteDirectory(src);
        }
    }
}

