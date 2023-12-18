### 20231219
更新了相关的Maven依赖，更新JDK版本为11，更新Hadoop版本为3.3.6，更新SpringBoot版本为2.7.18

# 【Hadoop/Java】基于HDFS的Java Web网络云盘
## [https://blog.csdn.net/weixin_53395564/article/details/123575761](https://blog.csdn.net/weixin_53395564/article/details/123575761)

本人BNUZ大学生萌新，水平不足，还请各位多多指教！

## 实验目的
1.  熟悉HDFS Java API的使用；
2.  能使用HDFS Java API编写应用程序

## 实验要求
云盘系统通过互联网为企业和个人提供信息的储存、读取、下载等服务，具有安全稳定、海量存储的特点。根据用户群定位，云盘系统可以划分为公有云盘、社区云盘、私有云盘等。请利用HDFS Java API，编写一个云盘系统，要求实现功能如下：
![云盘系统基本功能图](https://img-blog.csdnimg.cn/6deb76f475bd4885a0dd3017f8cac5ce.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

## 环境
Ubuntu 22.04LTS + Java (OpenJDK 11) + IDEA Ultimate 2023.3.1 + Hadoop 3.3.6

## 项目下载
Github仓库：[https://github.com/gennwolf/yunpan-hadoop](https://github.com/gennwolf/yunpan-hadoop)

## 使用框架以及Web服务器
Maven + SpringBoot 2.7.18 + SpringMVC + Apache Tomcat 9.0.83
前端模板来源：[https://colorlib.com](https://colorlib.com) (使用了Bootstrap + jQuery)

## 分布式集群配置
有3个节点，每个节点的主机名、IP以及担任的角色如下表所示：
|节点|主机名|IP|角色|
|:---------:|:---------:|:---------:|:---------:|
|1|Master|192.168.170.111|NameNode, SecondaryNameNode, DataNode, ResourceManager, NodeManager|
|2|Slave1|192.168.170.112|DataNode, NodeManager|
|3|Slave2|192.168.170.113|DataNode, NodeManager|

|网关|子网掩码|
|:---------:|:---------:|
|192.168.170.2|255.255.255.0|

##  实验步骤

1. 启动Hadoop集群，分别到各个节点使用jps命令查看进程是否与上表匹配：
![启动集群](https://img-blog.csdnimg.cn/6ec9c56271714e62a9f129172134730c.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
下图为Master节点的Java进程，和上表对应节点的角色相匹配：
![查看Master节点的进程](https://img-blog.csdnimg.cn/5d2117539a0b4b0989252bdf82d1276f.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
可以在Master上用ssh连接到其他节点查看进程

2. 在HDFS的根目录下创建一个文件userinfo.dat用于保存用户信息，用户信息包含用户名和密码，用逗号分隔，该文件用于云盘的登录/注册等操作，我们往里面添加两个用户spring和summer，密码都为123456，如下图：
![查看userinfo.dat文件内容](https://img-blog.csdnimg.cn/a6524cc74ef442d697ca47cf42d12a72.png#pic_center)

3.	每个用户只能访问自己的资源，用以前上数据库系统原理课程的知识来说，就是每一名用户都有自己的文件目录视图，在本次实验中，我的想法是，每一名用户对应一个目录，比如说用户spring对应HDFS中的目录/spring，用户summer对应/summer，用户spring是不能操作用户summer的文件目录的，这样就实现了用户之间的隔离性，确保每位用户访问到的是自己的资源，我们在/spring下创建一个文本文件test.txt，再创建一个目录dd，在dd下也创建一个文本文件test.txt，创建文件可以用touch命令，创建目录可以用mkdir命令，我们递归打印/spring目录下的所有文件查看是否创建成功，到这里就完成了文件的准备工作：
![递归打印/spring目录所有内容](https://img-blog.csdnimg.cn/6a7203027c584b1d815b046c30518091.png#pic_center)

4.	打开IDEA，创建SpringBoot项目，配置好Web相关依赖后，加入Hadoop依赖，依赖的version对应当前系统中的Hadoop版本：

```xml
	<dependency>
	    <groupId>org.apache.hadoop</groupId>
	    <artifactId>hadoop-client</artifactId>
	    <version>3.3.6</version>
	</dependency>
```

5.	项目的结构如图：
![项目结构](https://img-blog.csdnimg.cn/6844606ed98f4e6894ad252e1738ebc9.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
关键的文件及目录说明如下表所示：

|名称|类型|目录|
|:---------:|:---------:|:---------:|
|Controller|目录|用于存放Controller类|
|FileSystemController|Java类|用于接收前端的请求，从而进行请求处理以及HDFS操作的Controller类|
|PageController|Java类|用于接收前端请求，实现网页跳转的Controller类|
|domain|目录|用于存放数据类|
|HDFSFile|Java类|用于描述HDFS文件信息的数据类，含有三个字段，name表示名称，date表示修改日期，type表示类型（文件/目录）|
|YunpanHadoopApplication|Java类|用于启动Springboot项目的类|
|resources.static|目录|用于存放静态资源的目录，本项目的前端的所有静态资源，包括CSS、JS、图片等，都放在此目录下|
|application.properties|配置文件|SpringBoot配置文件|
|webapp.WEB-INF|目录|存放JSP页面|
|pom.xml|配置文件|Maven配置文件|
|...|...|...|

**下面围绕页面来进行功能讲解，本项目页面数量很少，只有3个页面：**

6.	由于操作HDFS，需要频繁创建FileSystem实例，所以我把创建FileSystem实例写成了一个函数，方便以后调用：

```java
    //实例化Configuration和FileSystem
    public FileSystem getFileSystem() throws IOException {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://192.168.170.111:9000");
        return FileSystem.get(conf);
    }
```

7.	另外本web项目全程session绑定，用户登陆后就设置名为LOGIN_STATUS的session，用户退出登录则删除该session。同时也设置一个名为path的session，用于记录当前用户访问的HDFS目录地址

```java
    //当前登录状态检查
    public boolean loginStatusCheck(HttpSession session) {
        return session.getAttribute("LOGIN_STATUS") != null;
    }
```

8.	index.jsp页面以及register.jsp页面
index.jsp页面全貌：
![index.jsp](https://img-blog.csdnimg.cn/3725bdbf7318415e8a5bf4c47f58cab0.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
点击“没有账号？点击此处注册 →”按钮，跳转到注册页面register.jsp，跳转功能用PageController类实现，对应的跳转语句：
前端（前端语句只示例一次）：

```html
	<div class="text-center p-t-136">
		<a class="txt2" href="<%=request.getContextPath()%>/jumpToRegisterPage">
		没有账号？点击此处注册
		<i class="fa fa-long-arrow-right m-l-5" aria-hidden="true"></i>
		</a>
	</div>
```
后台：

```java
    @RequestMapping("/jumpToRegisterPage")
    public String jumpToRegisterPage() {
        return "register";
    }
```
跳转到register.jsp页面，页面全貌：
![register.jsp](https://img-blog.csdnimg.cn/1cec3850a35e4848a1f8cbee6d4092c2.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
输入用户名，密码以及确认密码，每一项都不能为空，前端做了约束：
![约束](https://img-blog.csdnimg.cn/a4812ee5dbd24db982bf4a12584c758c.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
两次密码必须输入一致，后台做了约束，否则注册不成功：
![约束](https://img-blog.csdnimg.cn/d6adbc1d579b4c768e84b56ea40102be.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_16,color_FFFFFF,t_70,g_se,x_16#pic_center)
执行注册操作，用户名为test，密码为test，后台获取前台的数据，注册操作相关代码：

```java
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
```
注册的时候要检查该用户是否已经存在，调用userExistCheck方法来检查：

```java
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
```
若用户之前没有注册过，则允许该用户注册，注册时把用户信息写入到userinfo.dat中，调用insertUserInfoToFile方法：

```java
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
```
同时在HDFS中创建该用户的个人目录/test，调用mkdir方法：

```java
    //创建目录核心操作
    public void mkdir(String path) throws IOException {
        FileSystem fs = getFileSystem();
        Path srcPath = new Path(path);
        fs.mkdirs(srcPath);
        fs.close();
    }
```
注册成功后，跳转到登录页面，并且给出提示要求登录：
![提示](https://img-blog.csdnimg.cn/6f03b4b2e9c14771948fe0f90651259c.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
观察userinfo.dat文件，发现添加了新注册的用户信息：
![查看userinfo.dat](https://img-blog.csdnimg.cn/099d3a927e9c4be89c86b8599cc9de39.png#pic_center)
观察HDFS根目录，发现添加了test用户的个人目录：
![查看根目录](https://img-blog.csdnimg.cn/410dd9a0935f4b2d96c550c93897c13c.png#pic_center)
下面我们用spring用户登录，输入用户名spring，密码123456，点击登录按钮，把相关表单数据提交到Controller的login方法处理：
![登录](https://img-blog.csdnimg.cn/4fd570306e724659bb50d0090644c9fc.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

```java
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
```
登录时后台会检查用户名和密码是否正确，调用loginCheck方法：

```java
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
```
若登录不成功，则刷新登陆页面，并给出文字提示：
![提示](https://img-blog.csdnimg.cn/dbfc4757900347aa8289a2c374e16c20.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
若登陆成功，则设置session，LOGIN_STATUS的值为当前用户的用户名，path设置为用户的个人目录，然后跳转到个人网盘页面myfiles.jsp
9. myfiles.jsp页面：
个人网盘页面，页面全貌：
![myfiles.jsp](https://img-blog.csdnimg.cn/61e354bb8ee041838badeb32957307f6.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
该页面有上传文件按钮，表单左上角显示当前所在路径，表单第一行提供返回上级目录功能，然后就是文件列表，若为目录，则在类型列显示目录，若为文件，则在类型列显示文件，另外显示文件或目录的创建时间，最右边一列提供删除文件或目录的功能，左下角提供创建目录/退出登录以及注销账户的功能
当登录成功后，跳转到个人网盘页面，在页面打印用户个人目录下的文件，我们用的是spring用户，所以我们打印HDFS中/spring路径下的所有文件和目录，以及相关的信息，由于文件创建时间在HDFS中用的是时间戳表示，所以我们要先转换为我们方便看的时间，格式为年-月-日 时:分:秒，每一个文件或者目录相关信息我都放在HDFSFile数据类里面：

```java
import lombok.Data;

@Data
public class HDFSFile {
    private String name;
    private String date;
    private String type;
}
```
通过遍历得到文件和目录列表，用List类型封装HDFSFile数据，然后我们要保证目录始终在文件列表的前面，所以我排了个序，得到最终的文件列表，该方法名为getFileList方法：

```java
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
```
当点击文件列表中的目录的时候，改变session，把path的值变成当前目录，然后刷新页面，显示你选的目录中的文件，若点击文件列表中的文件，则下载该文件，处理文件和目录请求用Controller的fileHandle方法：

```java
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
```
若为下载，需要触发浏览器事件，所以要改变header，让浏览器执行下载操作：
![文件下载](https://img-blog.csdnimg.cn/7c5bd3f60ce94abdb383bfc4111fa093.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
若为目录跳转，则用新路径重新运行getFileList方法，刷新页面：
![点击目录](https://img-blog.csdnimg.cn/b580694655c94c7495dbe601d71b9b5f.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![目录跳转](https://img-blog.csdnimg.cn/89fbac2b12dd43c7b9bfa0198fd455f0.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
因为涉及HDFS核心操作，所以若检测到LOGIN_STATUS session不存在，则跳转到登陆页面要求用户登录，后面的核心操作也同样设定了安全约束：
![安全约束](https://img-blog.csdnimg.cn/0a35c9f5dce24594bbd783671d478d17.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
点击返回上层目录，则更新session，改变path，去掉path的最后一个/右边内容以及最后一个/，然后重新调用getFileList方法，刷新页面，请求交给Controller的back方法处理：
![返回上层目录](https://img-blog.csdnimg.cn/0a0b01664b6b4c4c8dd1c985b529052d.png#pic_center)

```java
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
```
若当前已经是用户个人目录的最上层，若还点返回上层目录，则弹出警告，并刷新页面：
![提示](https://img-blog.csdnimg.cn/a6f0a8fbf3824a21a1b4dcfbfcc5462d.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
点击test.txt最右边的删除按钮，则删除文件，若点击的是目录的删除，则会顺带删除该目录下面的所有文件，删除文件或目录的请求交给Controller的deleteFile方法处理，然后核心操作交给delete方法处理：
![删除文件](https://img-blog.csdnimg.cn/51e2cadcd16b4f63a7b26206ac069925.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

```java
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
```

```java
    //删除文件或目录核心操作
    public void delete(String path) throws IOException {
        FileSystem fs = getFileSystem();
        Path srcPath = new Path(path);
        fs.delete(srcPath, true);
        fs.close();
    }
```
删除成功后弹出提示框，然后刷新页面：
![提示](https://img-blog.csdnimg.cn/8e66af11b3f7420ca2800c83ef85581b.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![删除文件后刷新页面](https://img-blog.csdnimg.cn/4dd25bbc4aaf4aa7a8c0fbae47cfea51.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
点击创建目录按钮，则弹出提示框：
![创建目录](https://img-blog.csdnimg.cn/e989a767f2274704a02eef39cb599d80.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![弹窗](https://img-blog.csdnimg.cn/396e46f1125f417eaa4c5a2f00518fee.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
输入新目录的名称，输入不能为空，否则会弹出警示：
![提示](https://img-blog.csdnimg.cn/315d9ed5e322474190ea1e422da31973.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
若输入目录正确，则创建目录，刷新页面，创建目录请求交给Controller的makeDirectory方法处理：

```java
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
```
![输入目录名](https://img-blog.csdnimg.cn/911f56f6fd214fbab51a5bad229f36d8.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
创建成功后弹出提示：
![提示](https://img-blog.csdnimg.cn/5cf268e927f742e5989c83782f6257bb.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
页面刷新后可以看到刚刚创建的目录：
![刷新页面后的页面](https://img-blog.csdnimg.cn/ddd84ecbb3b64328892b722387d4748d.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
若要创建的目录已经存在，则弹出警告，刷新页面：
![提示](https://img-blog.csdnimg.cn/93262d3de1e44271995eeb0a5be0af3a.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
检测目录或文件是否存在，用fileExist方法处理：

```java
    //判断文件或目录是否存在
    public boolean fileExist(String path) throws IOException {
        FileSystem fs = getFileSystem();
        boolean isExist = fs.exists(new Path(path));
        fs.close();
        return isExist;
    }
```
选择文件，然后点击上传，若没有选择文件直接点击上传，前台做了相关约束，不允许提交请求：
![提示](https://img-blog.csdnimg.cn/96b6274bafde4c53ad39669bf34fc9a1.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
点击选择文件按钮，弹出文件选择框，选中文件，点击打开，则网页显示文件的名称：
![我的文件](https://img-blog.csdnimg.cn/d0afbe2dc2664678bc3a1a432751cef3.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
![文件信息](https://img-blog.csdnimg.cn/c1ba7b6f4a714850bc7ecfe6e24007b8.png#pic_center)
点击上传，由于上传需要时间，在上传过程中，不可进行浏览器的刷新等其他操作，很可能会造成文件上传失败，所以在等待上传的时间里，页面会显示警示文字：
![文件上传提示](https://img-blog.csdnimg.cn/2b77fa755a764aa6a2c714ee0e4d62eb.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
文件上传成功后，弹出提示框提示用户：
![提示](https://img-blog.csdnimg.cn/476064ff432f478781636ef85c0a1ee1.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
然后页面刷新，文件已经在文件列表中：
![页面刷新后](https://img-blog.csdnimg.cn/6414f1b357084c41825eb9c54501a125.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
用命令行查看/spring目录，文件已经上传到HDFS中：
![查看/spring目录](https://img-blog.csdnimg.cn/89954e03a2a24a2fbb3ec93a95e9ca30.png#pic_center)
若上传的文件已经在HDFS中了，如果重复上传，则不允许执行上传操作，并弹出警告，刷新页面：
![提示](https://img-blog.csdnimg.cn/2bac4b8d8a3646e1b6da939b193dac48.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
上传文件操作请求交给Controller的upload方法处理，这里用了commons-io以及commons-fileupload插件，需要在Maven POM文件中导入相关依赖：

```xml
	<dependency>
		<groupId>commons-fileupload</groupId>
		<artifactId>commons-fileupload</artifactId>
		<version>1.5</version>
	</dependency>
	<dependency>
		<groupId>commons-io</groupId>
		<artifactId>commons-io</artifactId>
		<version>2.15.1</version>
	</dependency>
```

```java
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
```
判断文件是否存在使用上面提到的fileExist方法处理
点击退出登录按钮，则清空LOGIN_STATUS session，然后返回登陆页面，要求客户登录：
![退出登录](https://img-blog.csdnimg.cn/b83b41a4b7714c97ba38a40af039a0c6.png#pic_center)
对应的退出登录请求交给Controller的logout方法处理：

```java
    //退出登录
    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("path");
        session.removeAttribute("LOGIN_STATUS");
        return "index";
    }
```
用户点击注销账户按钮，则弹出选择框让用户再次确认是否注销账户：
![注销账户](https://img-blog.csdnimg.cn/abc9eb2f98d449f98ae1e6291f8b3aad.png#pic_center)
![提示框](https://img-blog.csdnimg.cn/d00268ec31274046948ca104531c5a1a.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
当用户点击确认后，则开始执行注销用户任务，注销成功后，弹出提示框提示用户：
![提示](https://img-blog.csdnimg.cn/1fa0705958d94737ac848060f6eb6656.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV29sZi5HZW5u,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)
注销用户请求交给Controller的deleteUser方法处理：

```java
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
```
首先调用上面提到的delete方法，删除该用户的个人目录/spring，然后修改userinfo.dat，删除该用户的账号密码信息，修改userinfo.dat使用removeUserFromFile方法：

```java
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
```
查看HDFS根目录，发现spring用户的个人目录已经被移除：
![查看根目录](https://img-blog.csdnimg.cn/a08abb91b236469093195bbe14e323a8.png#pic_center)
查看userinfo.dat文件，发现spring用户的个人信息已经被删除：
![查看userinfo.dat](https://img-blog.csdnimg.cn/e31bf26ead694580b4055962d646fde4.png#pic_center)
然后清空session，返回登陆页面，完成注销账户的所有操作

**至此本云盘的所有功能以及代码都说明完毕！**

## 存在的一些问题
后端的话，我觉得优化空间还是很大的，代码的逻辑上应该是可以优化的

关于前端的话，我属于前端小白来着，非常不会写前端
我觉得文件上传可以写一个进度条，然后一些交互通讯其实可以用AJAX来处理
