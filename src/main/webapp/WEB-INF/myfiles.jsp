<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <title>File List</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <link href='https://fonts.googleapis.com/css?family=Roboto:400,100,300,700' rel='stylesheet' type='text/css'>

    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
    <meta http-equiv="pragram" content="no-cache">
    <link rel="stylesheet" href="css/style2.css">
    <script>${warning}</script>
    <script>
        history.pushState(null, null, document.URL);
        window.addEventListener('popstate', function () {
            history.pushState(null, null,document.URL);
        });
    </script>
</head>
<body>
<section class="ftco-section">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-6 text-center mb-5">
                <h2 class="heading-section">My Cloud Drive</h2>
            </div>
        </div>
        <div>

        </div>
        <div class="row">
            <div class="fileupload">
                <form method="post" action="<%=request.getContextPath()%>/upload" enctype="multipart/form-data" onsubmit="clickuploadbtn()">
                    <input type="file" name="file" class="selectfilebtn" required>
                    <input type="submit" value="Upload" class="uploadbtn" >
                </form>
                <p id="status"></p>
            </div>

            <div class="col-md-12">
                <div class="table-wrap">
                    <table class="table table-striped">
                        <thead>
                        <tr>
                            <th>Currnet Path: ${currentpath}</th>
                            <th>Create Time</th>
                            <th>Type</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <th scope="row"><a href="<%=request.getContextPath()%>/back">Upper Directory(..)</a></th>
                            <td></td><td></td><td></td>
                        </tr>
                        <c:forEach var="file" items="${filelist}">
                            <tr>
                                <th scope="row"><a href="<%=request.getContextPath()%>/fileHandle?filename=${file.name}&type=${file.type}">${file.name}</a></th>
                                <td>${file.date}</td>
                                <td>${file.type}</td>
                                <td><a href="<%=request.getContextPath()%>/deleteFile?filename=${file.name}">Delete</a></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="useract">
                <button id="mkdirbtn" onclick="enablemkdir()">Create Directory</button>
                <button id="logoutbtn" onclick="window.location.href='<%=request.getContextPath()%>/logout'">Logout</button>
                <a id="deleteuser" href="<%=request.getContextPath()%>/deleteUser" onclick="deleteUserCheck()">Delete Account</a>
            </div>
        </div>
    </div>
</section>
<script>
    function clickuploadbtn() {
        document.getElementById("status").innerHTML = "FILE UPLOADING! Please do not refresh or close the page!";
    }
    function deleteUserCheck() {
        return confirm("ALL DATA of the current account will be LOST!");
    }
    function enablemkdir() {
        var dirname = prompt("Please enter the directory name: ");
        if(dirname != '' && dirname != null)
            window.location.href = "<%=request.getContextPath()%>/makeDirectory?dirname=" + dirname;
        else
            alert("The directory name cannot be EMPTY!");
    }
</script>
<script src="js/jquery.min.js"></script>
<script src="js/popper.js"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/main2.js"></script>
</body>
</html>

