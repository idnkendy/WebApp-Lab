package controller;

import dao.UserDAO;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginController extends HttpServlet {
    
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Nếu đã login rồi thì chuyển vào trang chủ luôn
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/student");
            return;
        }
        // Chưa login thì hiện form
        req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String user = req.getParameter("username");
        String pass = req.getParameter("password");
        
        User account = userDAO.authenticate(user, pass);
        
        if (account != null) {
            // Đăng nhập thành công -> Tạo session
            HttpSession session = req.getSession();
            session.setAttribute("user", account);
            session.setMaxInactiveInterval(30 * 60); // 30 phút
            
            // Chuyển hướng vào trang danh sách sinh viên
            resp.sendRedirect(req.getContextPath() + "/student");
        } else {
            // Thất bại -> Quay lại login và báo lỗi
            req.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
            req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
        }
    }
}