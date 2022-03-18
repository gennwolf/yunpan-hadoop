<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8" %>
<html>
<head>
    <meta charset="utf-8">
    <title>注册</title>
    <!-- Mobile Specific Metas -->
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <!-- Font-->
    <link rel="stylesheet" type="text/css" href="css/nunito-font.css">
    <!-- Main Style Css -->
    <link rel="stylesheet" href="css/style.css"/>
    <meta http-equiv="pragram" content="no-cache">
</head>
<body class="form-v6">
<div class="page-content">
    <div class="form-v6-content">
        <div class="form-left">
            <img src="images/form-v6.png" alt="form">
        </div>
        <form class="form-detail" action="<%=request.getContextPath()%>/register" method="post">
            <h2>用户注册</h2>
            <div class="form-row">
                <input type="text" name="username" id="full-name" class="input-text" placeholder="用户名" required>
            </div>
            <div class="form-row">
                <input type="password" name="userpasswd" id="password" class="input-text" placeholder="密码" required>
            </div>
            <div class="form-row">
                <input type="password" name="userpasswd_confirm" id="comfirm-password" class="input-text" placeholder="确认密码" required>
            </div>
            <p style="color: black">
                ${status}
            </p>
            <div class="form-row-last">
                <input type="submit" name="register" class="register" value="注册">
            </div>
        </form>
    </div>
</div>
</body><!-- This templates was made by Colorlib (https://colorlib.com) -->
</html>