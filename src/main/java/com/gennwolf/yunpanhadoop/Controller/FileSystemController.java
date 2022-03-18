package com.gennwolf.yunpanhadoop.Controller;

import com.gennwolf.yunpanhadoop.domain.HDFSFile;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class FileSystemController {
    //实例化Configuration和FileSystem
    public FileSystem getFileSystem() throws IOException {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://192.168.170.111:9000");
        return FileSystem.get(conf);
    }

    //登录
    @RequestMapping("/login")
    public String login(HttpServletRequest request, HttpSession session, Model model) throws IOException {
        String username = request.getParameter("username");
        String userpasswd = request.getParameter("userpasswd");
        if(!loginCheck(username, userpasswd)) {
            model.addAttribute("status", "用户名或密码错误！");
            return "index";
        }
        session.setAttribute("LOGIN_STATUS", username);
        session.setAttribute("path", "/" + username);
        model.addAttribute("currentpath", "/" + username);
        model.addAttribute("filelist", getFileList("/" + username));
        return "myfiles";
    }

    //退出登录
    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("path");
        session.removeAttribute("LOGIN_STATUS");
        return "index";
    }

    //注册
    @RequestMapping("/register")
    public String register(HttpServletRequest request, Model model) throws IOException {
        String username = request.getParameter("username");
        String userpasswd = request.getParameter("userpasswd");
        String userpasswd_confirm = request.getParameter("userpasswd_confirm");
        if(!userpasswd.equals(userpasswd_confirm)) {
            model.addAttribute("status", "两次密码输入不一致！");
            return "register";
        }
        if(userExistCheck(username)) {
            model.addAttribute("status", "用户已存在！");
            return "register";
        }
        insertUserInfoToFile(username, userpasswd);
        mkdir("/" + username);
        model.addAttribute("status", "注册成功，请登录！");
        return "index";
    }

    //创建目录
    @RequestMapping("/makeDirectory")
    public String makeDirectory(HttpSession session, HttpServletRequest request, Model model) throws IOException {
        String dirname = request.getParameter("dirname");
        if(fileExist(session.getAttribute("path").toString() + "/" + dirname))
            model.addAttribute("warning", "alert(\"该目录已存在，请重新输入目录名！\");");
        else {
            mkdir(session.getAttribute("path").toString() + "/" + dirname);
            model.addAttribute("warning", "alert(\"创建成功！\");");
        }
        model.addAttribute("currentpath", session.getAttribute("path").toString());
        model.addAttribute("filelist", getFileList(session.getAttribute("path").toString()));
        return "myfiles";
    }

    //删除文件或目录
    @RequestMapping("/deleteFile")
    public String deleteFile(HttpSession session, HttpServletRequest request, Model model) throws IOException {
        if(!loginStatusCheck(session)) {
            model.addAttribute("status", "此操作需要你登录！");
            return "index";
        }
        String filename = request.getParameter("filename");
        delete(session.getAttribute("path").toString() + "/" + filename);
        model.addAttribute("warning", "alert(\"删除成功！\");");
        model.addAttribute("currentpath", session.getAttribute("path").toString());
        model.addAttribute("filelist", getFileList(session.getAttribute("path").toString()));
        return "myfiles";
    }

    //注销账户
    @RequestMapping("/deleteUser")
    public String deleteUser(HttpSession session, Model model) throws IOException {
        String username = session.getAttribute("path").toString().split("/")[1];
        delete("/" + username);
        removeUserFromFile(username);
        model.addAttribute("warning", "alert(\"注销成功！\");");
        session.removeAttribute("path");
        session.removeAttribute("LOGIN_STATUS");
        return "index";
    }

    //返回上一级目录
    @RequestMapping("/back")
    public String back(HttpSession session, HttpServletRequest request, Model model) throws IOException {
        if(!loginStatusCheck(session)) {
            model.addAttribute("status", "此操作需要你登录！");
            return "index";
        }
        String currentpath = session.getAttribute("path").toString();
        String[] pathsplit = currentpath.split("/");
        if(pathsplit.length == 2) {
            model.addAttribute("warning", "alert(\"当前已经是根目录！\");");
            model.addAttribute("currentpath", session.getAttribute("path").toString());
            model.addAttribute("filelist", getFileList(session.getAttribute("path").toString()));
            return "myfiles";
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i < pathsplit.length - 1; i++)
            sb.append("/").append(pathsplit[i]);
        session.setAttribute("path", sb.toString());
        model.addAttribute("currentpath", session.getAttribute("path").toString());
        model.addAttribute("filelist", getFileList(session.getAttribute("path").toString()));
        return "myfiles";
    }

    //处理文件和目录请求，如果是目录，则跳转到对应目录中去，如果是文件，则下载文件
    @RequestMapping("/fileHandle")
    public String fileHandle(HttpSession session, HttpServletRequest request, Model model, HttpServletResponse response) throws IOException {
        if(!loginStatusCheck(session)) {
            model.addAttribute("status", "此操作需要你登录！");
            return "index";
        }
        String filename = request.getParameter("filename");
        String filetype = request.getParameter("type");
        if(filetype.equals("目录")) {
            session.setAttribute("path", session.getAttribute("path").toString() + "/" + filename);
            model.addAttribute("currentpath", session.getAttribute("path").toString());
            model.addAttribute("filelist", getFileList(session.getAttribute("path").toString()));
            return "myfiles";
        }
        FileSystem fs = getFileSystem();
        FSDataInputStream in = fs.open(new Path(session.getAttribute("path").toString() + "/" + filename));
        response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
        byte[] buff = new byte[2048];
        int bytesRead;
        while((bytesRead = bufferedInputStream.read(buff, 0, buff.length)) != -1)
            bufferedOutputStream.write(buff, 0, bytesRead);
        bufferedInputStream.close();
        bufferedOutputStream.close();
        fs.close();
        return null;
    }

    //文件上传
    @RequestMapping("/upload")
    public String upload(HttpSession session, HttpServletRequest request, Model model) throws FileUploadException, IOException {
        FileSystem fs = getFileSystem();
        boolean isExist = false;
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding("UTF-8");
        List<FileItem> list = upload.parseRequest(request);
        for(FileItem item: list) {
            if(!item.isFormField()) {
                String filename = item.getName();
                if(filename == null || filename.trim().equals(""))
                    continue;
                isExist = fileExist(session.getAttribute("path").toString() + "/" + filename);
                if(isExist)
                    continue;
                fs = getFileSystem();
                InputStream in = item.getInputStream();
                FSDataOutputStream out = fs.create(new Path(session.getAttribute("path").toString() + "/" + filename));
                byte[] buff = new  byte[2048];
                int bytesRead = 0;
                while((bytesRead = in.read(buff)) > 0)
                    out.write(buff, 0, bytesRead);
                in.close();
                out.close();
                item.delete();
            }
        }
        model.addAttribute("currentpath", session.getAttribute("path").toString());
        model.addAttribute("filelist", getFileList(session.getAttribute("path").toString()));
        if(isExist)
            model.addAttribute("warning", "alert(\"网盘有同名文件，建议更改文件名！\");");
        else
            model.addAttribute("warning", "alert(\"文件上传成功！\");");
        fs.close();
        return "myfiles";
    }

    //创建目录核心操作
    public void mkdir(String path) throws IOException {
        FileSystem fs = getFileSystem();
        Path srcPath = new Path(path);
        fs.mkdirs(srcPath);
        fs.close();
    }

    //判断文件或目录是否存在
    public boolean fileExist(String path) throws IOException {
        FileSystem fs = getFileSystem();
        boolean isExist = fs.exists(new Path(path));
        fs.close();
        return isExist;
    }

    //删除文件或目录核心操作
    public void delete(String path) throws IOException {
        FileSystem fs = getFileSystem();
        Path srcPath = new Path(path);
        fs.delete(srcPath, true);
        fs.close();
    }

    //获取特定路径的所有文件
    public List<HDFSFile> getFileList(String path) throws IOException {
        FileSystem fs = getFileSystem();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<HDFSFile> fileList = new ArrayList<>();
        FileStatus[] fileStatuses = fs.listStatus(new Path(path));
        for(FileStatus fileStatus: fileStatuses) {
            HDFSFile file = new HDFSFile();
            file.setName(fileStatus.getPath().getName());
            file.setDate(format.format(fileStatus.getModificationTime()));
            if(fileStatus.isDirectory())
                file.setType("目录");
            else
                file.setType("文件");
            fileList.add(file);
        }
        //排序，目录放前面，文件放后面
        Collator collator = Collator.getInstance(Locale.CHINA);
        fileList.sort((f1, f2) -> (collator.compare(f1.getType(), f2.getType())));
        return fileList;
    }

    //检查用户名和密码是否正确
    public boolean loginCheck(String username, String userpasswd) throws IOException {
        FileSystem fs = getFileSystem();
        Path srcPath = new Path("/userinfo.dat");
        FSDataInputStream in = fs.open(srcPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = "";
        while((line = reader.readLine()) != null) {
            String[] userinfo = line.split(",");
            if(userinfo[0].equals(username) && userinfo[1].equals(userpasswd)) {
                fs.close();
                return true;
            }
        }
        fs.close();
        return false;
    }

    //从userinfo.dat中移除用户
    public void removeUserFromFile(String username) throws IOException {
        FileSystem fs = getFileSystem();
        Path srcPath = new Path("/userinfo.dat");
        FSDataInputStream in = fs.open(srcPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while((line = reader.readLine()) != null) {
            String[] userinfo = line.split(",");
            if(userinfo[0].equals(username))
                continue;
            sb.append(line).append("\n");
        }
        in.close();
        FSDataOutputStream out = fs.create(srcPath, true);
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.close();
        fs.close();
    }

    //把新注册的用户信息插入到userinfo.dat中
    public void insertUserInfoToFile(String username, String userpasswd) throws IOException {
        FileSystem fs = getFileSystem();
        Path srcPath = new Path("/userinfo.dat");
        FSDataOutputStream out = fs.append(srcPath);
        String userinfo = username + "," + userpasswd + "\n";
        out.write(userinfo.getBytes(StandardCharsets.UTF_8));
        out.close();
        fs.close();
    }

    //判断用户是否已经注册
    public boolean userExistCheck(String username) throws IOException {
        FileSystem fs = getFileSystem();
        Path srcPath = new Path("/userinfo.dat");
        FSDataInputStream in = fs.open(srcPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = "";
        while((line = reader.readLine()) != null) {
            String[] userinfo = line.split(",");
            if(userinfo[0].equals(username)) {
                fs.close();
                return true;
            }
        }
        fs.close();
        return false;
    }

    //当前登录状态检查
    public boolean loginStatusCheck(HttpSession session) {
        return session.getAttribute("LOGIN_STATUS") != null;
    }
}
