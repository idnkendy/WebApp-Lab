package dao;

import model.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserDAO extends StudentDAO {

    // --- CẤU HÌNH DB ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/student_management";
    private static final String DB_USER = "root";
    // KIỂM TRA LẠI MẬT KHẨU CỦA BẠN (XAMPP thường là chuỗi rỗng "")
    private static final String DB_PASSWORD = "deocook01"; 
    
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy Driver MySQL", e);
        }
    }

    public User authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";

        System.out.println("--- BẮT ĐẦU DEBUG LOGIN ---");
        System.out.println("User nhập: " + username);
        System.out.println("Pass nhập: " + password);

        try (Connection conn = getConnection(); 
             PreparedStatement ps = conn.prepareStatement(query)) {

            System.out.println("✅ Kết nối Database thành công!");

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("✅ Tìm thấy username trong DB.");
                String dbHash = rs.getString("password");
                System.out.println("Hash trong DB: " + dbHash);

                // Kiểm tra pass
                boolean check = BCrypt.checkpw(password, dbHash);
                System.out.println("👉 Kết quả so sánh BCrypt: " + check);

                if (check) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("role")
                    );
                } else {
                    System.out.println("❌ Sai mật khẩu (Hash không khớp)!");
                }
            } else {
                System.out.println("❌ Không tìm thấy username này trong bảng users.");
            }
        } catch (Exception e) {
            System.out.println("❌ LỖI KẾT NỐI DATABASE HOẶC CODE!");
            System.out.println("Lỗi chi tiết: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}