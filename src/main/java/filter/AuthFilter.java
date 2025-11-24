package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

// Chặn mọi yêu cầu gửi đến servlet "/student" (Trang danh sách)
@WebFilter("/student") 
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 1. Kiểm tra session xem đã có user chưa
        HttpSession session = req.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

        // 2. Nếu đã đăng nhập -> Cho qua (Vào trang danh sách)
        if (isLoggedIn) {
            chain.doFilter(request, response);
        } else {
            // 3. Nếu chưa đăng nhập -> Đá về trang Login
            // Lưu lại thông báo lỗi để hiển thị bên trang login
            res.sendRedirect(req.getContextPath() + "/login?error=Access Denied! Please Login First.");
        }
    }

    @Override
    public void destroy() {}
}