package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传和下载
 * @author MiracloW
 * @date 2022-10-12 14:18
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;
    /**
     * 请求头
     * Form Data
     * ------WebKitFormBoundaryeIFVPTXoZeOqhnTM
     * Content-Disposition: form-data; name="file"; filename="0a3b3288-3446-4420-bbff-f263d0c02d8e.jpg"
     * Content-Type: image/jpeg
     *
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){//file必须跟请求头中form表单中一致
        log.info(file.toString());

        //文件的原始文件名
        String originalFilename = file.getOriginalFilename();
        //获取文件名最后的格式 .jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用原文件名有问题，会覆盖
        //使用UUID重新生成文件名
        String fileName = UUID.randomUUID().toString()+suffix;

        //创建一个目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在
        if(!dir.exists()){
            dir.mkdirs();
        }

        try{
            //将临时文件转存到指定目录（配置文件下）
            file.transferTo(new File(basePath+fileName));
        }catch (IOException e){
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            //设置响应内容格式：图片
            response.setContentType("image/jpeg");

            //读文件
            int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
