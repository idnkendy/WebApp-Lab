<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login System</title>
    <style>
        body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; background: #f0f2f5; }
        .login-box { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); width: 300px; }
        .form-group { margin-bottom: 15px; }
        input { width: 100%; padding: 10px; margin-top: 5px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;}
        button { width: 100%; padding: 10px; background: #1877f2; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold;}
        .error { color: red; font-size: 14px; margin-bottom: 10px; text-align: center;}
        .success { color: green; font-size: 14px; margin-bottom: 10px; text-align: center;}
    </style>
</head>
<body>
    <div class="login-box">
        <h2 style="text-align: center">Đăng Nhập</h2>
        
        <c:if test="${not empty error}">
            <div class="error">${error}</div>
        </c:if>
        <c:if test="${not empty param.message}">
            <div class="success">${param.message}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post">
            <div class="form-group">
                <label>Username</label>
                <input type="text" name="username" required placeholder="Nhập admin hoặc user">
            </div>
            <div class="form-group">
                <label>Password</label>
                <input type="password" name="password" required placeholder="Nhập 123">
            </div>
            <button type="submit">Login</button>
        </form>
        <div style="margin-top: 10px; font-size: 12px; color: #666;">
            Demo: admin / 123
        </div>
    </div>
</body>
</html>