package org.example.reggie.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.R;
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
 * 文件上传的Controller
 */

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    //定义转存后的文件存储路径(从配置文件中动态获取)
    @Value("${reggie.path}")
    private String basePath;
    /**
     * 文件上传
     * @param file 方法名必须要与前端一致
     * @return 转存后的文件名
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //1，获取源文件名
        String originalFileName=file.getOriginalFilename();
        //2，截取源文件名的后缀
        String suffix=originalFileName.substring(originalFileName.lastIndexOf("."));
        //3，使用UUID随机生成新的文件名（与以源后缀名拼接组成）防止文件名覆盖的发生
        String fileName= UUID.randomUUID().toString()+suffix;
        //4，判断配置文件中的存储路径是否存在，如果不存在，创建
        //根据存储路径创建一个目录
        File dir =new File(basePath);
        if (!dir.exists()){
            //目录不存在，创建
            dir.mkdirs();
        }
        //5，将存储路径与新文件名拼接就是文件上传后的保存路径
        //6，将文件转存到指定位置
        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //7，返回
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //获取输入流
            FileInputStream fileInputStream=new FileInputStream(basePath+name);
            //获取输出流
            ServletOutputStream outputStream = response.getOutputStream();
            //通过输入流获取文件，通过输出流输出图片
            //定义格式
            response.setContentType("image/jpeg");
            int len=0;
            byte[] b=new byte[1024];
            while ((len=fileInputStream.read(b))!=-1){
                outputStream.write(b,0,len);
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
